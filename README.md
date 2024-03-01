# KMM Schemas

**If you have any tips/tricks/ideas you think are worth including please [raise new issue](https://github.com/Luteoos/kotlin-multiplatform-schemas/issues/new)**

## Overview
- [buildSrc](#buildsrc)
- [Sapling](#sapling)
- [Kotlin New Memory Model](#kotlin-new-memory-model)
- [CFlow.kt](#coreflowcflowkt)
- [KHandler.kt](#coreflowkhandlerkt)
- [Koin.kt](#coredikoinkt)
- [RxExtensions.kt](#corerxrxextensionskt)
- [KState.kt](#corekstatekt)
- [KController.kt](#corekcontrollerkt)

## Useful plugins
- [Kotlin Multiplatform Plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile)
- [Xcode Kotlin](https://github.com/touchlab/xcode-kotlin)
- [moko-KSwift](https://github.com/icerockdev/moko-kswift) - autogenerate `Enum` for `sealed class`
  
## Sources

- ## `buildSrc`

    Folder structure for adding dependencies to Kotlin project via class `Dependencies.kt`

    ### How to use
    ```kotlin
    object Dependencies {
        val your_dependencies_group = mapOf(
            "<your_dependency_id>" to "<dependency(ex. owner:package:version)>"
        )
    }
    ```
    In your `<module>/build.gradle.kts`
    ```kotlin
    dependencies {
        Dependencies.your_dependencies_group.forEach{
            implementation(it.value)
        }
    }
    ```
    or
    ```kotlin
    dependencies {
        implementation(Dependencies.your_dependencies_group["<your_dependency_id>"])
    }
    ```
- ## Sapling
  
  Simple KMP logging library idea-based on [Timber](https://github.com/JakeWharton/timber)

  ### How to use
  - Copy-paste `Sap.kt` into your project
  - [Optionally] Change package name
  
- ## Kotlin new memory model(Deprecated)
   - add to `gradle.properties` 
    ```properties
    # enable new MemModel - no object lock for thread
    kotlin.native.binary.memoryModel=experimental
    kotlin.native.binary.freezing=disabled
    ```
   -  add to Kotlin multiplatform module `build.gradle.kts`
   ```kotlin
   kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
        binaries.all {
            binaryOptions["memoryModel"] = "experimental"
            binaryOptions["freezing"] = "disabled"
        }
    }
   ```
- ## `core/flow/CFlow.kt` 
    [source](https://github.com/Kotlin/kmm-production-sample/blob/635ae763f7e666d25827f8e221672020063e617f/shared/src/iosMain/kotlin/com/github/jetbrains/rssreader/core/CFlow.kt)

    Contains `CFlow<T>` - wrapper for `kotlinx.coroutines.Flow` for mapping complex generic `<T>` in *iOS* code
    - *Android* can use `Flow` directly
    - *iOS* can use `Flow` via `.wrap().watch { /* callback */ }`
    ### Dependencies
    ```kotlin
    // <coroutines_version = 1.6.2>
    org.jetbrains.kotlinx:kotlinx-coroutines-core:<coroutines_version>
    ```
    
    ### How to use
    ```kotlin
    // core/src/commonMain/kotlin/dev/luteoos/<ExampleKotlinController>
    
    class ExampleKotlinController<T>{
        val exampleFlow = MutableStateFlow<T> // or any other Flow()

        fun updateExampleFlow(value: T){
            CoroutineScope(Dispatchers.Main).apply { 
                launch { 
                    exampleFlow.emit(value)
                }
            }
        }
    }
    ```
    ```kotlin
    // core/src/<commonMain or iosMain>/kotlin/dev/luteoos/core

    class CFlow<T: any>{
        ...
    }
    ```
     ```kotlin
    // core/src/iosMain/kotlin/dev/luteoos/utils/<iosRedux>

    fun ExampleKotlinController.watchExampleFlow() = this.exampleFlow.wrap()
    ```
    ```swift
    // iosApp/<ExampleIosClass>

    class ExampleIosClass : ObservableObject {
        @Published public var example: T? // ? for nullable

        let controller: ExampleKotlinController
        var exampleWatcher: Closeable? // can be made into bulk closing

        init(controller: ExampleKotlinController){
            self.controller = controller
            exampleWatcher = self.controller.watchExampleFlow().watch { [weak self] example in
                self?.example = example
            }
        }

        deinit {
            exampleWatcher?.close
        }
    }
    ```
- ## `core/flow/KHandler.kt`
    
    *Kotlin Native* implementation of `Handler` known from *JVM* for easy logic delay
    ### Dependencies
    ```kotlin
    // <coroutines_version = 1.6.2>
    org.jetbrains.kotlinx:kotlinx-coroutines-core:<coroutines_version>
    ```
    
    ### How to use
    ```kotlin
    KHandler().postDelayed({
            // your logic
        }, /*time in milis*/)
    
    // optionally
    .getJob() // returns currently running Job or null
    .setDispatcher(Dispatcher) // call before postDelayed to change execution Dispatcher
    ```
- ## `core/di/Koin.kt`
   [source](https://github.com/joreilly/PeopleInSpace/commit/e317d84dd0e466b454cf334604d210c88baa877d)

   `Koin.kt` - file with two `fun initKoin()`
   - *Android* use `initKoin { /*<your koin modules/context here>*/ }` to initialize `Koin`
   - *iOS* use `KoinKt.initKoin()` to initialize `Koin` for *KMM* module 
   - add your *core* modules inside `fun initKoin(appDeclaration: KoinAppDeclaration = {})`
   ### Dependencies
    ```kotlin
    // <koin_core_version = 3.2.0>
    api("io.insert-koin:koin-core:<koin_core_version>")
    ```
    
    ### How to use
    ```swift
    import core

    @main
    struct iOSApp: App{
        init(){
            KoinKt.doInitKoin()
        }
        //...
    }
    ```
    ```kotlin
    class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@MainApplication)
            modules(/*your_modules*/)
        }
        }
    }
    ```

- ## `core/rx/RxExtensions.kt`
    
    Extension methods for easier subscription to reaktive streams such as `Observable`, `Single`, `Completable`
    ### Dependencies
    ```kotlin
    // <badoo_reaktive_version = 3.2.0>
    implementation("com.badoo.reaktive:reaktive:<badoo_reaktive_version>")
    ```
    ### How to use
    ```kotlin
    SomeObservable<T>().resolve(
        onNext = {  },
        onError = {  },
        onComplete = {  }
    )

    //or

    SomeObservable<T>().resolve{
        //it...
    }
    
    ```
   
- ## `core/KState.kt`
  - **best use with** [moko-kswift gradle plugin](https://github.com/icerockdev/moko-kswift) or copy `KStateSwift.swift` to ios project   
   **Warning when using `KStateSwift`**  
   It might generate fatal error when changed and not updated manually
  - `sealed interface` for simple state management built in `KController.kt`
  - takes parameters `T` for **data type** and `E` for **error type**
  - *iOS* usage simplified thanks for `fun` inside `KState.kt`
  - *Android* usage as normal `sealed class`
  - States:
    - `Success(data: T)`
    - `Error(error: E)`
    - `Loading`
    - `Empty`

- ## `core/KController.kt` 
   **in-progress**
   *ViewController/ViewModel* base `class`
   - `protected` does **not** exist in `Swift`
    
    ### Dependencies
    ```kotlin
    // <koin_core_version = 3.2.0>
    api("io.insert-koin:koin-core:<koin_core_version>")
    
    // <badoo_reaktive_version = 3.2.0>
    implementation("com.badoo.reaktive:reaktive:<badoo_reaktive_version>")
    implementation("com.badoo.reaktive:coroutines-interop:<badoo_reaktive_version>")
    
    // <coroutines_version = 1.6.2>
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:<coroutines_version>")
    ```
- ## `core/KControllerInterface.kt`
    ### Example 
    ```kotlin
    interface MyContorllerInterface: KControllerInterface<Data, Data>{
        //...
    }
    ```
    Mocking in swift
    ```swift
    class MockController : KControllerInterface {
    func onDeInit() {
        print("deinit")
    }
    
    func onStart() {
        print("start")
    }
    
    func onStop() {
        print("stop")
    }
    
        func watchState() -> CFlow<KState> {
        let mockValue = /* MockState */
        return CFlowCompanion().getMock(mockValue: mockValue as KState) as! CFlow<KState>
    }
}
    ```