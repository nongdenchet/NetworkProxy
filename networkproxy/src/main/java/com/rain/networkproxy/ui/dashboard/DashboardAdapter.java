package com.rain.networkproxy.ui.dashboard;

import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import okhttp3.Response;

import com.rain.networkproxy.R;
import com.rain.networkproxy.model.PendingResponse;

final class DashboardAdapter extends ListAdapter<PendingResponse, DashboardAdapter.ViewHolder> {
    @Nullable
    private ItemListener itemListener;

    DashboardAdapter() {
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
        final Response originResponse = response.getResponse();
        final int code = originResponse.code();
        final String content = originResponse.request().url().url()
                + "(id=" + response.getId()
                + ", status=" + code
                + ")";
        viewHolder.tvTitle.setText(content);
        viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(),
                getBackgroundColor(code)));
    }

    @ColorRes
    private int getBackgroundColor(int code) {
        int color = R.color.network_proxy_green;
        if (code < 400 && code >= 300) {
            color = R.color.network_proxy_blue;
        } else if (code < 500 && code >= 400) {
            color = R.color.network_proxy_yellow;
        } else if (code >= 500) {
            color = R.color.network_proxy_red;
        }
        return color;
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
        final TextView tvTitle;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
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
