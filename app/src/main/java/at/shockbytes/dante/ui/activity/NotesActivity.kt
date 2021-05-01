package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.ActivityNotesBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.NotesBundle
import at.shockbytes.dante.ui.activity.core.BackNavigableActivity
import javax.inject.Inject

class NotesActivity : BackNavigableActivity<ActivityNotesBinding>() {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithBinding(ActivityNotesBinding::inflate)

        intent.extras?.getParcelable<NotesBundle>(ARG_NOTES_BUNDLE)?.let { notesBundle ->
            setupViews(notesBundle)
        }
    }

    private fun setupViews(notesBundle: NotesBundle) {
        supportActionBar?.elevation = 0f

        vb.etNotes.setText(notesBundle.notes)
        vb.txtNotesHeaderDescription.text = getString(R.string.dialogfragment_notes_header, notesBundle.title)

        notesBundle.thumbnailUrl?.let { bookImageLink ->
            imageLoader.loadImageWithCornerRadius(
                this,
                bookImageLink,
                vb.ivNotesCover,
                R.drawable.ic_placeholder_white,
                cornerDimension = resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        }

        vb.btnNotesSave.setOnClickListener {
            LocalBroadcastManager.getInstance(this).sendBroadcastSync(buildNotesIntent())
            supportFinishAfterTransition()
        }
        vb.btnNotesReset.setOnClickListener {
            vb.etNotes.setText("")
        }
    }

    private fun buildNotesIntent(): Intent {
        return Intent(ACTION_NOTES)
            .putExtra(NOTES_EXTRA, vb.etNotes.text.toString())
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        const val ACTION_NOTES = "write_notes"
        const val NOTES_EXTRA = "notes"

        private const val ARG_NOTES_BUNDLE = "arg_notes_bundle"

        fun newIntent(context: Context, notesBundle: NotesBundle): Intent {
            return Intent(context, NotesActivity::class.java)
                .putExtra(ARG_NOTES_BUNDLE, notesBundle)
        }
    }
}