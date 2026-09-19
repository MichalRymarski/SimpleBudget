package prayit.simplebudget.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(path: String): DataStore<Preferences>
