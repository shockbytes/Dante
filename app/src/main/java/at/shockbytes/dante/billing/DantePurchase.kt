package at.shockbytes.dante.billing

/**
 * @author  Martin Macheiner
 * Date:    28.08.2018
 */

sealed class DantePurchase {
    class NoPurchase(): DantePurchase()
    class StandardPurchase(): DantePurchase()
    class PremiumPurchase(): DantePurchase()
}