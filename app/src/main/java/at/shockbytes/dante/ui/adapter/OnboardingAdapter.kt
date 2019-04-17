package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.onboarding.OnboardingContent
import at.shockbytes.dante.util.DanteUtils
import com.airbnb.lottie.LottieAnimationView
import com.tbuonomo.creativeviewpager.adapter.CreativePagerAdapter

class OnboardingAdapter(private val context: Context) : CreativePagerAdapter {

    override fun getCount(): Int = OnboardingContent.values().size

    override fun instantiateContentItem(inflater: LayoutInflater, container: ViewGroup, position: Int): View {

        // Inflate the header view layout
        val contentRoot = inflater.inflate(R.layout.item_onboarding_header, container, false)

        // Bind the views
        val imageView = contentRoot.findViewById<ImageView>(R.id.itemCreativeImage)

        imageView.setImageDrawable(ContextCompat.getDrawable(context, OnboardingContent.values()[position].headerIcon))
        return contentRoot
    }

    override fun instantiateHeaderItem(inflater: LayoutInflater, container: ViewGroup, position: Int): View {

        val item = OnboardingContent.values()[position]
        val view = when (item) {
            OnboardingContent.WELCOME -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
            OnboardingContent.TRACKING -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
            OnboardingContent.NIGHT_MODE -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
            OnboardingContent.LOGIN -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
            OnboardingContent.SUGGESTIONS -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
            OnboardingContent.CALL_TO_ACTION -> {
                inflater.inflate(R.layout.item_onboarding_content_generic, container, false)
            }
        }

        view.findViewById<LottieAnimationView>(R.id.lottie_onboarding_item).apply {
            setAnimation(item.lottieRes)
        }

        return view
    }

    override fun isUpdatingBackgroundColor(): Boolean {
        return true
    }

    override fun requestBitmapAtPosition(position: Int): Bitmap? {
        return DanteUtils.getBitmap(context, OnboardingContent.values()[position].headerIcon)
    }
}