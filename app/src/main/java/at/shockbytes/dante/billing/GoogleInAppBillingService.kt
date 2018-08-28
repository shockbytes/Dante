package at.shockbytes.dante.billing

import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author  Martin Macheiner
 * Date:    28.08.2018
 */
class GoogleInAppBillingService : InAppBillingService {

    // TODO Remove later
    private var purchase: DantePurchase = DantePurchase.NoPurchase()

    override fun retrieveStandardPrice(): Single<Price> {
        return Single.fromCallable {
            // TODO Do not use fake data
            Price(0.99, "EUR")
        }
    }

    override fun retrievePremiumPrice(): Single<Price> {
        return Single.fromCallable {
            // TODO Do not use fake data
            Price(2.99, "EUR")
        }
    }

    override fun purchaseStandardBadge(): Completable {
        return Completable.fromCallable {
            // TODO Purchase standard badge
            purchase = DantePurchase.StandardPurchase()
            Unit
        }
    }

    override fun purchasePremiumBadge(): Completable {
        return Completable.fromCallable {
            // TODO Purchase premium badge
            purchase = DantePurchase.PremiumPurchase()
            Unit
        }
    }

    override fun getPurchase(): Single<DantePurchase> {
        // TODO Read from billing api
        return Single.just(purchase)
    }

}