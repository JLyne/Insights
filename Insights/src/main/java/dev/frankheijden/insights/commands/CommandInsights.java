package dev.frankheijden.insights.commands;

import dev.frankheijden.insights.Insights;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.commands.InsightsCommand;
import dev.frankheijden.insights.api.config.Messages;
import dev.frankheijden.insights.api.utils.StringUtils;
import dev.frankheijden.insights.concurrent.ContainerExecutorService;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@Command("insights|in")
public class CommandInsights extends InsightsCommand {

    public CommandInsights(InsightsPlugin plugin) {
        super(plugin);
    }

    @Command("reload")
    @Permission("insights.reload")
    private void reloadConfigurations(CommandSender sender) {
        plugin.reloadConfigs();
        plugin.reload();
        plugin.getMessages().getMessage(Messages.Key.CONFIGS_RELOADED).sendTo(sender);
    }

    @Command("stats")
    @Permission("insights.stats")
    private void displayStatistics(CommandSender sender) {
        ContainerExecutorService executor = ((Insights) plugin).getExecutor();
        plugin.getMessages().getMessage(Messages.Key.STATS).addTemplates(
                Messages.tagOf("chunks_scanned", StringUtils.pretty(executor.getCompletedTaskCount())),
                Messages.tagOf("queue_size", StringUtils.pretty(executor.getQueueSize()))
        ).sendTo(sender);
    }
}
