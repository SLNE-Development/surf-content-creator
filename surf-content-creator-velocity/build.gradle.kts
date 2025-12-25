plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-content-creator-core"))
    api(project(":surf-content-creator-fallback"))
}

velocityPluginFile {
    main = "dev.slne.surf.content.creator.velocity.VelocityContentCreatorPlugin"

    authors = listOf("Ammo", "red")

    pluginDependencies {
        register("commandapi")
        register("miniplaceholders")
    }
}
