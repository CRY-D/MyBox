package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.image.ImageInformation;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-31
 * @License Apache License Version 2.0
 */
public class ImageTooLargeController extends BaseController {

    protected BaseImageController parent;
    protected ImageInformation imageInfo;

    @FXML
    protected Label infoLabel;

    public ImageTooLargeController() {
    }

    @Override
    public void initControls() {
        super.initControls();

    }

    public void setValues(BaseImageController parent, ImageInformation imageInfo) {
        if (parent == null || imageInfo == null) {
            return;
        }
        this.parent = parent;
        this.imageInfo = imageInfo;
        infoLabel.setText(imageInfo.sampleInformation(null));
    }

    @FXML
    public void sample() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageSample")) {
            parent.loadImage(imageInfo);
            closeStage();
        } else {
            ImageSampleController controller = (ImageSampleController) loadScene(CommonValues.ImageSampleFxml);
            controller.loadImage(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void split() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageSplit")) {
            parent.loadImage(imageInfo);
        } else {
            closeStage();
            ImageSplitController controller = (ImageSplitController) loadScene(CommonValues.ImageSplitFxml);
            controller.loadImage(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void mem() {
        thisPane.setDisable(true);
        SettingsController controller = (SettingsController) loadScene(CommonValues.SettingsFxml);
        controller.tabPane.getSelectionModel().select(controller.baseTab);
        closeStage();
    }

    @FXML
    public void view() {
        thisPane.setDisable(true);
        if (parent.baseName.equals("ImageViewer")) {
            parent.loadImage(imageInfo);
            closeStage();
        } else {
            ImageViewerController controller = (ImageViewerController) loadScene(CommonValues.ImageViewerFxml);
            controller.loadImage(imageInfo);
            parent.closeStage();
            closeStage();
        }
    }

    @FXML
    public void cancel() {
        closeStage();
    }

}
