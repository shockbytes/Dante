package at.shockbytes.tracking.properties

abstract class BaseProperty<out T>(val value: T) {
    abstract fun getKey(): String
}