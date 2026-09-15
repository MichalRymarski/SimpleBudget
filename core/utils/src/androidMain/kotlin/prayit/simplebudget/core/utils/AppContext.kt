package prayit.simplebudget.core.utils

import android.content.Context

object AppContext {
    lateinit var instance: Context

    fun requireInstance(): Context {
        require(::instance.isInitialized) {
            "AppContext.instance accessed before the Application.onCreate() initialized it"
        }
        return instance
    }
}
