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
        tvProfileUserName.text = name
        tvProfileMailAddress.text = mailAddress
    }

    fun reset() {
        setUser("", "")
        imageView.setImageResource(0)
    }
}