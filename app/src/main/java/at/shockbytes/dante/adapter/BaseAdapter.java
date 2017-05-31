package at.shockbytes.dante.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * @author Martin Macheiner
 *         Date: 05.03.2017.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {

    public interface OnItemClickListener<T> {

        void onItemClick(T t, View v);
    }

    public interface OnItemLongClickListener<T> {

        void onItemLongClick(T t, View v);
    }

    public interface OnItemMoveListener<T> {

        void onItemMove(T t, int from, int to);

        void onItemMoveFinished();

        void onItemDismissed(T t, int position);
    }

    protected List<T> data;
    protected LayoutInflater inflater;
    protected Context context;

    protected OnItemMoveListener<T> onItemMoveListener;
    protected OnItemClickListener<T> onItemClickListener;
    protected OnItemLongClickListener<T> onItemLongClickListener;

    //----------------------------------------------------------------------

    public BaseAdapter(Context cxt, List<T> data) {

        context = cxt;
        inflater = LayoutInflater.from(cxt);
        this.data = new ArrayList<>();

        setData(data);
    }

    @Override
    public void onBindViewHolder(BaseAdapter.ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        onItemLongClickListener = listener;
    }

    public void setOnItemMoveListener(OnItemMoveListener<T> listener) {
        onItemMoveListener = listener;
    }

    //-----------------------------Data Section-----------------------------
    public void addEntity(int i, T entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void deleteEntity(T entity) {
        int location = getLocation(data, entity);
        if (location >= 0) {
            deleteEntity(location);
        }
    }

    public void deleteEntity(int i) {
        data.remove(i);
        notifyItemRemoved(i);
    }

    public void addEntityAtLast(T entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(T entity) {
        addEntity(0, entity);
    }

    public void updateEntity(T entity) {
        int location = getLocation(data, entity);
        if (location >= 0) {
            data.set(location, entity);
            notifyItemChanged(location);
        }
    }

    public void replace(T changed, int arrayIdx) {
        data.set(arrayIdx, changed);
        notifyItemChanged(arrayIdx);
    }

    public void moveEntity(int i, int dest) {
        T temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {

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
            T entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
        notifyDataSetChanged();
    }

    protected int getLocation(List<T> data, T searching) {

        for (int j = 0; j < data.size(); ++j) {
            T newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }
        return -1;
    }
    //----------------------------------------------------------------------

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        protected T content;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(content, itemView);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(content, itemView);
                    }
                    return true;
                }
            });

        }

        public abstract void bind(T t);

    }

}
