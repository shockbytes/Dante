package at.shockbytes.dante.billing

/**
 * @author  Martin Macheiner
 * Date:    28.08.2018
 */

sealed class DantePurchase {
    object NoPurchase : DantePurchase()
    object StandardPurchase : DantePurchase()
    object PremiumPurchase : DantePurchase()
}