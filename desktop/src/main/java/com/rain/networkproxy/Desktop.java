package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.socket.SocketClient;
import com.rain.networkproxy.socket.SocketConnectionStatus;
import com.rain.networkproxy.storage.FilterStorage;

import org.json.JSONObject;

import java.util.Collections;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.CheckBoxListCell;
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
    private final FilterStorage filterStorage = new FilterStorage();
    private final SocketClient socketClient = new SocketClient(state, filterStorage);
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();

    private final TextArea bodyTextArea = new TextArea();
    private final TextArea statusTextArea = new TextArea();

    private final ListView<InternalResponse> responseList = new ListView<>();
    private final ObservableList<InternalResponse> responseObservableList = FXCollections.observableArrayList();

    private final ListView<FilterItem> filterList = new ListView<>();
    private final ObservableList<FilterItem> filterObservableList = FXCollections.observableArrayList();

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
    }

    private Node toolBar() {
        final Text status = new Text();
        disposables.add(socketClient.getStatus()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(socketConnectionStatus -> {
                    status.setText(socketConnectionStatus.name());
                    if (socketConnectionStatus == SocketConnectionStatus.DISCONNECTED) {
                        cleanTextAreas();
                        responseObservableList.setAll(Collections.emptyList());
                    }
                }));

        Button connectBtn = new Button("Connect");
        connectBtn.setOnMouseClicked(event -> socketClient.connect());

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(status, connectBtn);
        return toolBar;
    }

    private Node content() {
        BorderPane borderPan = new BorderPane();
        borderPan.setPadding(new Insets(10, 10, 10, 10));
        borderPan.setCenter(detailView());
        borderPan.setLeft(leftView());
        return borderPan;
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

    private void sendInstruction(Instruction instruction) {
        final String message = gson.toJson(new SocketMessage<>(SocketMessage.INSTRUCTION, instruction));
        cleanTextAreas();
        socketClient.writeMessage(message);
    }

    private Node detailActions() {
        Button executeBtn = new Button("Execute");
        executeBtn.setOnMouseClicked(event -> {
            final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
            if (value == null) {
                return;
            }

            final String body = bodyTextArea.getText();
            final Integer status = Integer.valueOf(statusTextArea.getText());
            sendInstruction(new Instruction(value.getId(), new Instruction.Input(status, body)));
        });

        Button skipBtn = new Button("Skip");
        skipBtn.setOnMouseClicked(event -> {
            final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
            if (value == null) {
                return;
            }

            sendInstruction(new Instruction(value.getId(), new Instruction.Input()));
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

    private Node leftView() {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(filterView());
        borderPane.setCenter(pendingResponseView());
        return borderPane;
    }

    private Node filterView() {
        // TODO: handle activate/deactivate rule
        // TODO: handle add new rule
        filterList.setCellFactory(CheckBoxListCell.forListView(param -> new SimpleBooleanProperty()));
        filterList.setCellFactory(new Callback<ListView<FilterItem>, ListCell<FilterItem>>() {
            @Override
            public ListCell<FilterItem> call(ListView<FilterItem> param) {
                return new ListCell<FilterItem>() {
                    @Override
                    protected void updateItem(FilterItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getRule());
                        }
                    }
                };
            }
        });
        filterList.setItems(filterObservableList);
        filterList.setMaxHeight(200);

        disposables.add(filterStorage.getFilters()
                .distinctUntilChanged()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(filterObservableList::setAll));

        return filterList;
    }

    private Node pendingResponseView() {
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
        responseList.setOnMouseClicked(event -> {
            final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
            populateDetail(value);
        });
        responseList.setOnKeyReleased(event -> {
            final InternalResponse value = responseList.getSelectionModel().getSelectedItem();
            populateDetail(value);
        });

        disposables.add(state.getResponses()
                .distinctUntilChanged()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(responseObservableList::setAll));

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
