package at.shockbytes.dante.util.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Martin Macheiner
 *         Date: 15.12.2015.
 */
public class EnhancedRecyclerView extends RecyclerView{

    private View mEmptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {

            Adapter<?> adapter = getAdapter();
            if(adapter != null && mEmptyView != null){

                //Show empty view
                if(adapter.getItemCount() == 0){
                    mEmptyView.animate().alpha(1).start();
                    setNestedScrollingEnabled(false);
                }
                //Hide empty view
                else if(adapter.getItemCount() != 0){
                    mEmptyView.animate().alpha(0).start();
                    setNestedScrollingEnabled(true);
                }
            }
        }
    };

    public EnhancedRecyclerView(Context context) {
        super(context);
    }

    public EnhancedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhancedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter != null){
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView){
        mEmptyView = emptyView;
    }


}
