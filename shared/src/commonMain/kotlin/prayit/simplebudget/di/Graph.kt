package prayit.simplebudget.di

import dev.zacsweers.metro.createGraph

object Graph {
    val app: AppGraph by lazy { createGraph<AppGraph>() }
}
