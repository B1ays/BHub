package ru.blays.hub.core.logic.loggerAdapters

import okhttp3.Interceptor
import okhttp3.Response
import ru.blays.hub.core.logger.Logger
import java.io.IOException

class LoggerInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        val method = request.method
        val url = request.url

        Logger.d(
            tag = TAG,
            message = "Sending request | Start at: $startTime | [ $method ] - $url"
        )

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            Logger.e(
                tag = TAG,
                message = "Failed to send request | [ $method ] - $url",
                throwable = e
            )
            throw e
        }

        Logger.d(
            tag = TAG,
            message = "Received response | Time: ${System.nanoTime() - startTime} ns | Code: ${response.code} | [ $method ] - $url"
        )

        return response
    }

    companion object {
        private const val TAG = "Network"
    }
}