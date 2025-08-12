plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

dependencies {
    api(project(":surf-content-creator-core"))
    api(project(":surf-content-creator-fallback"))

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.3.0")
}

velocityPluginFile {
    main = "dev.slne.surf.content.creator.velocity.VelocityContentCreatorPlugin"

    pluginDependencies {
        register("commandapi")
        register("miniplaceholders")
    }
}
