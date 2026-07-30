package prayit.simplebudget.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface HomeRoute : NavKey {
    @Serializable data object Main : HomeRoute
}

@Serializable
data class BudgetItemRoute(val id: String) : NavKey

internal val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute.Main::class)
            subclass(BudgetItemRoute::class)
        }
    }
}
