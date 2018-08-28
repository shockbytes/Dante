package at.shockbytes.dante.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import at.shockbytes.dante.billing.InAppBillingService
import at.shockbytes.dante.billing.Price
import at.shockbytes.dante.util.SingleLiveEvent
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 28-Aug-18.
 */

class SupporterBadgeViewModel @Inject constructor(
        private val inAppBillingService: InAppBillingService) : BaseViewModel() {

    val standardPrice = MutableLiveData<Price>()
    val premiumPrice = MutableLiveData<Price>()

    val purchaseCompletedEvent = SingleLiveEvent<Unit>()

    init {
        poke()
    }

    override fun poke() {
        compositeDisposable.add(inAppBillingService.retrieveStandardPrice().subscribe { std ->
            standardPrice.postValue(std)
        })
        compositeDisposable.add(inAppBillingService.retrievePremiumPrice().subscribe { premium ->
            premiumPrice.postValue(premium)
        })
    }

    fun purchaseStandardBadge() {
        compositeDisposable.add(inAppBillingService.purchaseStandardBadge().subscribe({
            purchaseCompletedEvent.call()
        }, { throwable ->
            throwable.printStackTrace()
        }))
    }

    fun purchasePremiumBadge() {
        compositeDisposable.add(inAppBillingService.purchasePremiumBadge().subscribe({
            purchaseCompletedEvent.call()
        }, { throwable ->
            throwable.printStackTrace()
        }))
    }

}