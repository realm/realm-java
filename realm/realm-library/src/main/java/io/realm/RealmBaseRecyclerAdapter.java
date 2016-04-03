package io.realm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

/**
 * The RealmBaseRecyclerAdapter class is an abstract utility class for binding RecyclerView UI elements to Realm data.
 * <p>
 * This adapter will automatically handle any updates to its data and call {@link #notifyDataSetChanged()} as
 * appropriate.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the {@link OrderedRealmCollection} is
 * closed.
 *
 * @param <T> type of {@link RealmObject} stored in the adapter.
 * @param <VH> type of {@link RecyclerView.ViewHolder} used in the adapter.
 */
public abstract class RealmBaseRecyclerAdapter<T extends RealmObject, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected LayoutInflater inflater;
    protected Context context;
    private OrderedRealmCollection<T> adapterData;
    private RealmChangeListener listener;

    private boolean hasAutoUpdates;

    public RealmBaseRecyclerAdapter(Context context, OrderedRealmCollection<T> data, boolean autoUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context;
        this.adapterData = data;
        this.inflater = LayoutInflater.from(context);
        this.hasAutoUpdates = autoUpdate;
        if (autoUpdate) {
           listener = new RealmChangeListener() {
               @Override
               public void onChange() {
                   notifyDataSetChanged();
               }
           };
        }
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            addListener(adapterData);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            removeListener(adapterData);
        }
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
     * same after {@link #notifyDataSetChanged()} or {@link #updateData(OrderedRealmCollection)} has been called.
     *
     * @param index position of item in the adapter.
     * @return current item ID.
     */
    @Override
    public long getItemId(final int index) {
        return index;
    }

    @Override
    public int getItemCount() {
        return isDataValid() ? adapterData.size() : 0;
    }

    /**
     * Returns the item associated with the specified position.
     *
     * @param index index of the item.
     * @return the item at the specified position.
     */
    public T getItem(int index) {
        return isDataValid() ? adapterData.get(index) : null;
    }

    /**
     * Removes the item associated with the specified position. This also deletes the objects from the underlying Realm.
     *
     * @param index index of the item to remove.
     */
    public void removeItem(int index) {
        if (isDataValid()) {
            getRealm().beginTransaction();
            adapterData.deleteFromRealm(index);
            getRealm().commitTransaction();

            // There is no need to call notify methods if we have listener
            if (!hasAutoUpdates) {
                notifyItemRemoved(index);
            }
        }
    }

    /**
     * Clears all data from the adapter. This also deletes the objects from the underlying Realm.
     */
    public void clear() {
        if (isDataValid()) {
            getRealm().beginTransaction();
            int dataSize = adapterData.size();
            for (int i = dataSize - 1; i >= 0; i--) {
                adapterData.deleteFromRealm(i);
            }
            getRealm().commitTransaction();

            // There is no need to call notify methods if we have listener
            if (!hasAutoUpdates) {
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Returns data associated with this adapter.
     *
     * @return adapter data.
     */
    public OrderedRealmCollection<T> getData() {
        return adapterData;
    }

    /**
     * Updates the data associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    public void updateData(OrderedRealmCollection<T> data) {
        if (hasAutoUpdates) {
            if (adapterData != null) {
                removeListener(adapterData);
            }
            if (data != null) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.isValid();
    }

    private BaseRealm getRealm() {
        if (adapterData instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) adapterData;
            return realmResults.realm;
        } else if (adapterData instanceof RealmList) {
            RealmList realmList = (RealmList) adapterData;
            return realmList.realm;
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + adapterData.getClass());
        }
    }

    private void addListener(OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.removeWeakChangeListener(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.removeWeakChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }
}
