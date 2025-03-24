plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-content-creator-api"))
    api(libs.kaml)
    api(libs.twitch4j)
}