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
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.ui.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
        dispose();
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void dispose() {
        RxUtils.dispose(disposable);
    }

    private Single<String> responseToString(final PendingResponse pendingResponse) {
        return Single.fromCallable(() -> {
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
        });
    }

    private void showContent(final PendingResponse pendingResponse, final View view) {
        disposable = responseToString(pendingResponse)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonString -> showContent(jsonString, view), throwable ->
                        NPLogger.logError("DashboardDetailDialog#showDetail", throwable));
    }

    private void showContent(final String jsonString, final View view) {
        view.findViewById(R.id.progress).setVisibility(View.GONE);
        view.findViewById(R.id.content).setVisibility(View.VISIBLE);

        final TextView tvContent = view.findViewById(R.id.tvContent);
        if (jsonString.isEmpty()) {
            tvContent.setText(R.string.network_proxy_no_content);
        } else {
            tvContent.setText(jsonString);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("InflateParams")
    void show(final PendingResponse pendingResponse) {
        hide();
        final View view = LayoutInflater.from(context).inflate(R.layout.network_proxy_detail, null);
        alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setOnCancelListener(dialog -> dispose())
                .create();
        alertDialog.getWindow().setType(Utils.getOverlayType());
        alertDialog.setOnShowListener(dialog -> showContent(pendingResponse, view));
        alertDialog.show();
    }
}
