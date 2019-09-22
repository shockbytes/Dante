package at.shockbytes.dante.core.data

/**
 * Author:  Martin Macheiner
 * Date:    12.06.2018
 */
abstract class Mapper<In, Out> {

    abstract fun mapTo(data: In): Out

    abstract fun mapFrom(data: Out): In

    fun mapTo(data: List<In>): List<Out> {
        return data.map { mapTo(it) }
    }

    fun mapFrom(data: List<Out>): List<In> {
        return data.map { mapFrom(it) }
    }
}