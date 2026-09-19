package prayit.simplebudget.core.data

import java.io.File

actual fun dataDirPath(): String =
    File(System.getProperty("user.home") ?: ".", ".simplebudget").apply { mkdirs() }.absolutePath
