package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableImageCell;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesBrowserController extends ImageViewerController {

    private final ObservableList<File> imageFileList = FXCollections.observableArrayList();
    private final ObservableList<ImageInformation> tableData = FXCollections.observableArrayList();
    private int rowsNum, colsNum, filesNumber, popSize = 500;
    private List<VBox> imageBoxList;
    private List<ScrollPane> imageScrollList;
    private List<ImageView> imageViewList;
    private List<Label> imageTitleList;
    private Popup imagePop;
    private ImageView popView;
    private Text popText;

    private TableView<ImageInformation> tableView;
    private TableColumn<ImageInformation, ImageInformation> imageColumn;
    private TableColumn<ImageInformation, String> fileColumn, formatColumn, pixelsColumn, csColumn, loadColumn;
    private TableColumn<ImageInformation, Integer> indexColumn;
    private TableColumn<ImageInformation, Long> fileSizeColumn, modifiedTimeColumn, createTimeColumn;
    private TableColumn<ImageInformation, Boolean> isScaledColumn, isMutipleFramesColumn;

    protected List<File> nextFiles, previousFiles;
    protected List<Integer> selectedIndexes;
    protected List<ImageInformation> selectedImages;
    protected int maxShow = 100;
    private File path;
    private DisplayMode displayMode;

    private enum DisplayMode {
        ImagesGrid, FilesList, ThumbnailsList, None
    }

    @FXML
    protected VBox imagesPane, mainBox, viewBox, gridOptionsBox;
    @FXML
    protected HBox fileOpBox;
    @FXML
    protected ComboBox<String> colsnumBox, filesBox, popSizeSelector;
    @FXML
    protected CheckBox saveRotationCheck, popCheck;
    @FXML
    protected Label totalLabel;
    @FXML
    protected ToggleGroup popGroup;

    public ImagesBrowserController() {
        baseTitle = AppVariables.message("ImagesBrowser");
        TipsLabelKey = "ImagesBrowserTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            colsNum = -1;
            currentAngle = 0;
            displayMode = DisplayMode.ImagesGrid;
            defaultLoadWidth = 512;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> cvalues = Arrays.asList("3", "4", "6",
                    AppVariables.message("ThumbnailsList"), AppVariables.message("FilesList"),
                    "2", "5", "7", "8", "9", "10", "16", "25", "20", "12", "15");
            colsnumBox.getItems().addAll(cvalues);
            colsnumBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        if (AppVariables.message("ThumbnailsList").equals(newValue)) {
                            displayMode = DisplayMode.ThumbnailsList;
                        } else if (AppVariables.message("FilesList").equals(newValue)) {
                            displayMode = DisplayMode.FilesList;
                        } else {
                            tableData.clear();
                            displayMode = DisplayMode.ImagesGrid;
                            colsNum = Integer.valueOf(newValue);
                            if (colsNum >= 0) {
                                FxmlControl.setEditorNormal(colsnumBox);
                            } else {
                                FxmlControl.setEditorBadStyle(colsnumBox);
                                return;
                            }
                        }
                        makeImagesPane();
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(colsnumBox);
                    }
                }
            });

            filesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
                    try {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        filesNumber = Integer.valueOf(newValue);
                        if (filesNumber > 0) {
                            FxmlControl.setEditorNormal(filesBox);
                            makeImagesNevigator(true);

                        } else {
                            FxmlControl.setEditorBadStyle(filesBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(filesBox);
                    }
                }
            });

            List<String> sizeValues = Arrays.asList("500", "400", "600", "300", "200", "700");
            popSizeSelector.getItems().addAll(sizeValues);
            popSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue,
                        String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            FxmlControl.setEditorNormal(popSizeSelector);
                            popSize = v;
                            AppVariables.setUserConfigInt("ImageBrowserPopSize", popSize);
                        } else {
                            FxmlControl.setEditorBadStyle(popSizeSelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(popSizeSelector);
                    }
                }
            });
            popSizeSelector.getSelectionModel().select(AppVariables.getUserConfigInt("ImageBrowserPopSize", 400) + "");

            popCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Pop", true));
            popCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "Pop", popCheck.isSelected());
                }
            });

            saveRotationCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SaveRotation", true));
            saveRotationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "SaveRotation", saveRotationCheck.isSelected());
                }
            });

            fileBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            viewPane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            browsePane.disableProperty().bind(Bindings.isEmpty(imageFileList));
            mainBox.disableProperty().bind(Bindings.isEmpty(imageFileList));
            leftPaneControl.visibleProperty().bind(Bindings.isEmpty(imageFileList).not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        FxmlControl.setTooltip(selectFileButton, new Tooltip(message("SelectMultipleFilesBrowse")));
    }

    @Override
    protected void setLoadWidth() {
        makeImagesPane();
    }

    public void paneSize(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null
                || sPane == null) {
            return;
        }
        FxmlControl.paneSize(sPane, iView);
    }

    public void imageSize(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.imageSize(sPane, iView);
    }

    public void zoomIn(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.zoomIn(sPane, iView, xZoomStep, yZoomStep);
    }

    public void zoomOut(int index) {
        ImageView iView = imageViewList.get(index);
        ScrollPane sPane = imageScrollList.get(index);
        if (iView == null || iView.getImage() == null) {
            return;
        }
        FxmlControl.zoomOut(sPane, iView, xZoomStep, yZoomStep);
    }

    public void moveRight(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, -40, sPane.getVvalue());
    }

    public void moveLeft(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, 40, sPane.getVvalue());
    }

    public void moveUp(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, sPane.getHvalue(), 40);
    }

    public void moveDown(int index) {
        ScrollPane sPane = imageScrollList.get(index);
        if (sPane == null) {
            return;
        }
        FxmlControl.setScrollPane(sPane, sPane.getHvalue(), -40);
    }

    @Override
    public void rotate(int rotateAngle) {
        currentAngle = rotateAngle;
        if (saveRotationCheck.isSelected()) {
            saveRotation(selectedIndexes, rotateAngle);
        } else {
            rotate(selectedIndexes, rotateAngle);
        }
    }

    public void rotate(int index, int rotateAngle) {
        List<Integer> indexs = new ArrayList<>();
        indexs.add(index);
        if (saveRotationCheck.isSelected()) {
            saveRotation(indexs, rotateAngle);
        } else {
            rotate(indexs, rotateAngle);
        }
    }

    public void rotate(List<Integer> indexs, int rotateAngle) {
        switch (displayMode) {
            case FilesList:
                break;
            case ThumbnailsList:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < tableData.size(); ++i) {
                        ImageInformation info = tableData.get(i);
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(i, info);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageInformation info = tableData.get(index);
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(index, info);
                    }
                }
                tableView.refresh();
                if (indexs != null) {
                    for (int i = 0; i < indexs.size(); ++i) {
                        tableView.getSelectionModel().select(indexs.get(i));
                    }
                }
                break;
            case ImagesGrid:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < imageViewList.size(); ++i) {
                        ImageView iView = imageViewList.get(i);
                        iView.setRotate(iView.getRotate() + currentAngle);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageView iView = imageViewList.get(index);
                        iView.setRotate(iView.getRotate() + currentAngle);
                    }
                }
                break;
        }
    }

    public void saveRotation(List<Integer> indexs, final int rotateAngle) {
        if (!saveRotationCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (indexs == null || indexs.isEmpty()) {
                        for (int i = 0; i < tableData.size(); ++i) {
                            ImageInformation info = tableData.get(i);
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadImage(loadWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(i, newInfo);
                        }
                    } else {
                        for (int i = 0; i < indexs.size(); ++i) {
                            int index = indexs.get(i);
                            ImageInformation info = tableData.get(index);
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadImage(loadWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(index, newInfo);
                        }
                    }
                    return true;
                }

                private ImageInformation saveRotation(ImageInformation info, double rotateAngle) {
                    if (info == null || info.getImageFileInformation() == null || info.isIsMultipleFrames()) {
                        return null;
                    }
                    try {
                        File file = info.getImageFileInformation().getFile();
                        BufferedImage bufferedImage = ImageInformation.getBufferedImage(info);
                        bufferedImage = ImageManufacture.rotateImage(bufferedImage, (int) rotateAngle);
                        ImageFileWriters.writeImageFile(bufferedImage, file);
                        ImageInformation newInfo = loadImageInfo(file);
                        return newInfo;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return null;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (displayMode == DisplayMode.ImagesGrid) {
                        if (indexs == null || indexs.isEmpty()) {
                            for (int i = 0; i < tableData.size(); ++i) {
                                ImageView iView = imageViewList.get(i);
                                iView.setImage(tableData.get(i).getThumbnail());
                            }
                        } else {
                            for (int i = 0; i < indexs.size(); ++i) {
                                int index = indexs.get(i);
                                ImageView iView = imageViewList.get(index);
                                iView.setImage(tableData.get(index).getThumbnail());
                            }
                        }
                    } else {
                        tableView.refresh();
                        if (indexs != null) {
                            for (int i = 0; i < indexs.size(); ++i) {
                                tableView.getSelectionModel().select(indexs.get(i));
                            }
                        }
                    }
                    popInformation(message("Saved"));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void paneSize() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                paneSize(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                paneSize(i);
            }
        }
    }

    @FXML
    @Override
    public void loadedSize() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                imageSize(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                imageSize(i);
            }
        }
    }

    @FXML
    @Override
    public void zoomIn() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                zoomIn(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomIn(i);
            }
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                zoomOut(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomOut(i);
            }
        }
    }

    @FXML
    @Override
    public void moveRight() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveRight(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                moveRight(i);
            }
        }

    }

    @FXML
    @Override
    public void moveLeft() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveLeft(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                moveLeft(i);
            }
        }
    }

    @FXML
    @Override
    public void moveUp() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveUp(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                moveUp(i);
            }
        }
    }

    @FXML
    @Override
    public void moveDown() {
        if (selectedIndexes != null && !selectedIndexes.isEmpty()) {
            for (int i : selectedIndexes) {
                moveDown(i);
            }
        } else {
            for (int i = 0; i < imageViewList.size(); ++i) {
                moveDown(i);
            }
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFiles != null) {
            previousFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(nextFiles);
            makeImagesNevigator(false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFiles != null) {
            nextFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(previousFiles);
            makeImagesNevigator(false);
        }
    }

    @FXML
    public void viewAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            view(selectedIndexes.get(0));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void view(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                File file = imageInfo.getImageFileInformation().getFile();
                FxmlStage.openImageViewer(null, file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void renameAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            rename(selectedIndexes.get(0));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void rename(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            FileRenameController controller = (FileRenameController) FxmlStage.openStage(CommonValues.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(index, newFile);
                });
            });
            controller.set(file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void fileRenamed(int index, File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            recordFileOpened(newFile);
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            String oname = file.getAbsolutePath();
            changeFile(info, newFile);
            String nname = newFile.getAbsolutePath();
            tableData.set(index, info);
            imageFileList.set(index, newFile);
            if (displayMode == DisplayMode.ImagesGrid) {
                imageTitleList.get(index).setText(newFile.getName());
            } else if (displayMode == DisplayMode.FilesList || displayMode == DisplayMode.ThumbnailsList) {
                tableView.refresh();
            }
//            makeImagesNevigator(true);
            popInformation(MessageFormat.format(AppVariables.message("FileRenamed"), oname, nname));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @FXML
    public void deleteFilesAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
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
            }
            int count = 0;
            for (ImageInformation info : selectedImages) {
                File file = info.getImageFileInformation().getFile();
                if (FileTools.delete(file)) {
                    imageFileList.remove(file);
                    count++;
                }
            }
            popInformation(AppVariables.message("TotalDeletedFiles") + ": " + count);
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void delete(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
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
            }
            ImageInformation info = tableData.get(index);
            File file = info.getImageFileInformation().getFile();
            if (FileTools.delete(file)) {
                imageFileList.remove(file);
            }
            popSuccessful();
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        try {
            if (selectedImages == null || selectedImages.isEmpty()) {
                fileOpBox.setDisable(true);
                return;
            }
            ImageInformation info = selectedImages.get(0);
            if (info != null) {
                FxmlStage.showImageInformation(info);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void info(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                FxmlStage.showImageInformation(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void meta(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                FxmlStage.showImageMetaData(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void makeImagesPane() {
        try {
            imagesPane.getChildren().clear();
            imageBoxList = new ArrayList<>();
            imageViewList = new ArrayList<>();
            imageTitleList = new ArrayList<>();
            imageScrollList = new ArrayList<>();
            selectedImages = new ArrayList<>();
            selectedIndexes = new ArrayList<>();
            fileOpBox.setDisable(true);
            rowsNum = 0;

            if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
                if (viewBox.getChildren().contains(gridOptionsBox)) {
                    viewBox.getChildren().remove(gridOptionsBox);
                }
                loadWidthBox.setDisable(true);
                makeListBox();

            } else if (colsNum > 0) {
                if (!viewBox.getChildren().contains(gridOptionsBox)) {
                    viewBox.getChildren().add(gridOptionsBox);
                }
                loadWidthBox.setDisable(false);
                makeImagesGrid();
            }
            FxmlControl.refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void makeImagesGrid() {
        if (colsNum <= 0 || displayMode != DisplayMode.ImagesGrid) {
            return;
        }

        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    loadImageInfos();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    makeGridBox();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void makeGridBox() {
        int num = tableData.size();
        HBox line = new HBox();
        for (int i = 0; i < num; ++i) {
            if (i % colsNum == 0) {
                line = new HBox();
                line.setAlignment(Pos.TOP_CENTER);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                imagesPane.getChildren().add(line);
                VBox.setVgrow(line, Priority.ALWAYS);
                HBox.setHgrow(line, Priority.ALWAYS);
                rowsNum++;
            }

            final VBox vbox = new VBox();
            vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setPadding(new Insets(5, 5, 5, 5));
            line.getChildren().add(vbox);

            ScrollPane sPane = new ScrollPane();
            sPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(sPane, Priority.ALWAYS);
            HBox.setHgrow(sPane, Priority.ALWAYS);
            sPane.setPannable(true);
            sPane.setFitToWidth(true);
            sPane.setFitToHeight(true);

            HBox iBox = new HBox();
            iBox.setAlignment(Pos.TOP_CENTER);
            iBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(iBox, Priority.ALWAYS);
            HBox.setHgrow(iBox, Priority.ALWAYS);
            sPane.setContent(iBox);

            ImageView iView = new ImageView();
            iView.setPreserveRatio(true);
            iBox.getChildren().add(iView);

            Label titleLabel = new Label();
            titleLabel.setWrapText(true);
            VBox.setVgrow(titleLabel, Priority.NEVER);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            vbox.getChildren().add(titleLabel);
            vbox.getChildren().add(sPane);
            vbox.setPickOnBounds(false);

            ImageInformation imageInfo = tableData.get(i);
            File file = imageInfo.getImageFileInformation().getFile();
            iView.setImage(imageInfo.loadImage(loadWidth));

            String title = file.getName();
            if (imageInfo.isIsMultipleFrames()) {
                title += " " + AppVariables.message("MultipleFrames");
                titleLabel.setStyle("-fx-text-box-border: purple;   -fx-text-fill: purple;");
            }
            titleLabel.setText(title);

            final int index = i;
            vbox.setOnMouseClicked((MouseEvent event) -> {
                if (selectedImages.contains(imageInfo)) {
                    selectedImages.remove(imageInfo);
                    vbox.setStyle(null);
                } else {
                    selectedImages.add(imageInfo);
                    vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
                }
                Integer o = Integer.valueOf(index);
                if (selectedIndexes.contains(o)) {
                    selectedIndexes.remove(o);
                } else {
                    selectedIndexes.add(o);
                }
                fileOpBox.setDisable(selectedImages.isEmpty());
                viewButton.setDisable(selectedImages.size() > 1);
                infoButton.setDisable(selectedImages.size() > 1);
                renameButton.setDisable(selectedImages.size() > 1);

                if (event.getClickCount() > 1) {
                    FxmlStage.openImageViewer(null, file);
                }

                if (!needNotContextMenu && contextMenuCheck.isSelected()
                        && event.getButton() == MouseButton.SECONDARY) {
                    popImageMenu(index, iView, event);
                }

            });

            vbox.setOnMouseEntered((MouseEvent event) -> {
                String str1 = imageInfo.getFileName() + " "
                        + AppVariables.message("Format") + ":" + imageInfo.getImageFormat() + "  "
                        + AppVariables.message("ModifyTime") + ":" + DateTools.datetimeToString(file.lastModified()) + " "
                        + AppVariables.message("Size") + ":" + FileTools.showFileSize(file.length()) + "  "
                        + AppVariables.message("Pixels") + ":" + imageInfo.getWidth() + "x" + imageInfo.getHeight() + "  "
                        + AppVariables.message("LoadedSize") + ":"
                        + (int) iView.getImage().getWidth() + "x" + (int) iView.getImage().getHeight() + "  "
                        + AppVariables.message("DisplayedSize") + ":"
                        + (int) iView.getFitWidth() + "x" + (int) iView.getFitHeight();
                bottomLabel.setText(str1);
                if (imagePop != null) {
                    imagePop.hide();
                }
                if (popCheck.isSelected()) {
                    makeImagePopup(vbox, imageInfo, iView);
                }
            });

            imageScrollList.add(sPane);
            imageViewList.add(iView);
            imageTitleList.add(titleLabel);
            imageBoxList.add(vbox);

        }

        for (int i = 0; i < num; ++i) {
            double w = imagesPane.getWidth() / colsNum - 5;
            double h = imagesPane.getHeight() / rowsNum - 5;
            VBox vbox = imageBoxList.get(i);
            vbox.setPrefWidth(w);
            vbox.setPrefHeight(h);
        }
        // https://stackoverflow.com/questions/26152642/get-the-height-of-a-node-in-javafx-generate-a-layout-pass
        imagesPane.applyCss();
        imagesPane.layout();
        paneSize();

    }

    protected void popImageMenu(int index, ImageView iView, MouseEvent event) {
        if (needNotContextMenu || iView == null || iView.getImage() == null) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(message("LoadedSize") + "  CTRL+1");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            imageSize(index);
        });
        items.add(menu);

        menu = new MenuItem(message("PaneSize") + "  CTRL+2");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            paneSize(index);
        });
        items.add(menu);

        menu = new MenuItem(message("ZoomIn") + "  CTRL+3");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            zoomIn(index);
        });
        items.add(menu);

        menu = new MenuItem(message("ZoomOut") + "  CTRL+4");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            zoomIn(index);
        });
        items.add(menu);

        menu = new MenuItem(message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(message("RotateRight"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 90);
        });
        items.add(menu);

        menu = new MenuItem(message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(message("TurnOver"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 180);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        menu = new MenuItem(message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllAction();
        });
        items.add(menu);

        menu = new MenuItem(message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
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
        popMenu.show(iView, event.getScreenX(), event.getScreenY());

    }

    private void makeImagePopup(VBox imageBox, ImageInformation imageInfo, ImageView iView) {
        try {
            File file = imageInfo.getImageFileInformation().getFile();
            Image iImage = imageInfo.loadImage(loadWidth);
            imagePop = new Popup();
            imagePop.setWidth(popSize + 40);
            imagePop.setHeight(popSize + 40);
            imagePop.setAutoHide(true);

            VBox vbox = new VBox();
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setMaxHeight(Double.MAX_VALUE);
            vbox.setStyle("-fx-background-color: white;");
            imagePop.getContent().add(vbox);

            popView = new ImageView();
            popView.setImage(iImage);
            if (iImage.getWidth() > iImage.getHeight()) {
                popView.setFitWidth(popSize);
            } else {
                popView.setFitHeight(popSize);
            }
            popView.setPreserveRatio(true);
            vbox.getChildren().add(popView);

            popText = new Text();
            popText.setStyle("-fx-font-size: 1.0em;");

            vbox.getChildren().add(popText);
            vbox.setPadding(new Insets(15, 15, 15, 15));

            String info = imageInfo.getFileName() + "\n"
                    + AppVariables.message("Format") + ":" + imageInfo.getImageFormat() + "  "
                    + AppVariables.message("ModifyTime") + ":" + DateTools.datetimeToString(file.lastModified()) + "\n"
                    + AppVariables.message("Size") + ":" + FileTools.showFileSize(file.length()) + "  "
                    + AppVariables.message("Pixels") + ":" + imageInfo.getWidth() + "x" + imageInfo.getHeight() + "\n"
                    + AppVariables.message("LoadedSize") + ":"
                    + (int) iView.getImage().getWidth() + "x" + (int) iView.getImage().getHeight() + "  "
                    + AppVariables.message("DisplayedSize") + ":"
                    + (int) iView.getFitWidth() + "x" + (int) iView.getFitHeight();
            popText.setText(info);
            popText.setWrappingWidth(popSize);
            Bounds bounds = imageBox.localToScreen(imageBox.getBoundsInLocal());
            imagePop.show(imageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void makeSourceTable() {
        try {
            tableView = new TableView<>();
            tableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            HBox.setHgrow(tableView, Priority.ALWAYS);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setTableMenuButtonVisible(true);

            fileColumn = new TableColumn<>(AppVariables.message("File"));
            fileColumn.setPrefWidth(220);
            formatColumn = new TableColumn<>(AppVariables.message("Format"));
            formatColumn.setPrefWidth(60);
            csColumn = new TableColumn<>(AppVariables.message("Color"));
            csColumn.setPrefWidth(120);
            indexColumn = new TableColumn<>(AppVariables.message("Index"));
            pixelsColumn = new TableColumn<>(AppVariables.message("Pixels"));
            pixelsColumn.setPrefWidth(140);
            fileSizeColumn = new TableColumn<>(AppVariables.message("Size"));
            fileSizeColumn.setPrefWidth(140);
            isMutipleFramesColumn = new TableColumn<>(AppVariables.message("MultipleFrames"));
            modifiedTimeColumn = new TableColumn<>(AppVariables.message("ModifiedTime"));
            modifiedTimeColumn.setPrefWidth(200);
            createTimeColumn = new TableColumn<>(AppVariables.message("CreateTime"));
            createTimeColumn.setPrefWidth(200);

            fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            formatColumn.setCellValueFactory(new PropertyValueFactory<>("imageFormat"));
            csColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            pixelsColumn.setCellValueFactory(new PropertyValueFactory<>("pixelsString"));
            fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            fileSizeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(FileTools.showFileSize(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            isMutipleFramesColumn.setCellValueFactory(new PropertyValueFactory<>("isMultipleFrames"));
            isMutipleFramesColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                @Override
                public TableCell<ImageInformation, Boolean> call(
                        TableColumn<ImageInformation, Boolean> param) {
                    TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Boolean item,
                                boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(AppVariables.message(item.toString()));
                            if (item) {
                                text.setFill(Color.RED);
                            } else {
                                text.setFill(Color.BLACK);
                            }
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            modifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifiedTimeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setText(null);
                                setGraphic(null);
                                return;
                            }
                            text.setText(DateTools.datetimeToString(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });
            createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Long>, TableCell<ImageInformation, Long>>() {
                @Override
                public TableCell<ImageInformation, Long> call(
                        TableColumn<ImageInformation, Long> param) {
                    TableCell<ImageInformation, Long> cell = new TableCell<ImageInformation, Long>() {
                        private final Text text = new Text();

                        @Override
                        protected void updateItem(final Long item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null || item <= 0) {
                                setGraphic(null);
                                setText(null);
                                return;
                            }
                            text.setText(DateTools.datetimeToString(item));
                            setGraphic(text);
                        }
                    };
                    return cell;
                }
            });

            if (displayMode == DisplayMode.ThumbnailsList) {
                imageColumn = new TableColumn<>(AppVariables.message("Image"));
                imageColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
                imageColumn.setCellFactory(new TableImageCell());
                imageColumn.setPrefWidth(110);

                loadColumn = new TableColumn<>(AppVariables.message("LoadedSize"));
                loadColumn.setPrefWidth(140);
                loadColumn.setCellValueFactory(new PropertyValueFactory<>("loadSizeString"));

                isScaledColumn = new TableColumn<>(AppVariables.message("Scaled"));
                isScaledColumn.setCellValueFactory(new PropertyValueFactory<>("isScaled"));
                isScaledColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Boolean>, TableCell<ImageInformation, Boolean>>() {
                    @Override
                    public TableCell<ImageInformation, Boolean> call(
                            TableColumn<ImageInformation, Boolean> param) {
                        TableCell<ImageInformation, Boolean> cell = new TableCell<ImageInformation, Boolean>() {
                            @Override
                            protected void updateItem(final Boolean item,
                                    boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                setText(AppVariables.message(item.toString()));
                            }
                        };
                        return cell;
                    }
                });
                isScaledColumn.setPrefWidth(80);

                tableView.getColumns().addAll(imageColumn, fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn, loadColumn,
                        isMutipleFramesColumn, indexColumn, isScaledColumn, modifiedTimeColumn, createTimeColumn);
            } else {
                tableView.getColumns().addAll(fileColumn, formatColumn, csColumn, pixelsColumn, fileSizeColumn,
                        isMutipleFramesColumn, indexColumn, modifiedTimeColumn, createTimeColumn);
            }

            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        selectedImages = tableView.getSelectionModel().getSelectedItems();
                        selectedIndexes = tableView.getSelectionModel().getSelectedIndices();
                        fileOpBox.setDisable(selectedImages.isEmpty());
                        viewButton.setDisable(selectedImages.size() > 1);
                        infoButton.setDisable(selectedImages.size() > 1);
                        renameButton.setDisable(selectedImages.size() > 1);
                    }
                }
            });

            tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    ImageInformation info = tableView.getSelectionModel().getSelectedItem();
                    if (info == null) {
                        return;
                    }
                    File file = info.getImageFileInformation().getFile();
                    if (event.getClickCount() > 1) {
                        FxmlStage.openImageViewer(null, file);
                    }
                }
            });

            tableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    if (isSettingValues || !contextMenuCheck.isSelected()) {
                        return;
                    }
                    int index = tableView.getSelectionModel().getSelectedIndex();
                    if (index < 0) {
                        return;
                    }
                    popTableMenu(event, index);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void popTableMenu(ContextMenuEvent event, int index) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        menu = new MenuItem(message("View"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            view(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Information"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            info(index);
        });
        items.add(menu);

        menu = new MenuItem(message("MetaData"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            meta(index);
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("RotateLeft"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 270);
        });
        items.add(menu);

        menu = new MenuItem(message("RotateRight"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 90);
        });
        items.add(menu);

        menu = new MenuItem(message("TurnOver"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rotate(index, 180);
        });
        items.add(menu);

        menu = new MenuItem(message("SelectAll"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectAllAction();
        });
        items.add(menu);

        menu = new MenuItem(message("SelectNone"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            selectNoneAction();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            rename(index);
        });
        items.add(menu);

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            delete(index);
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
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    private void makeListBox() {
        try {
            if (displayMode != DisplayMode.ThumbnailsList && displayMode != DisplayMode.FilesList) {
                return;
            }
            makeSourceTable();
            imagesPane.getChildren().add(tableView);
            tableView.setItems(null);
            tableView.refresh();
            if (imageFileList == null || imageFileList.isEmpty()) {
                return;
            }

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        loadImageInfos();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        tableView.setItems(tableData);
                        tableView.refresh();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void loadImageInfos() {
        try {
            tableData.clear();
            for (int i = 0; i < imageFileList.size(); ++i) {
                File file = imageFileList.get(i);
                ImageInformation imageInfo = loadImageInfo(file);
                if (imageInfo != null) {
                    tableData.add(imageInfo);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private ImageInformation loadImageInfo(File file) {
        ImageInformation imageInfo;
        int width;
        if (displayMode == DisplayMode.ImagesGrid) {
            width = loadWidth;
        } else {
            width = 100;
        }
        ImageFileInformation finfo = ImageInformation.loadImageFileInformation(file);
        if (finfo == null) {
            return null;
        }
        imageInfo = finfo.getImageInformation();
        if (displayMode != DisplayMode.FilesList) {
            ImageInformation.loadImage(imageInfo, width);
        }
        return imageInfo;
    }

    @Override
    public void makeImageNevigator() {
        makeImagesNevigator(true);
    }

    private void makeImagesNevigator(boolean makeCurrentList) {
        if (isSettingValues) {
            return;
        }
        previousFiles = new ArrayList<>();
        nextFiles = new ArrayList<>();
        try {
            if (imageFileList != null && !imageFileList.isEmpty() && filesNumber > 0) {
                loadingController = openHandlingStage(Modality.WINDOW_MODAL);

                File firstFile = imageFileList.get(0);
                path = firstFile.getParentFile();
                List<File> pathFiles = new ArrayList<>();
                File[] pfiles = path.listFiles();
                if (pfiles != null) {
                    for (File file : pfiles) {
                        if (file.isFile() && FileTools.isSupportedImage(file)) {
                            pathFiles.add(file);
                        }
                    }
                    FileTools.sortFiles(pathFiles, sortMode);
                }
                totalLabel.setText("/" + pathFiles.size());

                if (makeCurrentList) {
                    imageFileList.clear();
                    int pos = pathFiles.indexOf(firstFile);
                    if (pos < 0) {
                        pos = 0;
                    }
                    int start;
                    int end;
                    if (pathFiles.size() <= filesNumber) {
                        start = 0;
                        end = pathFiles.size() - 1;
                    } else if (pos + filesNumber < pathFiles.size()) {
                        start = pos;
                        end = pos + filesNumber - 1;
                    } else {
                        start = pathFiles.size() - filesNumber;
                        end = pathFiles.size() - 1;
                    }
                    for (int i = start; i <= end; ++i) {
                        imageFileList.add(pathFiles.get(i));
                    }
                }

                if (pathFiles.size() > filesNumber) {

                    List<String> pathFnames = new ArrayList<>();
                    for (File f : pathFiles) {
                        pathFnames.add(f.getAbsolutePath());
                    }
                    List<String> iFnames = new ArrayList<>();
                    for (File f : imageFileList) {
                        iFnames.add(f.getAbsolutePath());
                    }
                    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                    for (String f : iFnames) {
                        int index = pathFnames.indexOf(f);
                        if (index < 0) {
                            continue;
                        }
                        if (index <= min) {
                            min = index;
                        }
                        if (index >= max) {
                            max = index;
                        }
                    }
                    if (max < min) {
                        min = -1;
                        max = pathFiles.size();
                    }

                    for (int i = max - 1; i >= 0; --i) {
                        String fname = pathFnames.get(i);
                        if (!iFnames.contains(fname)) {
                            previousFiles.add(0, new File(fname));
                            if (previousFiles.size() == filesNumber) {
                                break;
                            }
                        }
                    }

                    for (int i = min + 1; i < pathFnames.size(); ++i) {
                        String fname = pathFnames.get(i);
                        if (!iFnames.contains(fname)) {
                            nextFiles.add(new File(fname));
                            if (nextFiles.size() == filesNumber) {
                                break;
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        if (nextFiles.isEmpty()) {
            nextFiles = null;
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
        if (previousFiles.isEmpty()) {
            previousFiles = null;
            previousButton.setDisable(true);
        } else {
            previousButton.setDisable(false);
        }

        if (loadingController != null) {
            loadingController.closeStage();
        }
        makeImagesPane();
    }

    @FXML
    @Override
    public void selectSourcePath() {
        File defaultPath = AppVariables.getUserConfigPath(sourcePathKey);
        selectSourcePath(defaultPath);
    }

    @Override
    public void selectSourcePath(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            fileChooser.setTitle("Select multiple files");
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileOpened(files.get(0));

            loadImages(files);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void loadFiles(List<String> fileNames) {
        try {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < fileNames.size(); ++i) {
                File file = new File(fileNames.get(i));
                files.add(file);
            }
            loadImages(files);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files) {
        try {
            imageFileList.clear();
            if (files != null && !files.isEmpty()) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.exists() && file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
                filesNumber = imageFileList.size();
                colsNum = (int) Math.sqrt(filesNumber);
                colsNum = Math.max(colsNum, filesNumber / colsNum);
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<File> files, int cols) {
        try {
            imageFileList.clear();
            colsNum = cols;
            if (files != null && cols > 0) {
                for (int i = 0; i < files.size(); ++i) {
                    File file = files.get(i);
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        imageFileList.add(file);
                        if (imageFileList.size() >= maxShow) {
                            break;
                        }
                    }
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(File path, int number) {
        try {
            imageFileList.clear();
            if (path != null && path.isDirectory() && path.exists() && number > 0) {
                File[] pfiles = path.listFiles();
                if (pfiles != null) {
                    for (File file : pfiles) {
                        if (file.isFile() && FileTools.isSupportedImage(file)) {
                            imageFileList.add(file);
                            if (imageFileList.size() == number || imageFileList.size() >= maxShow) {
                                break;
                            }
                        }
                    }
                }
                if (!imageFileList.isEmpty()) {
                    filesNumber = imageFileList.size();
                    colsNum = (int) Math.sqrt(filesNumber);
                    colsNum = Math.max(colsNum, filesNumber / colsNum);
                }
            }
            loadImages();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void loadImages() {
        try {
            path = null;
            filesNumber = 0;
            currentAngle = 0;
            totalLabel.setText("");
            getMyStage().setTitle(getBaseTitle());
            if (imageFileList == null || imageFileList.isEmpty() || colsNum <= 0) {
                return;
            }
            isSettingValues = true;
            path = imageFileList.get(0).getParentFile();
            filesBox.getItems().clear();
            int total = 0;
            File[] pfiles = path.listFiles();
            if (pfiles != null) {
                for (File file : pfiles) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        total++;
                    }
                }
            }
            List<Integer> fvalues = Arrays.asList(9, 16, 12, 15, 25, 4, 3, 8, 6, 10,
                    36, 30, 24, 48);
            for (int fn : fvalues) {
                if (fn <= total) {
                    filesBox.getItems().add(fn + "");
                }
            }
            if (!filesBox.getItems().contains(total + "")) {
                if (filesBox.getItems().size() > 6) {
                    filesBox.getItems().add(6, total + "");
                } else {
                    filesBox.getItems().add(total + "");
                }
            }
            filesNumber = imageFileList.size();
            if (!filesBox.getItems().contains(filesNumber + "")) {
                filesBox.getItems().add(0, filesNumber + "");
            }
            filesBox.getSelectionModel().select(filesNumber + "");
            if (!colsnumBox.getItems().contains(colsNum + "")) {
                colsnumBox.getItems().add(0, colsNum + "");
            }
            colsnumBox.getSelectionModel().select(colsNum + "");
            isSettingValues = false;

            getMyStage().setTitle(getBaseTitle() + " " + path.getAbsolutePath());
            totalLabel.setText("/" + total);
            displayMode = DisplayMode.ImagesGrid;

            makeImagesNevigator(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public ImagesBrowserController refresh() {
        makeImagesNevigator(true);
        return this;
    }

    @FXML
    protected void filesListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(AppVariables.message("FilesList"));
    }

    @FXML
    protected void thumbsListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(AppVariables.message("ThumbnailsList"));
    }

    @FXML
    protected void gridAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select("" + (colsNum > 0 ? colsNum : 3));
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().selectAll();
        } else {
            selectedImages.clear();
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                selectedIndexes.add(i);
                selectedImages.add(tableData.get(i));
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
            }
            fileOpBox.setDisable(selectedImages.isEmpty());
            viewButton.setDisable(selectedImages.size() > 1);
            infoButton.setDisable(selectedImages.size() > 1);
            renameButton.setDisable(selectedImages.size() > 1);
        }
    }

    @FXML
    @Override
    public void selectNoneAction() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().clearSelection();
        } else {
            selectedImages.clear();
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle(null);
            }
            fileOpBox.setDisable(true);
            viewButton.setDisable(false);
            infoButton.setDisable(false);
            renameButton.setDisable(false);
        }
    }

}
