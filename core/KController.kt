/**
 * package dev.luteoos.core
 */

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent

abstract class KController : KoinComponent {
    protected val disposeBag: CompositeDisposable = CompositeDisposable()
    protected val kcontrollerScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    open fun onStart() {
    }

    open fun onStop(){
        disposeBag.clear()
    }

    /**
     * closes [kcontrollerScope] and renders it inactive
     */
    protected open fun onDeInit() {
        disposeBag.clear()
        kcontrollerScope.coroutineContext.cancel(CancellationException("${this::class.simpleName} onDeInit() invoked"))
    }

    protected fun start(disposable: Disposable) {
        disposeBag.add(disposable)
    }
}