package at.shockbytes.dante.core.injection

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.warehouse.Warehouse
import at.shockbytes.warehouse.WarehouseConfiguration
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.box.memory.InMemoryBoxEngine
import at.shockbytes.warehouse.ledger.Ledger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class WarehouseModule {

    @Provides
    @Singleton
    fun provideBookWarehouse(): Warehouse<BookEntity> {
        return Warehouse.new(
            boxes = listOf(
                Box.defaultFrom(InMemoryBoxEngine.default { it.isbn })
            ),
            ledger = Ledger.inMemory(),
            config = WarehouseConfiguration(
                leaderBoxId = BoxId.of("memory")
            )
        )
    }

    @Provides
    @Singleton
    fun provideBookLabelWarehouse(): Warehouse<BookLabel> {
        return Warehouse.new(
            boxes = listOf(
                Box.defaultFrom(InMemoryBoxEngine.default { it.title })
            ),
            ledger = Ledger.inMemory(),
            config = WarehouseConfiguration(
                leaderBoxId = BoxId.of("memory")
            )
        )
    }
}