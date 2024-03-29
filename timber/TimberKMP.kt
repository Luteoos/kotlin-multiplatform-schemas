package dev.luteoos.timber

/**
 *
 * [version = 2]
 * @author Mateusz Lutecki
 */
object Timber {
    private val tag: String = "[${this::class.simpleName}]"

    init {
        println("$tag created")
    }

    private val forest = mutableSetOf<Tree>()

    fun plantTree(tree: Tree) {
        forest.add(tree)
        println("$tag added Tree ${tree.tag}")
    }

    fun clear() {
        forest.clear()
        println("$tag forest cleared")
    }

    fun log(
        level: LogLevel,
        message: String,
        t: Throwable?,
        map: Map<String, String>,
        vararg args: Any?
    ) {
        when (level) {
            LogLevel.ANALYTICS -> forest.forEach { it.analytics(message, t, map, *args) }
            else -> forest.forEach { it.log(level, message, t, *args) }
        }
    }

    abstract class Tree {
        open val tag: String = "[${this::class.simpleName}]"

        abstract fun log(level: LogLevel, message: String, t: Throwable?, vararg args: Any?)

        abstract fun analytics(
            message: String,
            t: Throwable?,
            map: Map<String, String>,
            vararg args: Any?
        )

        protected fun String.basicFormat(vararg args: Any?): String {
            if (args.isEmpty())
                return this
            val builder = StringBuilder()
            val newStrings = this.split("%[\\d|.]*[sdf]|[%]".toRegex())
            for (i in 0 until args.count()) {
                builder.append("${newStrings[i]}${args[i]}")
            }
            return builder.toString()
        }

    }

    class GenericDebugTree(private val colorWrap: Boolean = true) : Tree() {
        override fun log(level: LogLevel, message: String, t: Throwable?, vararg args: Any?) {
            val messageWithArgs = "Timber.${level}: ${message.basicFormat(*args)}"
            when (level) {
                LogLevel.ERROR -> printError(messageWithArgs, t)
                LogLevel.INFO -> printInfo(messageWithArgs, t)
                LogLevel.WARN -> printWarn(messageWithArgs, t)
                else -> printDefault(messageWithArgs, t)
            }
        }

        override fun analytics(
            message: String,
            t: Throwable?,
            map: Map<String, String>,
            vararg args: Any?
        ) {
            println(buildString {
                append("Timber.ANALYTICS: ${message.basicFormat(*args)}")
                t?.let {
                    append("\n\t")
                    append(it.stackTraceToString())
                }
                map.forEach {
                    append("\n\t" + it.key + " -> " + it.value)
                }
            })
        }

        private fun printError(messageWithArgs: String, t: Throwable?) {
            println(buildString {
                append(wrapColor(messageWithArgs, AnsiColor.ANSI_RED))
                append("\n")
                t?.let {
                    append(wrapColor(it.stackTraceToString(), AnsiColor.ANSI_RED))
                }
            })
        }

        private fun printWarn(messageWithArgs: String, t: Throwable?) {
            println(buildString {
                append(wrapColor(messageWithArgs, AnsiColor.ANSI_YELLOW))
                append("\n")
                t?.let {
                    append(wrapColor(it.stackTraceToString(), AnsiColor.ANSI_YELLOW))
                }
            })
        }

        private fun printInfo(messageWithArgs: String, t: Throwable?) {
            println(buildString {
                append(wrapColor(messageWithArgs, AnsiColor.ANSI_BLUE))
                append("\n")
                t?.let {
                    append(wrapColor(it.stackTraceToString(), AnsiColor.ANSI_BLUE))
                }
            })
        }

        private fun printDefault(messageWithArgs: String, t: Throwable?) {
            println(buildString {
                append(messageWithArgs)
                append("\n")
                t?.let {
                    append(it.stackTraceToString())
                }
            })
        }

        private enum class AnsiColor(val value: String) {
            ANSI_RESET("\u001B[0m"),
            ANSI_BLACK("\u001B[30m"),
            ANSI_RED("\u001B[31m"),
            ANSI_GREEN("\u001B[32m"),
            ANSI_LIGHT_YELLOW("\u001B[93m"),
            ANSI_YELLOW("\u001B[33m"),
            ANSI_YELLOW_BACKGROUND("\u001B[43m"),
            ANSI_BLUE("\u001B[34m"),
            ANSI_PURPLE("\u001B[35m"),
            ANSI_CYAN("\u001B[36m"),
            ANSI_WHITE("\u001B[37m"),
            ANSI_BOLD("\u001B[1m"),
            ANSI_UNBOLD("\u001B[21m"),
            ANSI_UNDERLINE("\u001B[4m"),
            ANSI_STOP_UNDERLINE("\u001B[24m"),
            ANSI_BLINK("\u001B[5m"),
        }

        private inline fun wrapColor(message: String, color: AnsiColor) =
            if (colorWrap)
                "${color.value}$message${AnsiColor.ANSI_RESET.value}"
            else
                message

    }

    /**
     * Int [level] matching [android.util.Log.DEBUG] except [ANALYTICS]
     */
    enum class LogLevel(val level: Int) {
        ANALYTICS(13),
        ASSERT(7),
        DEBUG(3),
        ERROR(6),
        INFO(4),
        VERBOSE(2),
        WARN(5),

    }

    fun e(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.ERROR, message, t, mapOf(), *args)

    fun e(message: String, vararg args: Any?) =
        log(LogLevel.ERROR, message, null, mapOf(), *args)

    fun e(t: Throwable?) =
        log(LogLevel.ERROR, "", t, mapOf())

    fun d(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.DEBUG, message, t, mapOf(), *args)

    fun d(message: String, vararg args: Any?) =
        log(LogLevel.DEBUG, message, null, mapOf(), *args)

    fun d(t: Throwable?) =
        log(LogLevel.DEBUG, "", t, mapOf())

    fun i(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.INFO, message, t, mapOf(), *args)

    fun i(message: String, vararg args: Any?) =
        log(LogLevel.INFO, message, null, mapOf(), *args)

    fun i(t: Throwable?) =
        log(LogLevel.INFO, "", t, mapOf())

    fun v(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.VERBOSE, message, t, mapOf(), *args)

    fun v(message: String, vararg args: Any?) =
        log(LogLevel.VERBOSE, message, null, mapOf(), *args)

    fun v(t: Throwable?) =
        log(LogLevel.VERBOSE, "", t, mapOf())

    fun w(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.WARN, message, t, mapOf(), *args)

    fun w(message: String, vararg args: Any?) =
        log(LogLevel.WARN, message, null, mapOf(), *args)

    fun w(t: Throwable?) =
        log(LogLevel.WARN, "", t, mapOf())

    fun wtf(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.ASSERT, message, t, mapOf(), *args)

    fun wtf(message: String, vararg args: Any?) =
        log(LogLevel.ASSERT, message, null, mapOf(), *args)

    fun wtf(t: Throwable?) =
        log(LogLevel.ASSERT, "", t, mapOf())

    fun analytics(message: String, t: Throwable?, map: Map<String, String>, vararg args: Any?) =
        log(LogLevel.ANALYTICS, message, t, map, *args)

    fun analytics(message: String, t: Throwable?, vararg args: Any?) =
        log(LogLevel.ANALYTICS, message, t, mapOf(), *args)

    fun analytics(message: String, map: Map<String, String>, vararg args: Any?) =
        log(LogLevel.ANALYTICS, message, null, map, *args)

    fun analytics(message: String, vararg args: Any?) =
        log(LogLevel.ANALYTICS, message, null, mapOf(), *args)

}