package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.databinding.FragmentBookActionSheetBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.OnBookActionClickedListener
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.setVisible

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2020
 */
class BookActionBottomSheetFragment : BaseBottomSheetFragment<FragmentBookActionSheetBinding>() {


    private lateinit var listener: OnBookActionClickedListener

    private var book: BookEntity by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = (parentFragment as? OnBookActionClickedListener)
            ?: throw IllegalStateException("$parentFragment does not implement OnBookActionClickedListener interface")
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBookActionSheetBinding {
        return FragmentBookActionSheetBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        hideSelectedStateItem(book.state)
        setupClickListeners()
    }

    private fun hideSelectedStateItem(state: BookState) {
        when (state) {
            BookState.READ_LATER -> listOf(vb.btnBookActionMoveToUpcoming)
            BookState.READING -> listOf(vb.btnBookActionMoveToCurrent)
            BookState.READ -> listOf(vb.btnBookActionMoveToDone)
            BookState.WISHLIST -> listOf(vb.btnBookActionEdit, vb.btnBookActionSuggest)
        }.forEach { it.setVisible(false) }
    }

    private fun setupClickListeners() {
        vb.btnBookActionMoveToUpcoming.setOnClickListener {
            listener.onMoveToUpcoming(book)
            dismiss()
        }
        vb.btnBookActionMoveToCurrent.setOnClickListener {
            listener.onMoveToCurrent(book)
            dismiss()
        }
        vb.btnBookActionMoveToDone.setOnClickListener {
            listener.onMoveToDone(book)
            dismiss()
        }
        vb.btnBookActionShare.setOnClickListener {
            listener.onShare(book)
            dismiss()
        }
        vb.btnBookActionEdit.setOnClickListener {
            listener.onEdit(book)
            dismiss()
        }
        vb.btnBookActionSuggest.setOnClickListener {
            listener.onSuggest(book)
            dismiss()
        }
        vb.btnBookActionDelete.setOnClickListener {
            listener.onDelete(book) { gotDeleted ->
                if (gotDeleted) {
                    dismiss()
                }
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
    override fun bindViewModel() = Unit
    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(book: BookEntity): BookActionBottomSheetFragment {
            return BookActionBottomSheetFragment().apply {
                this.book = book
            }
        }
    }
}