package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2019-12-27
 * @License Apache License Version 2.0
 */
public class MyBoxLanguagesController extends BaseController {

    protected ObservableList<LanguageItem> interfaceData, tableData;
    protected String langName;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab interfaceTab, tableTab;
    @FXML
    protected ListView<String> listView;
    @FXML
    protected TableView<LanguageItem> interfaceView, tableView;
    @FXML
    protected TableColumn<LanguageItem, String> keyColumn, englishColumn, valueColumn,
            tableKeyColumn, tableValueColumn, tableEnglishColumn;
    @FXML
    protected Label langLabel;
    @FXML
    protected Button useButton, copyEnglishButton;

    public MyBoxLanguagesController() {
        baseTitle = AppVariables.message("ManageLanguages");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initIntefaceView();
            initTableView();
            initListView();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initListView() {
        try {
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkListSelected();
                }
            });
            listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    checkListSelected();
                }
            });

            checkListSelected();
            loadList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initIntefaceView() {
        try {
            interfaceData = FXCollections.observableArrayList();

            keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            englishColumn.setCellValueFactory(new PropertyValueFactory<>("english"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            valueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LanguageItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<LanguageItem, String> t) {
                    if (t == null) {
                        return;
                    }
                    LanguageItem row = t.getRowValue();
                    row.setValue(t.getNewValue());
                }
            });
            valueColumn.getStyleClass().add("editable-column");

            interfaceView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            interfaceView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });

            interfaceView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    popMenu(interfaceView, event);
                }
            });

            interfaceView.setItems(interfaceData);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initTableView() {
        try {
            tableData = FXCollections.observableArrayList();

            tableKeyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
            tableEnglishColumn.setCellValueFactory(new PropertyValueFactory<>("english"));
            tableValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            tableValueColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            tableValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LanguageItem, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<LanguageItem, String> t) {
                    if (t == null) {
                        return;
                    }
                    LanguageItem row = t.getRowValue();
                    row.setValue(t.getNewValue());
                }
            });
            tableValueColumn.getStyleClass().add("editable-column");

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });

            tableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    popMenu(tableView, event);
                }
            });

            tableView.setItems(tableData);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case E:
                copyEnglish();
                return;
        }
        super.controlAltHandler(event);
    }

    protected void checkListSelected() {
        if (isSettingValues) {
            return;
        }
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteButton.setDisable(true);
            useButton.setDisable(true);
        } else {
            deleteButton.setDisable(false);
            useButton.setDisable(false);
            langName = selected;
            langLabel.setText(langName);
            loadLanguage(langName);
        }
    }

    protected void checkTableSelected() {
        if (isSettingValues) {
            return;
        }
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        boolean selected = (tab == tableTab) && tableView.getSelectionModel().getSelectedItem() != null
                || (tab == interfaceTab) && interfaceView.getSelectionModel().getSelectedItem() != null;
        copyEnglishButton.setDisable(!selected);
    }

    public void loadList() {
        try {
            isSettingValues = true;
            listView.getItems().clear();
            listView.getItems().addAll(ConfigTools.userLanguages());
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void loadLanguage(String name) {
        interfaceData.clear();
        tableData.clear();
        saveButton.setDisable(true);
        copyEnglishButton.setDisable(true);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        error = null;
                        Map<String, String> interfaceItems = null;
                        File interfaceFile = ConfigTools.interfaceLanguageFile(name);
                        if (interfaceFile != null && interfaceFile.exists()) {
                            interfaceItems = ConfigTools.readValues(interfaceFile);
                        }
                        Enumeration<String> interfaceKeys = CommonValues.BundleEn.getKeys();
                        while (interfaceKeys.hasMoreElements()) {
                            String key = interfaceKeys.nextElement();
                            LanguageItem item = new LanguageItem(key, CommonValues.BundleEn.getString(key));
                            if (interfaceItems != null) {
                                item.setValue(interfaceItems.get(key));
                            }
                            interfaceData.add(item);
                        }

                        File tableFile = ConfigTools.tableLanguageFile(name);
                        Map<String, String> dataItems = null;
                        if (tableFile != null && tableFile.exists()) {
                            dataItems = ConfigTools.readValues(tableFile);
                        }
                        Enumeration<String> tableKeys = CommonValues.TableBundleEn.getKeys();
                        while (tableKeys.hasMoreElements()) {
                            String key = tableKeys.nextElement();
                            LanguageItem item = new LanguageItem(key, CommonValues.TableBundleEn.getString(key));
                            if (dataItems != null) {
                                item.setValue(dataItems.get(key));
                            }
                            tableData.add(item);
                        }

                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error == null) {
                        interfaceView.refresh();
                        tableView.refresh();
                        saveButton.setDisable(false);
                    } else {
                        popError(error);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void popMenu(TableView<LanguageItem> view, ContextMenuEvent event) {
        if (view.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;
        menu = new MenuItem(message("CopyEnglish"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyEnglish();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(view, event.getScreenX(), event.getScreenY());
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(message("ManageLanguages"));
        dialog.setHeaderText(message("InputLangaugeName"));
        dialog.setContentText("");
        dialog.getEditor().setPrefWidth(200);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isBlank()) {
            return;
        }
        langName = result.get().trim();
        langLabel.setText(langName);
        loadLanguage(null);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<String> selected = listView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVariables.message("SureDelete"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonSure) {
            return;
        }
        for (String name : selected) {
            File interfaceFile = ConfigTools.interfaceLanguageFile(name);
            File tableFile = ConfigTools.tableLanguageFile(name);
            FileTools.delete(interfaceFile);
            FileTools.delete(tableFile);
        }
        isSettingValues = true;
        listView.getItems().removeAll(selected);
        isSettingValues = false;
        langName = null;
        langLabel.setText("");
        interfaceData.clear();
        tableData.clear();
        checkListSelected();
    }

    @FXML
    public void copyEnglish() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == interfaceTab) {
            List<LanguageItem> selected = interfaceView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            for (LanguageItem item : selected) {
                item.setValue(item.getEnglish());
            }
            interfaceView.refresh();

        } else if (tab == tableTab) {
            List<LanguageItem> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                return;
            }
            for (LanguageItem item : selected) {
                item.setValue(item.getEnglish());
            }
            tableView.refresh();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            if (langName == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        try {
                            error = null;
                            Map<String, String> interfaceItems = new HashMap();
                            File interfaceFile = ConfigTools.interfaceLanguageFile(langName);
                            for (LanguageItem item : interfaceData) {
                                if (item.getValue() != null && !item.getValue().isBlank()) {
                                    interfaceItems.put(item.getKey(), item.getValue());
                                }
                            }
                            ConfigTools.writeValues(interfaceFile, interfaceItems);

                            Map<String, String> tableItems = new HashMap();
                            File tableFile = ConfigTools.tableLanguageFile(langName);
                            for (LanguageItem item : tableData) {
                                if (item.getValue() != null && !item.getValue().isBlank()) {
                                    tableItems.put(item.getKey(), item.getValue());
                                }
                            }
                            ConfigTools.writeValues(tableFile, tableItems);

                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error == null) {
                            if (!listView.getItems().contains(langName)) {
                                listView.getItems().add(0, langName);
                            }
                            popSuccessful();
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void useAction() {
        String name = listView.getSelectionModel().getSelectedItem();
        if (name == null) {
            return;
        }
        AppVariables.setLanguage(name);
        refresh();
    }

    @FXML
    public void openPath() {
        browseURI(AppVariables.MyBoxLanguagesPath.toURI());
    }

    protected class LanguageItem {

        protected String key, english, value;

        public LanguageItem(String key, String english) {
            this.key = key;
            this.english = english;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getEnglish() {
            return english;
        }

        public void setEnglish(String english) {
            this.english = english;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
