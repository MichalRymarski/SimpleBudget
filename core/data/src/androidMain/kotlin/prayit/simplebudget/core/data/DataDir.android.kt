package prayit.simplebudget.core.data

import prayit.simplebudget.core.utils.AppContext

actual fun dataDirPath(): String =
    AppContext.requireInstance().filesDir.absolutePath
