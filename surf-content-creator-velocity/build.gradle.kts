plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-content-creator-core"))
    api(project(":surf-content-creator-fallback"))
    
    compileOnly("com.github.NEZNAMY:TAB-API:5.0.4")
}

velocityPluginFile {
    main = "dev.slne.surf.content.creator.velocity.VelocityContentCreatorPlugin"

    pluginDependencies {
        register("surf-api-velocity")
        register("surf-data-velocity")
        register("commandapi")
        register("tab")
    }
}
