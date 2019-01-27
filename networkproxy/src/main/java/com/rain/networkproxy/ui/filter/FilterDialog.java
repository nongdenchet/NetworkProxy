package com.rain.networkproxy.ui.filter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.rain.networkproxy.InstanceProvider;
import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.ui.Utils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public final class FilterDialog {
    private final Context context;
    private final FilterDialogViewModel viewModel;
    private final FilterAdapter adapter;

    @Nullable
    private AlertDialog dialog;
    @Nullable
    private Disposable disposable;

    public FilterDialog(@NonNull Context context) {
        this.context = context;
        this.viewModel = new FilterDialogViewModel(
                InstanceProvider.instance().provideFilterStorage(context),
                InstanceProvider.instance().provideResourceProvider(context)
        );
        this.adapter = new FilterAdapter(context, new FilterAdapter.Listener() {
            @Override
            public void post(FilterAction action) {
                viewModel.post(action);
            }
        });
    }

    public void show() {
        dispose();
        if (dialog == null) {
            dialog = createDialog();
        }
        bindViewModel();
        dialog.show();
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private AlertDialog createDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(createView())
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dispose();
                    }
                })
                .create();
        dialog.getWindow().setType(Utils.getOverlayType());
        return dialog;
    }

    private void bindViewModel() {
        viewModel.initialize();
        disposable = viewModel.observeItems()
                .subscribe(new Consumer<List<FilterItemViewModel>>() {
                    @Override
                    public void accept(List<FilterItemViewModel> items) {
                        adapter.submitList(items);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("FilterDialog#observeItems", throwable);
                    }
                });
    }

    @NonNull
    @SuppressLint("InflateParams")
    private View createView() {
        final View view = LayoutInflater.from(context).inflate(R.layout.network_proxy_dialog_filter, null);

        final ListView lvFilter = view.findViewById(R.id.lvFilter);
        lvFilter.setAdapter(adapter);

        final EditText edtRule = view.findViewById(R.id.edtRule);
        final View btnSubmit = view.findViewById(R.id.btnSubmit);
        edtRule.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                final Resources resources = context.getResources();
                if (s.toString().isEmpty()) {
                    btnSubmit.setBackgroundColor(resources.getColor(R.color.network_proxy_grey));
                    btnSubmit.setEnabled(false);
                } else {
                    btnSubmit.setBackgroundColor(resources.getColor(android.R.color.black));
                    btnSubmit.setEnabled(true);
                }
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = edtRule.getText().toString();
                if (!content.isEmpty()) {
                    viewModel.post(new FilterAction.Add(content));
                    edtRule.setText("");
                }
            }
        });

        return view;
    }

    private void dispose() {
        viewModel.dispose();
        RxUtils.dispose(disposable);
    }
}
