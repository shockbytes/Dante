package at.shockbytes.dante.ui.custom.profile

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ProfileHeaderViewBinding
import at.shockbytes.dante.util.layoutInflater

class ProfileHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val vb: ProfileHeaderViewBinding
        get() = ProfileHeaderViewBinding.inflate(context.layoutInflater(), this, true)

    val imageView: ImageView
        get() = vb.ivProfileUser

    fun setUser(name: String?, mailAddress: String?) {
        val userName = if (name.isNullOrEmpty()) context.getString(R.string.anonymous_user) else name
        vb.tvProfileUserName.text = userName
        vb.tvProfileMailAddress.text = mailAddress
    }

    fun reset() {
        setUser("", "")
        imageView.setImageResource(0)
    }
}