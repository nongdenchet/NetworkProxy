package com.rain.networkproxy.ui.filter;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.rain.networkproxy.R;

final class FilterAdapter extends ListAdapter<FilterItemViewModel, FilterAdapter.ViewHolder> {
    private final Listener listener;

    FilterAdapter(@NonNull Listener listener) {
        super(callback);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FilterItemViewModel> callback = new DiffUtil.ItemCallback<FilterItemViewModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull FilterItemViewModel oldItem, @NonNull FilterItemViewModel newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FilterItemViewModel oldItem, @NonNull FilterItemViewModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View itemView = layoutInflater.inflate(R.layout.network_proxy_item_filter, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final FilterItemViewModel item = getItem(position);
        viewHolder.ivClose.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        viewHolder.cbActive.setChecked(item.isActive());
        viewHolder.cbActive.setText(item.getName());
    }

    interface Listener {
        void post(FilterAction action);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbActive;
        final ImageView ivClose;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivClose = itemView.findViewById(R.id.ivClose);
            ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.post(new FilterAction.Remove(getAdapterPosition()));
                    }
                }
            });
            cbActive = itemView.findViewById(R.id.cbActive);
            cbActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        final int position = getAdapterPosition();
                        final FilterItemViewModel item = getItem(position);
                        if (isChecked != item.isActive()) {
                            listener.post(new FilterAction.Update(position, isChecked));
                        }
                    }
                }
            });
        }
    }
}
