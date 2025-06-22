import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.paper.PaperPluginDescription
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.pluginYml)
}

val dependencyDir = "$group.dependencies"

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.placeholderapi)
    implementation(libs.cloudPaper)
    implementation(libs.cloudAnnotations)
    compileOnly(project(":Insights-API"))
}

tasks.withType<ShadowJar> {
    exclude("com/mojang/**")
    relocate("com.github.zafarkhaja.semver", "$dependencyDir.semver")
    relocate("org.incendo.cloud", "$dependencyDir.cloud")
    relocate("io.leangen.geantyref", "$dependencyDir.typetoken")
    relocate("me.lucko.commodore", "$dependencyDir.commodore")
}

paper {
    main = "dev.frankheijden.insights.Insights"
    description = "Insights about your server and regional block limits"
    apiVersion = "1.21.6"
    website = "https://github.com/InsightsPlugin/Insights"
    authors = listOf("FrankHeijden")
    serverDependencies {
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }

    permissions {
        register("insights.info") {
            description = "Allows you to see information about insights"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}
