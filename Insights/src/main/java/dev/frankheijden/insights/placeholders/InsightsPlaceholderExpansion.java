package dev.frankheijden.insights.placeholders;

import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.concurrent.storage.Storage;
import dev.frankheijden.insights.api.config.LimitEnvironment;
import dev.frankheijden.insights.api.config.limits.Limit;
import dev.frankheijden.insights.api.objects.wrappers.ScanObject;
import dev.frankheijden.insights.api.utils.ChunkUtils;
import dev.frankheijden.insights.api.utils.StringUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class InsightsPlaceholderExpansion extends PlaceholderExpansion {

    private final InsightsPlugin plugin;

    public InsightsPlaceholderExpansion(InsightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "insights";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String[] args = identifier.split("_");
        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "limits":
                if (args.length < 3) break;

                String itemString = StringUtils.join(args, "_", 2).toUpperCase(Locale.ENGLISH);
                final ScanObject<?> item;
                try {
                    item = ScanObject.parse(itemString);
                } catch (IllegalArgumentException ex) {
                    return "";
                }

                Location location = player.getLocation();
                World world = location.getWorld();
                UUID worldUid = world.getUID();
                LimitEnvironment env = new LimitEnvironment(player, world.getName());
                Optional<Limit> limitOptional = plugin.getLimits().getFirstLimit(item, env);
                if (limitOptional.isEmpty()) break;

                Limit limit = limitOptional.get();
                switch (args[1].toLowerCase(Locale.ENGLISH)) {
                    case "name": return limit.getLimit(item).getName();
                    case "max": return String.valueOf(limit.getLimit(item).getLimit());
                    case "count":
                        Optional<Region> regionOptional = plugin.getAddonManager().getRegion(location);
                        Optional<Storage> storageOptional;
                        if (regionOptional.isPresent()) {
                            Region region = regionOptional.get();
                            storageOptional = plugin.getAddonStorage().get(region.getKey());
                        } else {
                            long chunkKey = ChunkUtils.getKey(location);
                            storageOptional = plugin.getWorldStorage().getWorld(worldUid).get(chunkKey);
                        }
                        return storageOptional.map(storage -> String.valueOf(storage.count(limit, item)))
                                .orElse("");
                    default: break;
                }
                break;
            default: break;
        }
        return "";
    }
}
