/**
 * package dev.luteoos.core
 */

import com.badoo.reaktive.disposable.CompositeDisposable
import com.badoo.reaktive.disposable.Disposable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * Base Controller class simplifies [KState] publishing and handling Rx flows
 * @author [Luteoos](http://luteoos.dev)
 */
abstract class KController<stateData, stateError> : KoinComponent {

    protected val disposeBag: CompositeDisposable = CompositeDisposable()
    protected val kcontrollerScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    protected abstract val state: MutableStateFlow<KState<stateData, stateError>>

    fun getStateFlow(): StateFlow<KState<stateData, stateError>> = state

    fun wrapState() = state.wrap()

    open fun onStart() {
    }

    open fun onStop() {
        disposeBag.clear()
    }

    /**
     * closes [kcontrollerScope] and renders it inactive
     */
    open fun onDeInit() {
        disposeBag.clear()
        kcontrollerScope.coroutineContext.cancel(CancellationException("${this::class.simpleName} onDeInit() invoked"))
    }

    protected fun start(disposable: Disposable) {
        disposeBag.add(disposable)
    }

    /**
     * publish new [KState] using [kcontrollerScope]
     */
    internal fun publish(newState: KState<stateData, stateError>){
        kcontrollerScope.launch {
            state.emit(newState)
        }
    }
}