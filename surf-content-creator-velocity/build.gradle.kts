plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-content-creator-core"))
}

surfPaperPluginApi  {
    mainClass("dev.slne.surf.content.creator.paper.PaperMain")
    generateLibraryLoader(false)

    serverDependencies {
        register("surf-social-paper")
    }

    authors.addAll("twisti", "red")
}
