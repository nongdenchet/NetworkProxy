package com.rain.networkproxy.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.ui.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class DetailDialog {
    private final Context context;

    @Nullable
    private Disposable disposable;
    @Nullable
    private AlertDialog alertDialog;

    DetailDialog(@NonNull Context context) {
        this.context = context;
    }

    void hide() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("ConstantConditions")
    private AlertDialog showDialog(String jsonString) {
        final View view = LayoutInflater.from(context).inflate(R.layout.network_proxy_detail, null);
        final TextView content = view.findViewById(R.id.content);
        if (jsonString.isEmpty()) {
            content.setText(R.string.network_proxy_no_content);
        } else {
            content.setText(jsonString);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        dialog.getWindow().setType(Utils.getOverlayType());
        dialog.show();
        return dialog;
    }

    private Single<String> responseToString(final PendingResponse pendingResponse) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final Response response = pendingResponse.getResponse();
                final ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return "";
                }

                final String json = Utils.readFromBuffer(response.headers(), responseBody);
                try {
                    if (json.startsWith("[")) {
                        return new JSONArray(json).toString(2);
                    } else {
                        return new JSONObject(json).toString(2);
                    }
                } catch (Exception e) {
                    NPLogger.logError("DetailDialog#responseToString", e);
                    return json;
                }
            }
        });
    }

    void show(final PendingResponse pendingResponse) {
        hide();
        disposable = responseToString(pendingResponse)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String content) {
                        alertDialog = showDialog(content);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("DashboardDetailDialog#showDetail", throwable);
                    }
                });
    }
}
