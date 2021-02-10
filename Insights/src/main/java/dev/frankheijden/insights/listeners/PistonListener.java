package dev.frankheijden.insights.listeners;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.AddonCuboid;
import dev.frankheijden.insights.api.concurrent.storage.DistributionStorage;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.listeners.InsightsListener;
import dev.frankheijden.insights.api.tasks.ScanTask;
import dev.frankheijden.insights.api.utils.BlockUtils;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PistonListener extends InsightsListener {

    public PistonListener(InsightsPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    /**
     * Handles pistons, blocks the event if necessary.
     * Events are cancelled whenever:
     *   - The chunk is queued for scanning
     *   - The chunk hasn't been scanned yet
     *   - The limit was surpassed (lowest limit)
     * Events are allowed whenever:
     *   - Pushes/retractions happen within the same chunk
     *   - No limit exists for the pushed/retracted material
     */
    private void handlePistonEvent(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            Block relative = block.getRelative(event.getDirection());
            if (handlePistonBlock(block, relative)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean handlePistonBlock(Block from, Block to) {
        Optional<AddonCuboid> cuboidOptional = plugin.getAddonManager().getCuboid(to.getLocation());

        // Always allow piston pushes within the same chunk
        if (!cuboidOptional.isPresent() && BlockUtils.isSameChunk(from, to)) return false;

        Material material = from.getType();
        Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(material, limit -> true);

        // If no limit is present, allow the block to be moved.
        if (!limitOptional.isPresent()) return false;

        Chunk chunk = to.getChunk();
        UUID worldUid = chunk.getWorld().getUID();
        long chunkKey = ChunkUtils.getKey(chunk);

        boolean queued;
        Optional<DistributionStorage> storageOptional;
        if (cuboidOptional.isPresent()) {
            String key = cuboidOptional.get().getKey();
            queued = plugin.getAddonScanTracker().isQueued(key);
            storageOptional = plugin.getAddonStorage().get(key);
        } else {
            queued = plugin.getWorldChunkScanTracker().isQueued(worldUid, chunkKey);
            storageOptional = plugin.getWorldStorage().getWorld(worldUid).get(chunkKey);
        }

        // If the area is already queued, cancel the event and wait for the area to complete scanning.
        if (queued) return true;

        // If the storage is not present, scan it & cancel the event.
        if (!storageOptional.isPresent()) {
            if (cuboidOptional.isPresent()) {
                AddonCuboid cuboid = cuboidOptional.get();
                plugin.getAddonScanTracker().add(cuboid.getAddon());
                ScanTask.scan(plugin, cuboid.toChunkParts(), info -> {}, storage -> {
                    plugin.getAddonScanTracker().remove(cuboid.getAddon());
                    plugin.getAddonStorage().put(cuboid.getKey(), storage);
                });
            } else {
                plugin.getChunkContainerExecutor().submit(chunk);
            }
            return true;
        }

        // Else, the storage is present, and we can apply a limit.
        DistributionStorage storage = storageOptional.get();
        Limit limit = limitOptional.get();

        // Cache doesn't need to updated here just yet, needs to be done in MONITOR event phase.
        return storage.count(limit, material) + 1 > limit.getLimit();
    }
}
