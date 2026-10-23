package com.diego_ramos.croche_api.common.result

class Result<T> private constructor(
    private val storedValue: T?,
    private val storedError: ResultError?
) {
    val isSuccess: Boolean
        get() = storedError == null

    val isFailure: Boolean
        get() = !isSuccess

    val value: T
        get() {
            check(isSuccess) {
                "Cannot read the value from a failed Result."
            }
            @Suppress("UNCHECKED_CAST")
            return storedValue as T
        }

    val error: ResultError
        get() = storedError
            ?: throw IllegalStateException(
                "Cannot read the error from a successful Result."
            )

    companion object {
        fun <T> success(value: T): Result<T> =
            Result(value, null)

        fun <T> failure(error: ResultError): Result<T> =
            Result(null, error)

        fun <T> failure(
            code: String,
            message: String
        ): Result<T> =
            failure(ResultError(code, message))
    }
}
