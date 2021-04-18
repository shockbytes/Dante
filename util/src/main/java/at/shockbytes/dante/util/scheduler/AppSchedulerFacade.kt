package at.shockbytes.dante.util.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

class AppSchedulerFacade : SchedulerFacade {

    override val ui: Scheduler
        get() = AndroidSchedulers.mainThread()

    override val computation: Scheduler
        get() = Schedulers.computation()

    override val io: Scheduler
        get() = Schedulers.io()
}