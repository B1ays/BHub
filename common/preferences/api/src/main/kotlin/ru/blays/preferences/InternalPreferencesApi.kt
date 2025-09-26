package ru.blays.preferences

@RequiresOptIn(
    message = "This api should not be used directly and is intended for internal implementation",
    level = RequiresOptIn.Level.ERROR
)
@Retention(value = AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class InternalPreferencesApi
