package dev.frankheijden.insights.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
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

public class CommandScan extends InsightsCommand {

    public CommandScan(InsightsPlugin plugin) {
        super(plugin);
    }

    @CommandMethod("scan <radius> tile")
    @CommandPermission("insights.scan.tile")
    private void handleTileScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius
    ) {
        handleScan(player, radius, RTileEntityTypes.getTileEntityMaterials(), false);
    }

    @CommandMethod("scan <radius> all")
    @CommandPermission("insights.scan.all")
    private void handleAllScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius
    ) {
        handleScan(player, radius, null, false);
    }

    @CommandMethod("scan <radius> custom <materials>")
    @CommandPermission("insights.scan.custom")
    private void handleCustomScan(
            Player player,
            @Argument("radius") @Range(min = "0", max = "25") int radius,
            @Argument("materials") Material[] materials
    ) {
        handleScan(player, radius, new HashSet<>(Arrays.asList(materials)), true);
    }

    /**
     * Scans chunks in a radius around a player.
     */
    public void handleScan(Player player, int radius, Set<Material> materials, boolean displayZeros) {
        Chunk chunk = player.getChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Generate chunk parts
        int edge = (2 * radius) + 1;
        int chunkCount = edge * edge;
        List<ChunkPart> chunkParts = new ArrayList<>(chunkCount);
        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                chunkParts.add(new ChunkLocation(world, x, z).toPart());
            }
        }

        ScanTask.scanAndDisplay(plugin, player, chunkParts, materials, displayZeros);
    }
}
