package prayit.simplebudget.androidApp

import android.app.Application
import prayit.simplebudget.core.utils.AppContext
import prayit.simplebudget.core.utils.initLog

class MParafiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.instance = applicationContext
        initLog()
    }
}
