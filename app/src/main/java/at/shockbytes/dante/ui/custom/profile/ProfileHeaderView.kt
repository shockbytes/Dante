package at.shockbytes.dante.ui.custom.profile

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import at.shockbytes.dante.R
import kotlinx.android.synthetic.main.profile_header_view.view.*

class ProfileHeaderView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.profile_header_view, this)
    }

    val imageView: ImageView
        get() = ivProfileUser

    fun setUser(name: String?, mailAddress: String?) {
        val userName = if (name.isNullOrEmpty()) context.getString(R.string.anonymous_user) else name
        tvProfileUserName.text = userName
        tvProfileMailAddress.text = mailAddress
    }

    fun reset() {
        setUser("", "")
        imageView.setImageResource(0)
    }
}