package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import dev.frankheijden.insights.api.reflection.RTileEntityTypes;
import dev.frankheijden.insights.api.tasks.ScanTask;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandScanWorld extends InsightsCommand {

    public CommandScanWorld(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scanworld tile")
    @CommandPermission("insights.scanworld.tile")
    private void handleTileScan(Player player) {
        handleScan(player, RTileEntityTypes.getTileEntityMaterials(), false);
    }

    @CommandMethod("scanworld all")
    @CommandPermission("insights.scanworld.all")
    private void handleAllScan(Player player) {
        handleScan(player, null, false);
    }

    @CommandMethod("scanworld custom <materials>")
    @CommandPermission("insights.scanworld.custom")
    private void handleCustomScan(Player player, @Argument("materials") Material[] materials) {
        handleScan(player, new HashSet<>(Arrays.asList(materials)), true);
    }

    /**
     * Scans chunks in the world of a player.
     */
    public void handleScan(Player player, Set<Material> materials, boolean displayZeros) {
        World world = player.getWorld();

        // Generate chunk parts
        Chunk[] chunks = world.getLoadedChunks();
        List<ChunkPart> chunkParts = new ArrayList<>(chunks.length);
        for (Chunk chunk : chunks) {
            chunkParts.add(ChunkLocation.of(chunk).toPart());
        }

        ScanTask.scanAndDisplay(plugin, player, chunkParts, materials, displayZeros);
    }
}
