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
2. ## `core/flow/CFlow.kt` 
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
3. ## Kotlin new memory model
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