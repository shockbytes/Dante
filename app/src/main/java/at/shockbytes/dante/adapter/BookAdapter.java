package at.shockbytes.dante.adapter;

import android.content.Context;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.books.Book;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 06.01.2016.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    public interface OnItemClickListener {

        void onItemClick(Book t, View v);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(Book t, View v);
    }

    public interface OnBookPopupItemSelectedListener {

        void onDelete(Book b);

        void onShare(Book b);

        void onMoveToUpcoming(Book b);

        void onMoveToCurrent(Book b);

        void onMoveToDone(Book b);
    }


    private ArrayList<Book> data;
    private Context context;
    private final LayoutInflater inflater;
    private Book.State state;

    private boolean showOverflow;

    private OnBookPopupItemSelectedListener onBookPopupItemSelectedListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    //----------------------------------------------------------------------

    public BookAdapter(Context context, List<Book> data, Book.State state,
                       OnBookPopupItemSelectedListener onBookPopupItemSelectedListener,
                       boolean showOverflow) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.state = state;
        this.showOverflow = showOverflow;
        this.onBookPopupItemSelectedListener = onBookPopupItemSelectedListener;

        this.data = new ArrayList<>();
        setData(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.listitem_book, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }

    //-----------------------------Data Section-----------------------------
    public void addEntity(int i, Book entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void addEntityAtLast(Book entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(Book entity) {
        addEntity(0, entity);
    }

    public void deleteEntity(Book book) {
        int location = getLocation(data, book);
        if (location > -1) {
            deleteEntity(location);
        }
    }

    public void deleteEntity(int i) {
        data.remove(i);
        notifyItemRemoved(i);
    }

    public void moveEntity(int i, int dest) {
        Book temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
    }

    public void setData(List<Book> data) {

        if (data == null) {
            return;
        }

        //Remove all deleted items
        for (int i = this.data.size() - 1; i >= 0; --i) {
            //Remove all deleted items
            if (getLocation(data, this.data.get(i)) < 0) {
                deleteEntity(i);
            }
        }

        //Add and move items
        for (int i = 0; i < data.size(); ++i) {
            Book entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
    }

    public int getLocation(Book searching) {
        return getLocation(data, searching);
    }

    private int getLocation(List<Book> data, Book searching) {

        for (int j = 0; j < data.size(); ++j) {
            Book newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    //----------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {

        private Book book;

        @BindView(R.id.listitem_book_txt_title)
        protected TextView txtTitle;

        @BindView(R.id.listitem_book_txt_subtitle)
        protected TextView txtSubTitle;

        @BindView(R.id.listitem_book_txt_author)
        protected TextView txtAuthor;

        @BindView(R.id.listitem_book_img_thumb)
        protected ImageView imgViewThumb;

        @BindView(R.id.listitem_book_img_overflow)
        protected ImageButton imgBtnOverflow;

        private PopupMenu popupMenu;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(book, itemView);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(book, itemView);
                    }
                    return true;
                }
            });

            int visibilityOverflow = showOverflow ? View.VISIBLE : View.GONE;
            imgBtnOverflow.setVisibility(visibilityOverflow);

            popupMenu = new PopupMenu(context, imgBtnOverflow);
            popupMenu.getMenuInflater().inflate(R.menu.popup_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this);
            tryShowIconsInPopupMenu(popupMenu);
            hideSelectedPopupItem();
        }

        private void hideSelectedPopupItem() {

            MenuItem item = null;
            switch (state) {

                case READ_LATER:

                    item = popupMenu.getMenu().findItem(R.id.popup_item_move_to_upcoming);
                    break;

                case READING:

                    item = popupMenu.getMenu().findItem(R.id.popup_item_move_to_current);
                    break;

                case READ:

                    item = popupMenu.getMenu().findItem(R.id.popup_item_move_to_done);
                    break;
            }
            item.setVisible(false);
        }

        public void bind(Book b) {

            book = b;

            txtTitle.setText(book.getTitle());
            txtAuthor.setText(book.getAuthor());
            txtSubTitle.setText(book.getSubTitle());

            String thumbnailAddress = book.getThumbnailAddress();
            if (thumbnailAddress != null && !thumbnailAddress.isEmpty()) {
                Picasso.with(context).load(thumbnailAddress)
                        .placeholder(R.drawable.ic_placeholder).into(imgViewThumb);
            }
        }

        @OnClick(R.id.listitem_book_img_overflow)
        public void onClickOverflow() {
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (onBookPopupItemSelectedListener == null) {
                return false;
            }

            // Do not delete book from adapter when user just wants to share it!
            if (item.getItemId() != R.id.popup_item_share) {
                deleteEntity(book);
            }

            switch (item.getItemId()) {

                case R.id.popup_item_move_to_upcoming:

                    onBookPopupItemSelectedListener.onMoveToUpcoming(book);
                    break;

                case R.id.popup_item_move_to_current:

                    onBookPopupItemSelectedListener.onMoveToCurrent(book);
                    break;

                case R.id.popup_item_move_to_done:

                    onBookPopupItemSelectedListener.onMoveToDone(book);
                    break;

                case R.id.popup_item_share:

                    onBookPopupItemSelectedListener.onShare(book);
                    break;

                case R.id.popup_item_delete:

                    onBookPopupItemSelectedListener.onDelete(book);
                    break;
            }
            return true;
        }
    }

    private void tryShowIconsInPopupMenu(PopupMenu menu) {

        try {
            Field fieldPopup = menu.getClass().getDeclaredField("mPopup");
            fieldPopup.setAccessible(true);
            MenuPopupHelper popup = (MenuPopupHelper) fieldPopup.get(menu);
            popup.setForceShowIcon(true);
        } catch (Exception e) {
            Log.d("Dante", "Cannot force to show icons in popupmenu");
        }
    }

}