pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Workout"
include(":app")
include(":wear")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":core:data")
include(":features:workout:bridge")
include(":features:workout:impl")
include(":features:history:bridge")
include(":features:history:impl")
include(":features:stats:bridge")
include(":features:stats:impl")
include(":features:onboarding:bridge")
include(":features:onboarding:impl")

project(":core:data").projectDir = file("core/data")
project(":features:workout:bridge").projectDir = file("features/workout/bridge")
project(":features:workout:impl").projectDir = file("features/workout/impl")
project(":features:history:bridge").projectDir = file("features/history/bridge")
project(":features:history:impl").projectDir = file("features/history/impl")
project(":features:stats:bridge").projectDir = file("features/stats/bridge")
project(":features:stats:impl").projectDir = file("features/stats/impl")
project(":features:onboarding:bridge").projectDir = file("features/onboarding/bridge")
project(":features:onboarding:impl").projectDir = file("features/onboarding/impl")
