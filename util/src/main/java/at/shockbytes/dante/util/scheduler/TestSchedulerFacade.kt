package at.shockbytes.dante.util.scheduler

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Tests should all run in sequence, therefore use trampoline scheduler
 */
class TestSchedulerFacade : SchedulerFacade {

    override val ui: Scheduler
        get() = Schedulers.trampoline()

    override val computation: Scheduler
        get() = Schedulers.trampoline()

    override val io: Scheduler
        get() = Schedulers.trampoline()
}