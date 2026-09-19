package prayit.simplebudget.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File

actual fun createDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.create { File(path) }
