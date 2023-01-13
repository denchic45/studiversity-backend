package com.studiversity.util

sealed class OptionalProperty<out T> {

    object NotPresent : OptionalProperty<Nothing>()

    data class Present<T>(val value: T) : OptionalProperty<T>()

    fun ifPresent(onPresent: (value: T) -> Unit) {
        if (this is Present) onPresent(value)
    }
}

fun <T> OptionalProperty<T>.requirePresent(): T {
    return presentOrElse { throw IllegalStateException("Value not present") }
}

fun <T> OptionalProperty<T>.presentOrElse(defaultValue: () -> T) =
    (this as? OptionalProperty.Present)?.value ?: defaultValue()