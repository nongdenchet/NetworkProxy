package com.rain.networkproxy.ui.dashboard;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rain.networkproxy.R;
import com.rain.networkproxy.model.PendingResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

final class DashboardAdapter extends ArrayAdapter<PendingResponse> {
    private final List<PendingResponse> items = new ArrayList<>();
    private final Listener listener;

    DashboardAdapter(@NonNull Context context, @NonNull Listener listener) {
        super(context, -1);
        this.listener = listener;
    }

    interface Listener {
        void onProceed(PendingResponse pendingResponse);
        void onShow(PendingResponse pendingResponse);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Nullable
    @Override
    public PendingResponse getItem(int position) {
        return items.get(position);
    }

    @MainThread
    void submitList(List<PendingResponse> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateView(parent);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final PendingResponse item = getItem(position);
        if (item != null) {
            final Response originResponse = item.getResponse();
            final int code = originResponse.code();
            final String content = originResponse.request().url().url()
                    + "(id=" + item.getId()
                    + ", status=" + code
                    + ")";
            viewHolder.tvTitle.setText(content);
            convertView.setBackgroundColor(getContext().getResources().getColor(getBackgroundColor(code)));
        }

        return convertView;
    }

    @NonNull
    private View onCreateView(@NonNull ViewGroup viewGroup) {
        final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        return layoutInflater.inflate(R.layout.network_proxy_item_pending, viewGroup, false);
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

    private int getAdapterPosition(View child) {
        final View itemView = (View) child.getParent();
        final ListView listView = (ListView) itemView.getParent();
        if (listView == null) {
            return AdapterView.INVALID_POSITION;
        }

        return listView.getPositionForView(itemView);
    }

    final class ViewHolder {
        final TextView tvTitle;

        ViewHolder(@NonNull final View itemView) {
            tvTitle = itemView.findViewById(R.id.tvTitle);
            itemView.findViewById(R.id.tvProceed).setOnClickListener(v -> {
                final int position = getAdapterPosition(v);

                if (position != AdapterView.INVALID_POSITION) {
                    listener.onProceed(getItem(getAdapterPosition(v)));
                }
            });
            itemView.findViewById(R.id.tvDetail).setOnClickListener(v -> {
                final int position = getAdapterPosition(v);

                if (position != AdapterView.INVALID_POSITION) {
                    listener.onShow(getItem(getAdapterPosition(v)));
                }
            });
        }
    }
}
