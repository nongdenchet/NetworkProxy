package com.rain.networkproxy;

import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.socket.SocketClient;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.JSONObject;

import java.util.List;

public class Desktop extends Application {
    private final DesktopState state = new DesktopState();
    private final SocketClient socketClient = new SocketClient(state);
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();

    private final TextArea detailText = new TextArea();
    private final ListView<InternalResponse> responseList = new ListView<>();
    private final ObservableList<InternalResponse> responseObservableList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        BorderPane borderPan = new BorderPane();
        borderPan.setPadding(new Insets(10, 10, 10, 10));
        borderPan.setCenter(detailView());
        borderPan.setLeft(pendingResponsesView());
        stage.setTitle("NetworkProxyClient");
        stage.setScene(new Scene(borderPan));
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();

        socketClient.start();
        bindState();
    }

    private void bindState() {
        disposables.add(state.getResponses()
                .distinctUntilChanged()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(new Consumer<List<InternalResponse>>() {
                    @Override
                    public void accept(List<InternalResponse> internalResponses) {
                        responseObservableList.setAll(internalResponses);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    @Override
    public void stop() {
        disposables.dispose();
        socketClient.stop();
    }

    private Node detailView() {
        Button executeBtn = new Button("Execute");
        executeBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
                if (value == null) {
                    return;
                }

                final String body = detailText.getText().isEmpty() ? null : detailText.getText();
                final String message = gson.toJson(new Instruction(value.getId(), new Instruction.Input(null, body)));
                detailText.setText("");
                socketClient.writeMessage(message);
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(executeBtn);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10, 10, 10, 10));

        BorderPane borderPan = new BorderPane();
        borderPan.setCenter(detailText);
        borderPan.setBottom(hbox);
        borderPan.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths.DEFAULT
        )));
        return borderPan;
    }

    private Node pendingResponsesView() {
        responseList.setCellFactory(new Callback<ListView<InternalResponse>, ListCell<InternalResponse>>() {
            @Override
            public ListCell<InternalResponse> call(ListView<InternalResponse> param) {
                return new ListCell<InternalResponse>() {
                    @Override
                    protected void updateItem(InternalResponse item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getUrl());
                        }
                    }
                };
            }
        });
        responseList.setItems(responseObservableList);
        responseList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
                populateDetail(value);
            }
        });
        responseList.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
                populateDetail(value);
            }
        });

        return responseList;
    }

    private void populateDetail(@Nullable InternalResponse internalResponse) {
        if (internalResponse == null) {
            return;
        }

        final String json = internalResponse.getBody();
        if (json == null) {
            detailText.setText("");
        } else {
            JSONObject jsonObj = new JSONObject(json);
            detailText.setText(jsonObj.toString(2));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
