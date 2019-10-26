package mara.mybox.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BytesEditerController;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.ImageInformationController;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.controller.ImageMetaDataController;
import mara.mybox.controller.ImageStatisticController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.controller.ImagesBrowserController;
import mara.mybox.controller.LoadingController;
import mara.mybox.controller.MarkdownEditerController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.TextEditerController;
import mara.mybox.data.VisitHistory;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonImageValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-1-27 21:48:55
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlStage {

    public static BaseController initScene(final Stage stage, final String newFxml,
            StageStyle stageStyle) {
        try {
            if (stage == null) {
                return null;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(newFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            pane.getStylesheets().add(FxmlStage.class.getResource(AppVariables.getStyle()).toExternalForm());
            Scene scene = new Scene(pane);

            final BaseController controller = (BaseController) fxmlLoader.getController();
            controller.setMyStage(stage);
            controller.setMyScene(scene);
            controller.setLoadFxml(newFxml);

//            stage.setUserData(controller);
            stage.getIcons().add(CommonImageValues.AppIcon);
            stage.setTitle(controller.getBaseTitle());
            if (stageStyle != null) {
                stage.initStyle(stageStyle);
            }
            stage.setOnShown(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    controller.afterStageShown();
                }
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.leavingScene()) {
                        event.consume();
                    } else {
                        FxmlStage.closeStage(stage);
                    }
                }
            });

            stage.setScene(scene);
            stage.show();
            controller.afterSceneLoaded();

            if (controller.getMainMenuController() != null && !newFxml.equals(CommonValues.LoadingFxml)) {
                VisitHistory.visitMenu(controller.getBaseTitle(), newFxml);
            }

            Platform.setImplicitExit(AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty());

            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Stage myStage,
            String newFxml, boolean isOwned, Modality modality, StageStyle stageStyle) {
        try {
            Stage stage = new Stage();
            stage.initModality(modality);
            if (isOwned && myStage != null) {
                stage.initOwner(myStage);
            } else {
                stage.initOwner(null);
            }
            return initScene(stage, newFxml, stageStyle);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openStage(Stage myStage,
            String newFxml, boolean isOwned, Modality modality) {
        return openStage(myStage, newFxml, isOwned, modality, null);

    }

    public static BaseController openStage(Stage myStage,
            String newFxml, boolean isOwned) {
        return openStage(myStage, newFxml, isOwned, Modality.NONE);
    }

    public static BaseController openStage(Stage myStage,
            String newFxml) {
        return openStage(myStage, newFxml, false, Modality.NONE);
    }

    public static BaseController openStage(String newFxml) {
        return openStage(null, newFxml, false, Modality.NONE);
    }

    public static BaseController openScene(Stage stage, String newFxml, StageStyle stageStyle) {
        try {
            Stage newStage = new Stage();  // new stage should be opened instead of keeping old stage, to clean resources
            newStage.initModality(Modality.NONE);
            newStage.initStyle(StageStyle.DECORATED);
            newStage.initOwner(null);
            BaseController controller = initScene(newStage, newFxml, stageStyle);

            if (stage != null) {
                closeStage(stage);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BaseController openScene(Stage stage, String newFxml) {
        return openScene(stage, newFxml, null);
    }

    public static void closeStage(Stage stage) {
        try {
            stage.close();
            if (Window.getWindows().isEmpty()) {
                appExit();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void appExit() {
        try {
            if (Window.getWindows() != null) {
                List<Window> windows = new ArrayList<>();
                windows.addAll(Window.getWindows());
                for (Window window : windows) {
                    window.hide();
                }
            }
            if (AppVariables.scheduledTasks != null && !AppVariables.scheduledTasks.isEmpty()) {
                if (AppVariables.getUserConfigBoolean("StopAlarmsWhenExit")) {
                    for (Long key : AppVariables.scheduledTasks.keySet()) {
                        ScheduledFuture future = AppVariables.scheduledTasks.get(key);
                        future.cancel(true);
                    }
                    AppVariables.scheduledTasks = null;
                    if (AppVariables.executorService != null) {
                        AppVariables.executorService.shutdownNow();
                        AppVariables.executorService = null;
                    }
                }

            } else {
                if (AppVariables.scheduledTasks != null) {
                    AppVariables.scheduledTasks = null;
                }
                if (AppVariables.executorService != null) {
                    AppVariables.executorService.shutdownNow();
                    AppVariables.executorService = null;
                }
            }

            if (AppVariables.scheduledTasks == null || AppVariables.scheduledTasks.isEmpty()) {
                Platform.exit(); // Some thread may still be alive after this
                System.exit(0);  // Go
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public static BaseController openMyBox(Stage stage) {
        return openScene(stage, CommonValues.MyboxFxml);
    }

    public static PdfViewController openPdfViewer(Stage stage, File file) {
        try {
            final PdfViewController controller
                    = (PdfViewController) openScene(stage, CommonValues.PdfViewFxml);
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, String link) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.switchBroswerTab();
            controller.loadLink(link);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static HtmlEditorController openHtmlEditor(Stage stage, File file) {
        try {
            final HtmlEditorController controller
                    = (HtmlEditorController) openScene(stage, CommonValues.HtmlEditorFxml);
            controller.switchBroswerTab();
            controller.sourceFileChanged(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage, File file) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(file.getAbsolutePath());
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            File file, Image image, ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(file, image, imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            ImageInformation imageInfo) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(imageInfo);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageManufactureController openImageManufacture(Stage stage,
            Image image) {
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) openScene(stage, CommonValues.ImageManufactureFxml);
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage) {
        try {
            final ImageViewerController controller
                    = (ImageViewerController) openScene(stage, CommonValues.ImageViewerFxml);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageViewerController openImageViewer(Stage stage, File file) {
        try {
            final ImageViewerController controller = openImageViewer(stage);
            if (controller != null) {
                controller.loadImage(file.getAbsolutePath());
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImagesBrowserController openImagesBrowser(Stage stage) {
        try {
            final ImagesBrowserController controller = (ImagesBrowserController) openScene(stage,
                    CommonValues.ImagesBrowserFxml);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static TextEditerController openTextEditer(Stage stage, File file) {
        try {
            final TextEditerController controller
                    = (TextEditerController) openScene(stage, CommonValues.TextEditerFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static BytesEditerController openBytesEditer(Stage stage, File file) {
        try {
            final BytesEditerController controller
                    = (BytesEditerController) openScene(stage, CommonValues.BytesEditerFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static MarkdownEditerController openMarkdownEditer(Stage stage, File file) {
        try {
            final MarkdownEditerController controller
                    = (MarkdownEditerController) openScene(stage, CommonValues.MarkdownEditorFxml);
            controller.openFile(file);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageInformationController openImageInformation(Stage stage, ImageInformation info) {
        try {
            if (info == null) {
                return null;
            }
            final ImageInformationController controller = (ImageInformationController) openScene(stage,
                    CommonValues.ImageInformationFxml);
            controller.loadImageFileInformation(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageMetaDataController openImageMetaData(Stage stage, ImageInformation info) {
        try {
            final ImageMetaDataController controller = (ImageMetaDataController) openScene(stage,
                    CommonValues.ImageMetaDataFxml);
            controller.loadImageFileMeta(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageStatisticController openImageStatistic(Stage stage, ImageInformation info) {
        try {
            final ImageStatisticController controller = (ImageStatisticController) openScene(stage,
                    CommonValues.ImageStatisticFxml);
            controller.loadImage(info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static ImageStatisticController openImageStatistic(Stage stage, Image image) {
        try {
            final ImageStatisticController controller = (ImageStatisticController) openScene(stage,
                    CommonValues.ImageStatisticFxml);
            controller.loadImage(image);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static LoadingController openLoadingStage(Stage stage, Modality block, String info) {
        return openLoadingStage(stage, block, null, info);
    }

    public static LoadingController openLoadingStage(Stage stage, Modality block, Task task, String info) {
        try {
            final LoadingController controller
                    = (LoadingController) FxmlStage.openStage(stage, CommonValues.LoadingFxml,
                            true, block, StageStyle.TRANSPARENT);
            controller.init(task);
            if (info != null) {
                controller.setInfo(info);
            }
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    public static BaseController openTarget(Stage stage, String filename) {
        return openTarget(stage, filename, true);
    }

    public static BaseController openTarget(Stage stage,
            String filename, boolean mustOpen) {
        BaseController controller = null;
        if (filename == null) {
            return controller;
        }
        if (filename.startsWith("http") || filename.startsWith("ftp")) {
            return openHtmlEditor(null, filename);
        }

        File file = new File(filename);
        if (!file.exists()) {
            return controller;
        }
        if (file.isDirectory()) {
            SystemTools.browseURI(file.toURI());
            return controller;
        }

        String suffix = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (CommonValues.SupportedImages.contains(suffix)) {
            controller = openImageViewer(stage, file);

        } else if ("html".equals(suffix) || "htm".equals(suffix)) {
            controller = openHtmlEditor(stage, file);

        } else if ("md".equals(suffix)) {
            controller = openMarkdownEditer(stage, file);

        } else if (Arrays.asList(CommonValues.TextFileSuffix).contains(suffix)) {
            controller = openTextEditer(stage, file);

        } else if ("pdf".equals(suffix)) {
            controller = openPdfViewer(stage, file);

        } else if (mustOpen) {
            controller = openBytesEditer(stage, file);
            //            try {
//               browseURI(file.toURI());
//            } catch (Exception e) {
//            }
        }
        return controller;
    }

    public static void alertError(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            // https://stackoverflow.com/questions/38799220/javafx-how-to-bring-dialog-alert-to-the-front-of-the-screen?r=SearchResults
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void alertWarning(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.setContentText(information);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public static void alertInformation(Stage myStage, String information) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(myStage.getTitle());
            alert.setHeaderText(null);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setContent(new Label(information));
//            alert.setContentText(information);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            alert.showAndWait();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
