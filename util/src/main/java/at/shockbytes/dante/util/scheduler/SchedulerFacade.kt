package at.shockbytes.dante.util.scheduler

import io.reactivex.rxjava3.core.Scheduler

interface SchedulerFacade {

    val ui: Scheduler

    val computation: Scheduler

    val io: Scheduler
}