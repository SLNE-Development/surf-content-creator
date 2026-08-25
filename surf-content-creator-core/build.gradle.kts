plugins {
    id("dev.slne.surf.api.gradle.core")
}

dependencies {
    api(project(":surf-content-creator-api"))
    api(libs.twitch4j)
    compileOnly("dev.slne.surf.social:surf-social-api:+")
}

sourceSets.test {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().compileClasspath
}