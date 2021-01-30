package eu.monniot.subpleaseapp.clients

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import okhttp3.internal.platform.Platform
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

class CurlLoggingInterceptor @JvmOverloads constructor(
    private val logger: Logger = Logger.DEFAULT
) : Interceptor {

    private var curlOptions: String? = null

    /** Set any additional curl command options (see 'curl --help').  */
    fun setCurlOptions(curlOptions: String?) {
        this.curlOptions = curlOptions
    }

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val request = chain.request()
        var compressed = false
        var curlCmd = "curl -v"
        if (curlOptions != null) {
            curlCmd += " $curlOptions"
        }
        curlCmd += " -X" + request.method
        val headers = request.headers
        var i = 0
        val count = headers.size
        while (i < count) {
            val name = headers.name(i)
            val value = headers.value(i)
            if ("Accept-Encoding".equals(name, ignoreCase = true) && "gzip".equals(
                    value,
                    ignoreCase = true
                )
            ) {
                compressed = true
            }
            curlCmd += " -H \"$name: $value\""
            i++
        }
        val requestBody = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data '" + buffer.readString(charset!!).replace("\n", "\\n") + "'"
        }
        curlCmd += (if (compressed) " --compressed " else " ") + request.url
        logger.log("╭--- cURL (" + request.url + ")")
        logger.log(curlCmd)
        logger.log("╰--- (copy and paste the above line to a terminal)")
        return chain.proceed(request)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

    }

    interface Logger {
        fun log(message: String)

        companion object {
            /** A [Logger] defaults output appropriate for the current platform. */
            @JvmField
            val DEFAULT: Logger = object : Logger {
                override fun log(message: String) {
                    Platform.get().log(message, Platform.INFO, null)
                }
            }
        }
    }

}