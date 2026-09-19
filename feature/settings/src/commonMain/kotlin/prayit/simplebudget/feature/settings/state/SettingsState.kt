package prayit.simplebudget.feature.settings.state

import prayit.simplebudget.core.domain.model.AppSettings

sealed interface SettingsState {
    data object Loading : SettingsState
    data class Content(
        val settings: AppSettings = AppSettings(),
        val isSaving: Boolean = false,
    ) : SettingsState
}
