plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "surf-content-creator"

include("surf-content-creator-api")
include("surf-content-creator-core")
include("surf-content-creator-velocity")
include("surf-content-creator-fallback")
