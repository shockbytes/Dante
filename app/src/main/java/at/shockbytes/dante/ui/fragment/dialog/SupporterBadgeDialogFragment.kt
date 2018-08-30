package at.shockbytes.dante.ui.fragment.dialog

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.viewmodel.SupporterBadgeViewModel
import at.shockbytes.dante.util.roundDouble
import at.shockbytes.dante.util.tracking.Tracker
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    28.08.2018
 */
class SupporterBadgeDialogFragment : InteractiveViewDialogFragment<Unit>() {

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_support, null, false)

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    protected lateinit var tracker: Tracker

    private lateinit var viewModel: SupporterBadgeViewModel

    private val imgViewDeveloper: ImageView by bindView(R.id.supporterBadgeImageViewDeveloper)
    private val btnStandardPrice: Button by bindView(R.id.supporterBadgeStandardButton)
    private val btnPremiumPrice: Button by bindView(R.id.supporterBadgePremiumButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[SupporterBadgeViewModel::class.java]

        tracker.trackOnClickSupporterBadgePage()
    }

    override fun setupViews() {

        context?.let { ctx ->
            Glide.with(ctx)
                    .load(R.drawable.ic_developer)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.ic_user_template_dark))
                    .into(imgViewDeveloper)
        }

        btnStandardPrice.setOnClickListener {
            tracker.trackBuySupporterBadge("standard")
            viewModel.purchaseStandardBadge()
        }

        btnPremiumPrice.setOnClickListener {
            tracker.trackBuySupporterBadge("premium")
            viewModel.purchasePremiumBadge()
        }

        // ----------------------- Setup observer -----------------------

        viewModel.standardPrice.observe(this, Observer { stdPrice ->
            btnStandardPrice.text = getString(R.string.in_app_price_standard,
                    stdPrice?.amount?.roundDouble(2)?.toString(), stdPrice?.currency)
            btnStandardPrice.isEnabled = true
        })
        viewModel.premiumPrice.observe(this, Observer { premiumPrice ->
            btnPremiumPrice.text = getString(R.string.in_app_price_premium,
                    premiumPrice?.amount?.roundDouble(2)?.toString(), premiumPrice?.currency)
            btnPremiumPrice.isEnabled = true
        })

        viewModel.purchaseCompletedEvent.observe(this, Observer {
            Toast.makeText(context!!, R.string.in_app_purchase_thanks, Toast.LENGTH_LONG).show()
            dismiss()
        })
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        fun newInstance(): SupporterBadgeDialogFragment {
            val fragment = SupporterBadgeDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }

}