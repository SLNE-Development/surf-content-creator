plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "surf-content-creator"

include("surf-content-creator-api")
include("surf-content-creator-core")
include("surf-content-creator-velocity")