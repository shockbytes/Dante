package at.shockbytes.dante.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import at.shockbytes.dante.ui.fragment.onboarding.OnboardingLoginFragment

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return OnboardingLoginFragment.newInstance()
    }
}