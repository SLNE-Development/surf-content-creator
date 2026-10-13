plugins {
    id("dev.slne.surf.api.gradle.minestom")
}

repositories {
    mavenLocal()
}

minestomPluginFile {
    id = "surf-content-creator-minestom"
    main = "dev.slne.surf.content.creator.minestom.ContentCreatorMinestomPlugin"
    authors = listOf("twisti", "red")

    pluginDependencies {
        register("surf-social-minestom")
    }
}

dependencies {
    api(project(":surf-content-creator-core"))
}
