package com.mrkrob.news.domain.model

sealed class NewsException(message: String?, cause: Throwable? = null) : Exception(message, cause) {

    class NoConnection(cause: Throwable) : NewsException(cause.message, cause)

    class InvalidApiKey(message: String?) : NewsException(message)

    class RateLimited(message: String?) : NewsException(message)

    class Api(message: String?) : NewsException(message)
}
