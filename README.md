# KMM Schemas

1. ## `buildSrc`

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
2. ## Kotlin new memory model
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
3. ## `core/flow/CFlow.kt` 
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
4. ## `core/flow/KHandler.kt`
    
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
5. ## `core/di/Koin.kt`
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
6. ## **in-progress** `core/KController.kt` 
   *ViewController/ViewModel* base `class`
    
    ### Dependencies
    ```kotlin
    // <koin_core_version = 3.2.0>
    api("io.insert-koin:koin-core:<koin_core_version>")
    
    // <badoo_reaktive_version = 3.2.0>
    implementation("com.badoo.reaktive:reaktive:<badoo_reaktive_version>")
    implementation("com.badoo.reaktive:coroutines-interop:<badoo_reaktive_version>")
    ```
    
