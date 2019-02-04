package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.rain.networkproxy.filter.FilterEvent;
import com.rain.networkproxy.filter.FilterInteractor;
import com.rain.networkproxy.filter.FilterStorage;
import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.setting.SettingStorage;
import com.rain.networkproxy.socket.SocketClient;
import com.rain.networkproxy.socket.SocketConnectionStatus;
import com.rain.networkproxy.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Optional;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
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
import javafx.util.StringConverter;

public class Desktop extends Application {
    private final DesktopState state = new DesktopState();
    private final FilterStorage filterStorage = new FilterStorage();
    private final SettingStorage settingStorage = new SettingStorage();
    private final FilterInteractor filterInteractor = new FilterInteractor(filterStorage);
    private final SocketClient socketClient = new SocketClient(state, filterStorage, settingStorage);
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
        filterInteractor.start();
    }

    private void showAddFilter() {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add filter");
        dialog.setHeaderText("Example: \n/todos \n/todos/* \n/todos/*/comments \nhttp://www.com.example/todos");
        dialog.setContentText("Enter your rule:");

        final Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> filterInteractor.handle(new FilterEvent.Create(value)));
    }

    private void showChangePort() {
        final TextInputDialog dialog = new TextInputDialog(String.valueOf(settingStorage.getPort()));
        dialog.setTitle("Change Port");
        dialog.setContentText("Enter your port:");

        final Optional<String> result = dialog.showAndWait();
        result.filter(StringUtils::isInteger)
                .ifPresent(value -> settingStorage.setPort(Integer.valueOf(value)));
    }

    private Node toolBar() {
        final Button btnConnect = new Button("Connect");
        btnConnect.setOnMouseClicked(event -> socketClient.connect());

        final Button btnAddFilter = new Button("Add Filter");
        btnAddFilter.setOnMouseClicked(event -> showAddFilter());

        final Button btnChangePort = new Button("Change Port");
        btnChangePort.setOnMouseClicked(event -> showChangePort());

        final Text status = new Text();
        disposables.add(socketClient.getStatus()
                .observeOn(JavaFxScheduler.platform())
                .subscribe(socketConnectionStatus -> {
                    status.setText(socketConnectionStatus.name());
                    if (socketConnectionStatus == SocketConnectionStatus.DISCONNECTED) {
                        cleanTextAreas();
                        responseObservableList.setAll(Collections.emptyList());
                        btnConnect.setDisable(false);
                    } else if (socketConnectionStatus == SocketConnectionStatus.CONNECTED) {
                        btnConnect.setDisable(true);
                    }
                }));

        final ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(status, btnConnect, btnAddFilter, btnChangePort);
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

    private Callback<ListView<FilterItem>, ListCell<FilterItem>> filterCellFactory() {
        return list -> {
            final CheckBoxListCell<FilterItem> cell = new CheckBoxListCell<>(param -> {
                final ObservableBooleanValue observable = new SimpleBooleanProperty(param.isActive());
                observable.addListener((ignored, oldValue, newValue) ->
                        filterInteractor.handle(new FilterEvent.Update(param.getRule(), newValue)));

                return observable;
            }, new StringConverter<FilterItem>() {
                @Override
                public String toString(FilterItem object) {
                    return object.getRule();
                }

                @Override
                public FilterItem fromString(String string) {
                    return null;
                }
            });

            // Menu
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete"));
            deleteItem.setOnAction(event -> filterInteractor.handle(new FilterEvent.Delete(cell.getItem().getRule())));
            contextMenu.getItems().addAll(deleteItem);
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });

            return cell;
        };
    }

    private Node filterView() {
        filterList.setCellFactory(filterCellFactory());
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
            if (json.startsWith("[")) {
                bodyTextArea.setText(new JSONArray(json).toString(2));
            } else {
                bodyTextArea.setText(new JSONObject(json).toString(2));
            }
            statusTextArea.setText(String.valueOf(internalResponse.getStatus()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
