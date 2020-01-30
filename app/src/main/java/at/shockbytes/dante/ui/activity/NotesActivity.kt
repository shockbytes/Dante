package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.NotesBundle
import at.shockbytes.dante.ui.activity.core.BackNavigableActivity
import kotlinx.android.synthetic.main.activity_notes.*
import javax.inject.Inject

class NotesActivity: BackNavigableActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        intent.extras?.getParcelable<NotesBundle>(ARG_NOTES_BUNDLE)?.let { notesBundle ->
            setupViews(notesBundle)
        }
    }

    private fun setupViews(notesBundle: NotesBundle) {
        supportActionBar?.elevation = 0f


        et_notes.setText(notesBundle.notes)
        txt_notes_header_description.text = getString(R.string.dialogfragment_notes_header, notesBundle.title)

        notesBundle.thumbnailUrl?.let { bookImageLink ->
            imageLoader.loadImageWithCornerRadius(
                this,
                bookImageLink,
                iv_notes_cover,
                R.drawable.ic_placeholder_white,
                cornerDimension = resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        }

        btn_notes_save.setOnClickListener {
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(buildNotesIntent())
            supportFinishAfterTransition()
        }
        btn_notes_reset.setOnClickListener {
            et_notes.setText("")
        }
    }

    private fun buildNotesIntent(): Intent {
        return Intent("NOTES")
            .putExtra("notes", et_notes.text.toString())
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }


    companion object {

        private const val ARG_NOTES_BUNDLE = "arg_notes_bundle"

        fun newIntent(context: Context, notesBundle: NotesBundle): Intent {
            return Intent(context, NotesActivity::class.java)
                .putExtra(ARG_NOTES_BUNDLE, notesBundle)
        }
    }
}