 package dev.luteoos.timber

 import android.os.Build
 import android.util.Log
 import java.util.regex.Pattern
 
 class AndroidDebugTree : Timber.Tree() {
     private val fqcnIgnore = listOf(
         Timber::class.java.name,
         Timber.Tree::class.java.name,
         Timber.GenericDebugTree::class.java.name,
         AndroidDebugTree::class.java.name
     )
 
     override val tag: String
         get() = Throwable().stackTrace
             .first { it.className !in fqcnIgnore }
             .let(::createStackElementTag) ?: super.tag
 
     /**
      * Extract the tag which should be used for the message from the `element`. By default
      * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
      * becomes `Foo`).
      */
     private fun createStackElementTag(element: StackTraceElement): String? {
         var tag = element.className.substringAfterLast('.')
         val m = ANONYMOUS_CLASS.matcher(tag)
         if (m.find()) {
             tag = m.replaceAll("")
         }
         // Tag length limit was removed in API 26.
         return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
             tag
         } else {
             tag.substring(0, MAX_TAG_LENGTH)
         }
     }
 
     override fun log(level: Timber.LogLevel, message: String, t: Throwable?, vararg args: Any?) {
         val formattedMessage = message.format(args)
         when (level) {
             Timber.LogLevel.ERROR -> Log.e(tag, formattedMessage, t)
             Timber.LogLevel.WARN -> Log.w(tag, formattedMessage, t)
             Timber.LogLevel.INFO -> Log.i(tag, formattedMessage, t)
             Timber.LogLevel.DEBUG -> Log.d(tag, formattedMessage, t)
             Timber.LogLevel.ASSERT -> Log.wtf(tag, formattedMessage, t)
             else -> Log.v(tag, formattedMessage, t)
         }
     }
 
     override fun analytics(
         message: String,
         t: Throwable?,
         map: Map<String, String>,
         vararg args: Any?
     ) {
         Log.v(
             tag,
             buildString {
                 append(message.format(args))
                 map.forEach {
                     append("\n\t" + it.key + " -> " + it.value)
                 }
             },
             t
         )
     }
 
     companion object {
         private const val MAX_TAG_LENGTH = 23
         private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
     }
 }