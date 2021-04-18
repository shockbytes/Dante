package at.shockbytes.dante.ui.custom.profile

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.util.setVisible
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.profile_action_view.view.*

class ProfileActionView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val clickSubject = PublishSubject.create<ProfileActionViewClick>()
    fun onActionButtonClicked(): Observable<ProfileActionViewClick> = clickSubject

    init {
        inflate(context, R.layout.profile_action_view, this)
        initializeClickListeners()
        setState(ProfileActionViewState.Hidden)
    }

    private fun initializeClickListeners() {
        btnProfileActionViewUpgrade.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.UPGRADE_ANONYMOUS_ACCOUNT)
        }
        btnProfileActionViewChangeName.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.CHANGE_NAME)
        }
        btnProfileActionViewChangeImage.setOnClickListener {
            clickSubject.onNext(ProfileActionViewClick.CHANGE_IMAGE)
        }
        btnProfileActionViewChangePassword.setOnClickListener {
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
                btnProfileActionViewUpgrade.setVisible(state.showUpgrade)
                btnProfileActionViewChangeName.setVisible(state.showChangeName)
                btnProfileActionViewChangeImage.setVisible(state.showChangeImage)
                btnProfileActionViewChangePassword.setVisible(state.showChangePassword)
            }
        }
    }
}