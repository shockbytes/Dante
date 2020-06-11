package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.OnBookActionClickedListener
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.fragment_book_action_sheet.*

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2020
 */
class BookActionBottomSheetFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.fragment_book_action_sheet

    private lateinit var listener: OnBookActionClickedListener

    private var book: BookEntity by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = (parentFragment as? OnBookActionClickedListener)
            ?: throw IllegalStateException("$parentFragment does not implement OnBookActionClickedListener interface")
    }

    override fun setupViews() {
        hideSelectedStateItem(book.state)
        setupClickListeners()
    }

    private fun hideSelectedStateItem(state: BookState) {
        when (state) {
            BookState.READ_LATER -> btn_book_action_move_to_upcoming
            BookState.READING -> btn_book_action_move_to_current
            BookState.READ -> btn_book_action_move_to_done
        }.setVisible(false)
    }

    private fun setupClickListeners() {
        btn_book_action_move_to_upcoming.setOnClickListener {
            listener.onMoveToUpcoming(book)
            dismiss()
        }
        btn_book_action_move_to_current.setOnClickListener {
            listener.onMoveToCurrent(book)
            dismiss()
        }
        btn_book_action_move_to_done.setOnClickListener {
            listener.onMoveToDone(book)
            dismiss()
        }
        btn_book_action_share.setOnClickListener {
            listener.onShare(book)
            dismiss()
        }
        btn_book_action_edit.setOnClickListener {
            listener.onEdit(book)
            dismiss()
        }
        btn_book_action_delete.setOnClickListener {
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