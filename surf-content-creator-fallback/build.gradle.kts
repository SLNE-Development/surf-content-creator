plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-content-creator-core"))
    api(libs.surf.database)
}