package at.shockbytes.dante.util.scheduler

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppSchedulerFacade : SchedulerFacade {

    override val ui: Scheduler
        get() = AndroidSchedulers.mainThread()

    override val computation: Scheduler
        get() = Schedulers.computation()

    override val io: Scheduler
        get() = Schedulers.io()
}