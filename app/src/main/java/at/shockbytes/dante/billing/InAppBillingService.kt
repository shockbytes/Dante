package at.shockbytes.dante.billing

import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 28-Aug-18.
 */
interface InAppBillingService {

    fun retrieveStandardPrice(): Single<Price>

    fun retrievePremiumPrice(): Single<Price>

    fun purchaseStandardBadge(): Completable

    fun purchasePremiumBadge(): Completable

    fun getPurchase(): Single<DantePurchase>

}