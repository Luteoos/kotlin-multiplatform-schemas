package dev.luteoos.core

interface KControllerInterface<stateData, stateError> {
    fun getStateFlow(): StateFlow<KState<stateData, stateError>>
    fun watchState(): CFlow<KState<stateData, stateError>>
    fun onStart()
    fun onStop()
    fun onDeInit()
}