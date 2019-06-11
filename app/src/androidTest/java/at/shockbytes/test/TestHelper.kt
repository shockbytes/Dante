package at.shockbytes.test

import org.mockito.Mockito

/**
 * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 * https://stackoverflow.com/a/48091649/3111388
 */
fun <T> any(): T = Mockito.any<T>()