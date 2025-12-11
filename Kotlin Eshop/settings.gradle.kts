pluginManagement {
    repositories {
        gradlePluginPortal()  // <== This is essential for kotlin plugin
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Eshop"
include(":app")
