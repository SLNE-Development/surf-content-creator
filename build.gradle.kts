import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.4+")
    }
}

allprojects {
    group = "dev.slne.surf.content.creator"
    version = findProperty("version") as String

    tasks.withType<ShadowJar> {
        exclude("kotlin/**")

        val relocations = mutableSetOf(
            "feign",
            "com.apollographql",
            "com.benasher44",
            "com.charleskorn",
            "com.fasterxml",
            "com.github.benmanes",
            "com.github.philippheuer",
            "com.github.twitch4j",
            "com.neovisionaries",
            "com.netflix",
            "com.nytimes",
            "io.github",
            "it.krzeminski",
            "net.thauvin",
            "okhttp3",
            "okio",
            "org.apache",
            "orf.checkerframework",
            "org.HdrHistogram",
            "org.intellij",
            "org.jetbrains",
            "org.slf4j",
            "rx",
        )

        relocations.forEach { relocation ->
//            relocate(relocation, "dev.slne.surf.content.creator.libs.$relocation")
        }
    }
}