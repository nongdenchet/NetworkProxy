package com.rain.networkproxy.ui.filter;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.rain.networkproxy.R;

import java.util.ArrayList;
import java.util.List;

final class FilterAdapter extends ArrayAdapter<FilterItemViewModel> {
    private final List<FilterItemViewModel> items = new ArrayList<>();
    private final Listener listener;

    FilterAdapter(@NonNull Context context, @NonNull Listener listener) {
        super(context, -1);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Nullable
    @Override
    public FilterItemViewModel getItem(int position) {
        return items.get(position);
    }

    @MainThread
    void submitList(List<FilterItemViewModel> newItems) {
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

        final FilterItemViewModel item = getItem(position);
        if (item != null) {
            viewHolder.ivClose.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            viewHolder.cbActive.setChecked(item.isActive());
            viewHolder.cbActive.jumpDrawablesToCurrentState();
            viewHolder.cbActive.setText(item.getName());
        }

        return convertView;
    }

    @NonNull
    private View onCreateView(@NonNull ViewGroup viewGroup) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        return layoutInflater.inflate(R.layout.network_proxy_item_filter, viewGroup, false);
    }

    interface Listener {
        void post(FilterAction action);
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
        final CheckBox cbActive;
        final ImageView ivClose;

        ViewHolder(@NonNull View itemView) {
            ivClose = itemView.findViewById(R.id.ivClose);
            ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition(v);

                    if (position != AdapterView.INVALID_POSITION) {
                        listener.post(new FilterAction.Remove(getAdapterPosition(v)));
                    }
                }
            });
            cbActive = itemView.findViewById(R.id.cbActive);
            cbActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final int position = getAdapterPosition(buttonView);

                    if (position != AdapterView.INVALID_POSITION) {
                        final FilterItemViewModel item = getItem(position);
                        if (item != null && isChecked != item.isActive()) {
                            listener.post(new FilterAction.Update(position, isChecked));
                        }
                    }
                }
            });
        }
    }
}
