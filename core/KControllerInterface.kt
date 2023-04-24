package dev.luteoos.core

interface KControllerInterface<stateData, stateError> {
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    fun getStateFlow(): StateFlow<KState<stateData, stateError>>
    fun watchState(): CFlow<KState<stateData, stateError>>
    fun onStart()
    fun onStop()
    fun onDeInit()
}