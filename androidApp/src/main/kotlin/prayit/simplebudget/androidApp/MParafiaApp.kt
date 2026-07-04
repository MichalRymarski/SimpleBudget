package prayit.simplebudget.androidApp

import android.app.Application
import prayit.simplebudget.core.data.dbSetup.AppContext

class MParafiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.instance = applicationContext
    }
}
