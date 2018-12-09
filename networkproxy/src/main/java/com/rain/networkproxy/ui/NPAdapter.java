package com.rain.networkproxy.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rain.networkproxy.R;
import com.rain.networkproxy.model.PendingResponse;

final class NPAdapter extends ListAdapter<PendingResponse, NPAdapter.ViewHolder> {
    @Nullable
    private ItemListener itemListener;

    NPAdapter() {
        super(callback);
    }

    interface ItemListener {
        void onProceed(PendingResponse pendingResponse);
    }

    void setItemListener(@Nullable ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View itemView = layoutInflater.inflate(R.layout.network_proxy_item_pending, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        final PendingResponse response = getItem(pos);
        viewHolder.tvId.setText(response.getId());
        viewHolder.tvTitle.setText(response.getResponse().request().url().encodedPath());
    }

    private static final DiffUtil.ItemCallback<PendingResponse> callback = new DiffUtil.ItemCallback<PendingResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull PendingResponse oldItem, @NonNull PendingResponse newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PendingResponse oldItem, @NonNull PendingResponse newItem) {
            return oldItem.equals(newItem);
        }
    };

    final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId;
        final TextView tvTitle;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            itemView.findViewById(R.id.tvProceed).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemListener != null) {
                        itemListener.onProceed(getItem(getAdapterPosition()));
                    }
                }
            });
        }
    }
}