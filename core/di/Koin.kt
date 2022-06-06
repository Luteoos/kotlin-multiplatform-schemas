/**
 * package dev.luteoos.core
 */

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(coreModule)
}

/**
 * called by iOS and other non-JVM targets by
 * `KoinKt.doInitKoin()`
 */
fun initKoin() = initKoin {}
