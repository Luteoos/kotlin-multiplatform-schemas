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

    open fun onDestroy() {
        disposeBag.clear()
        kcontrollerScope.coroutineContext.cancel(CancellationException("${this::class.simpleName} onDestroy() invoked"))
    }

    protected fun start(disposable: Disposable) {
        disposeBag.add(disposable)
    }
}