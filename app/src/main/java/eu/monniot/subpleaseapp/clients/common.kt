package eu.monniot.subpleaseapp.clients

import okhttp3.*
import okio.BufferedSource
import java.io.IOException

typealias ApiCallback<T> = (OkHttpClient, (Result<T>) -> Unit) -> Unit

internal fun OkHttpClient.rawExecute(request: Request, cb: (Result<Response>) -> Unit) {

    this.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cb(Result.NetworkError(e, call))
        }

        override fun onResponse(call: Call, response: Response) {
            cb(Result.Success(response))
        }
    })
}

private fun OkHttpClient.execAs(request: Request, cb: (Result<ResponseBody>) -> Unit) {
    this.rawExecute(request) {
        cb(it.flatMap { response ->
            if (response.code != 200)
                response.use {
                    Result.NonOkError(response.code, response.body!!.string())
                }
            else {
                Result.Success(response.body!!)
            }
        })
    }
}

// Remember to .use the resulting source
internal fun OkHttpClient.execAsSource(request: Request, cb: (Result<BufferedSource>) -> Unit) {
    this.execAs(request) {
        cb(it.map { body ->
            body.source()
        })
    }
}


