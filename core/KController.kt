/**
 * package dev.luteoos.core
 */

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import org.koin.core.component.KoinComponent

abstract class KController : KoinComponent {
    protected val disposeBag: CompositeDisposable = CompositeDisposable()

    open fun onStart() {
    }

    open fun onDestroy() {
        disposeBag.clear()
    }

    protected fun start(disposable: Disposable) {
        disposeBag.add(disposable)
    }
}