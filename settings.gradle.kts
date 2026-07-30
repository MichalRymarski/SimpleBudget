rootProject.name = "SimpleBudget"

pluginManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}

val moduleList = listOf(
    ":shared",
    ":androidApp",
    ":desktopApp",
    ":core:utils",
    ":core:resources",
    ":core:components",
    ":core:domain",
    ":core:data",
    ":core:export",
    ":feature:home",
    ":feature:budgetItem",
)
moduleList.forEach {
    include(it)
}

