/**
 * package dev.luteoos.core
 */

/**
 * Based on [moko - ResourceState](https://github.com/icerockdev/moko-mvvm/blob/master/mvvm-state/src/commonMain/kotlin/dev/icerock/moko/mvvm/ResourceState.kt)
 */
sealed class State<out T, out E>{
    class Loading<out T, out E>: State<T, E>()
    class Empty<out T, out E>: State<T, E>()
    data class Success<out T, out E>(val content: T) : State<T, E>()
    data class Error<out T, out E>(val error: E) : State<T, E>()

    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isEmpty(): Boolean = this is Empty
    fun isFailed(): Boolean = this is Error

    fun data(): T? = (this as? Success)?.content
    fun error(): E? = (this as? Error)?.error
}