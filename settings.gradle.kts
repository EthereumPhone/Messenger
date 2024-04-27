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
        maven { url = uri("https://jitpack.io") }
    }
    

}


rootProject.name = "Messenger"
include(":app")
include(":feature:chat")
include(":feature:contracts")
include(":core:data")
include(":core:domain")
include(":core:database")
include(":core:datastore")
include(":core:common")
include(":android-smsmms")

