package at.shockbytes.dante.ui.custom.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.ProfileActionViewBinding
import at.shockbytes.dante.util.setVisible
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ProfileActionView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val vb = ProfileActionViewBinding.inflate(LayoutInflater.from(context), this, false)

    private val clickSubject = PublishSubject.create<ProfileActionViewClick>()
    fun onActionButtonClicked(): Observable<ProfileActionViewClick> = clickSubject

    init {
        initializeClickListeners()
        setState(ProfileActionViewState.Hidden)
    }

    private fun initializeClickListeners() {
        vb.btnProfileActionViewUpgrade.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.UPGRADE_ANONYMOUS_ACCOUNT)
        }
        vb.btnProfileActionViewChangeName.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.CHANGE_NAME)
        }
        vb.btnProfileActionViewChangeImage.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.CHANGE_IMAGE)
        }
        vb.btnProfileActionViewChangePassword.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.CHANGE_PASSWORD)
        }
    }

    fun setState(state: ProfileActionViewState) {
        when (state) {
            ProfileActionViewState.Hidden -> {
                setVisible(false)
            }
            is ProfileActionViewState.Visible -> {
                setVisible(true)
                vb.btnProfileActionViewUpgrade.setVisible(state.showUpgrade)
                vb.btnProfileActionViewChangeName.setVisible(state.showChangeName)
                vb.btnProfileActionViewChangeImage.setVisible(state.showChangeImage)
                vb.btnProfileActionViewChangePassword.setVisible(state.showChangePassword)
            }
        }
    }
}