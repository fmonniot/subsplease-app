package eu.monniot.subpleaseapp.clients

import okhttp3.Call
import java.io.IOException

// TODO Rewrite the possible outcome when I know what they should be
sealed class Result<out A> {
    data class NetworkError(val error: IOException, val call: Call) : Result<Nothing>()
    data class NonOkError(val code: Int, val body: String) : Result<Nothing>()
    data class ParsingError(val error: String) : Result<Nothing>()
    data class Success<A>(val value: A) : Result<A>()


    fun <B> map(f: (A) -> B): Result<B> {
        return when (this) {
            is NetworkError -> this
            is NonOkError -> this
            is ParsingError -> this
            is Success -> Success(
                f(this.value)
            )
        }
    }

    fun <B> flatMap(f: (A) -> Result<B>): Result<B> {
        return when (this) {
            is NetworkError -> this
            is NonOkError -> this
            is ParsingError -> this
            is Success -> f(this.value)
        }
    }

    fun get(): A? {
        return when (this) {
            is Success -> this.value
            else -> null
        }
    }

    override fun toString(): String {
        return when(this) {
            is NetworkError -> "NetworkError(error=$error, call=$call)"
            is NonOkError -> "NonOkError(code=$code, body='$body')"
            is ParsingError -> "ParsingError(error='$error')"
            is Success -> "Success(value=$value)"
        }
    }

    companion object {
        fun <B> success(b: B): Result<B> {
            return Success(b)
        }
    }
}