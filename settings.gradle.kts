pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Pour Sceneform
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HandARTracker"
include(":app")