package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.socket.SocketClient;
import com.rain.networkproxy.socket.SocketConnectionStatus;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

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
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Desktop extends Application {
    private final DesktopState state = new DesktopState();
    private final SocketClient socketClient = new SocketClient(state);
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();

    private final TextArea bodyTextArea = new TextArea();
    private final TextArea statusTextArea = new TextArea();
    private final ListView<InternalResponse> responseList = new ListView<>();
    private final ObservableList<InternalResponse> responseObservableList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar());
        borderPane.setCenter(content());
        stage.setTitle("NetworkProxyClient");
        stage.setScene(new Scene(borderPane));
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();

        socketClient.start();
        bindState();
    }

    private Node toolBar() {
        final Text status = new Text();
        disposables.add(socketClient.getStatus()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(new Consumer<SocketConnectionStatus>() {
                    @Override
                    public void accept(SocketConnectionStatus socketConnectionStatus) {
                        status.setText(socketConnectionStatus.name());
                        if (socketConnectionStatus == SocketConnectionStatus.DISCONNECTED) {
                            cleanTextAreas();
                            responseObservableList.setAll(Collections.<InternalResponse>emptyList());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));

        Button connectBtn = new Button("Connect");
        connectBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                socketClient.connect();
            }
        });

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(status, connectBtn);
        return toolBar;
    }

    private Node content() {
        BorderPane borderPan = new BorderPane();
        borderPan.setPadding(new Insets(10, 10, 10, 10));
        borderPan.setCenter(detailView());
        borderPan.setLeft(pendingResponsesView());
        return borderPan;
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

    private void cleanTextAreas() {
        bodyTextArea.setText("");
        statusTextArea.setText("");
    }

    private Node detailActions() {
        Button executeBtn = new Button("Execute");
        executeBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
                if (value == null) {
                    return;
                }

                final String body = bodyTextArea.getText();
                final Integer status = Integer.valueOf(statusTextArea.getText());
                final String message = gson.toJson(new Instruction(value.getId(), new Instruction.Input(status, body)));
                cleanTextAreas();
                socketClient.writeMessage(message);
            }
        });

        Button skipBtn = new Button("Skip");
        skipBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
                if (value == null) {
                    return;
                }

                final String message = gson.toJson(new Instruction(value.getId(), new Instruction.Input()));
                cleanTextAreas();
                socketClient.writeMessage(message);
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(executeBtn, skipBtn);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10, 10, 10, 10));
        return hbox;
    }

    private Node detailView() {
        BorderPane borderPan = new BorderPane();
        borderPan.setTop(statusTextArea);
        borderPan.setCenter(bodyTextArea);
        borderPan.setBottom(detailActions());
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
            cleanTextAreas();
        } else {
            bodyTextArea.setText(new JSONObject(json).toString(2));
            statusTextArea.setText(String.valueOf(internalResponse.getStatus()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
