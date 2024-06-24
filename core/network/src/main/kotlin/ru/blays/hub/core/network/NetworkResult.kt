package ru.blays.hub.core.network

sealed class NetworkResult <T: Any> {
    data class Success<T: Any>(val data: T) : NetworkResult<T>()
    data class Failure<T: Any>(val error: Throwable) : NetworkResult<T>() {
        private val _data: MutableMap<String, Any> by lazy(::mutableMapOf)

        internal fun <R: Any> addData(key: String, value: R): NetworkResult<T> {
            _data[key] = value
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun <R: Any> getData(key: String): R? {
            return _data[key] as? R
        }

        operator fun <R: Any> get(key: String): R? {
            return getData(key)
        }
    }

    fun getOrNull(): T? {
        return when(this) {
            is Success -> data
            is Failure -> null
        }
    }

    fun getOrThrow(
        exception: Exception = IllegalArgumentException("Result is Failure")
    ): T {
        Result
        return when(this) {
            is Success -> data
            is Failure -> throw exception
        }
    }

    fun getOrDefault(defaultValue: T): T {
        return when(this) {
            is Success -> data
            is Failure -> defaultValue
        }
    }

    fun getOrElse(onError: (exception: Throwable) -> T) : T {
        return when(this) {
            is Success -> data
            is Failure -> onError(error)
        }
    }

    inline fun <reified R: Any> map(transform: (T) -> R): NetworkResult<R> {
        return when(this) {
            is Failure -> Failure(error)
            is Success -> Success(transform(data))
        }
    }

    companion object {
        fun <T: Any> success(data: T): NetworkResult<T> {
            return Success(data)
        }
        fun <T: Any> failure(error: Throwable): NetworkResult<T> {
            return Failure(error)
        }
    }
}