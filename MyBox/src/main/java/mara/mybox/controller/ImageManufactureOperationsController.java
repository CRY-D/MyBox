package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationsController extends BaseImageController {

    protected ImageManufactureController imageController;

    protected ImageManufactureViewController viewController;
    protected ImageManufactureCopyController copyController;
    protected ImageManufactureClipboardController clipboardController;
    protected ImageManufactureCropController cropController;
    protected ImageManufactureScaleController scaleController;
    protected ImageManufactureColorController colorController;
    protected ImageManufactureEffectsController effectController;
    protected ImageManufactureEnhancementController enhancementController;
    protected ImageManufactureRichTextController richTextController;
    protected ImageManufactureTextController textController;
    protected ImageManufacturePenController penController;
    protected ImageManufactureTransformController transformController;
    protected ImageManufactureArcController arcController;
    protected ImageManufactureShadowController shadowController;
    protected ImageManufactureMarginsController marginsController;

    protected ImageManufactureOperationController currentController;

    @FXML
    protected Accordion accordionPane;
    @FXML
    protected TitledPane viewPane, copyPane, clipboardPane, cropPane, scalePane, colorPane,
            effectPane, enhancementPane, transformPane, shadowPane,
            marginsPane, arcPane, penPane, textPane, richTextPane;

    public ImageManufactureOperationsController() {
        baseTitle = AppVariables.message("ImageManufacture");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            accordionPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
                @Override
                public void changed(ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n) {
                    checkPaneStatus();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void checkPaneStatus() {
        try {
            TitledPane currentPane = accordionPane.getExpandedPane();
            if (imageController == null || currentPane == null) {
                return;
            }
            imageController.showRightPane();
            imageController.resetImagePane();
            ImageManufactureOperationController controller;
            if (currentPane.equals(viewPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureViewFxml);
                if (controller != null) {
                    viewController = (ImageManufactureViewController) controller;
                }
                viewController.paneExpanded();
                currentController = viewController;
            } else if (currentPane.equals(copyPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureCopyFxml);
                if (controller != null) {
                    copyController = (ImageManufactureCopyController) controller;
                }
                copyController.paneExpanded();
                currentController = copyController;
            } else if (currentPane.equals(clipboardPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureClipboardFxml);
                if (controller != null) {
                    clipboardController = (ImageManufactureClipboardController) controller;
                }
                clipboardController.paneExpanded();
                currentController = clipboardController;
            } else if (currentPane.equals(cropPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureCropFxml);
                if (controller != null) {
                    cropController = (ImageManufactureCropController) controller;
                }
                cropController.paneExpanded();
                currentController = cropController;
            } else if (currentPane.equals(colorPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureColorFxml);
                if (controller != null) {
                    colorController = (ImageManufactureColorController) controller;
                }
                colorController.paneExpanded();
                currentController = colorController;
            } else if (currentPane.equals(effectPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureEffectsFxml);
                if (controller != null) {
                    effectController = (ImageManufactureEffectsController) controller;
                }
                effectController.paneExpanded();
                currentController = effectController;
            } else if (currentPane.equals(enhancementPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureEnhancementFxml);
                if (controller != null) {
                    enhancementController = (ImageManufactureEnhancementController) controller;
                }
                enhancementController.paneExpanded();
                currentController = enhancementController;
            } else if (currentPane.equals(scalePane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureScaleFxml);
                if (controller != null) {
                    scaleController = (ImageManufactureScaleController) controller;
                }
                scaleController.paneExpanded();
                currentController = scaleController;
            } else if (currentPane.equals(transformPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureTransformFxml);
                if (controller != null) {
                    transformController = (ImageManufactureTransformController) controller;
                }
                transformController.paneExpanded();
                currentController = transformController;
            } else if (currentPane.equals(shadowPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureShadowFxml);
                if (controller != null) {
                    shadowController = (ImageManufactureShadowController) controller;
                }
                shadowController.paneExpanded();
                currentController = shadowController;
            } else if (currentPane.equals(marginsPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureMarginsFxml);
                if (controller != null) {
                    marginsController = (ImageManufactureMarginsController) controller;
                }
                marginsController.paneExpanded();
                currentController = marginsController;
            } else if (currentPane.equals(arcPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureArcFxml);
                if (controller != null) {
                    arcController = (ImageManufactureArcController) controller;
                }
                arcController.paneExpanded();
                currentController = arcController;
            } else if (currentPane.equals(penPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufacturePenFxml);
                if (controller != null) {
                    penController = (ImageManufacturePenController) controller;
                }
                penController.paneExpanded();
                currentController = penController;
            } else if (currentPane.equals(textPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureTextFxml);
                if (controller != null) {
                    textController = (ImageManufactureTextController) controller;
                }
                textController.paneExpanded();
                currentController = textController;
            } else if (currentPane.equals(richTextPane)) {
                controller = checkPaneStatus(currentPane, CommonValues.ImageManufactureRichTextFxml);
                if (controller != null) {
                    richTextController = (ImageManufactureRichTextController) controller;
                }
                richTextController.paneExpanded();
                currentController = richTextController;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected ImageManufactureOperationController checkPaneStatus(TitledPane titledPane, String fxml) {
        try {
            if (titledPane.getContent() == null) {
                ImageManufactureOperationController controller
                        = (ImageManufactureOperationController) FxmlStage.setScene(fxml);
                titledPane.setContent(controller.getMyScene().getRoot());
                controller.imageController = imageController;
                controller.scopeController = imageController.scopeController;
                controller.operationsController = this;
                controller.imageView = imageController.imageView;
                controller.maskView = imageController.maskView;
                controller.baseName = imageController.baseName;
                controller.baseTitle = imageController.baseTitle;
                controller.initPane();
                controller.refreshStyle();
                return controller;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return null;
    }

    public void resetOperationPanes() {
        if (currentController == null) {
            return;
        }
        if (viewController == currentController) {
            viewController.resetOperationPane();
        } else if (copyController == currentController) {
            copyController.resetOperationPane();
        } else if (clipboardController == currentController) {
            clipboardController.resetOperationPane();
        } else if (cropController == currentController) {
            cropController.resetOperationPane();
        } else if (scaleController == currentController) {
            scaleController.resetOperationPane();
        } else if (colorController == currentController) {
            colorController.resetOperationPane();
        } else if (enhancementController == currentController) {
            enhancementController.resetOperationPane();
        } else if (richTextController == currentController) {
            richTextController.resetOperationPane();
        } else if (textController == currentController) {
            textController.resetOperationPane();
        } else if (penController == currentController) {
            penController.resetOperationPane();
        } else if (transformController == currentController) {
            transformController.resetOperationPane();
        } else if (arcController == currentController) {
            arcController.resetOperationPane();
        } else if (shadowController == currentController) {
            shadowController.resetOperationPane();
        } else if (marginsController == currentController) {
            marginsController.resetOperationPane();
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (currentController != null) {
            currentController.keyEventsHandler(event);
        }
    }

    @Override
    public BaseImageController refresh() {
        return null;  //Bypass since this is part of frame
    }

    /*
        events passed from image pane
     */
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (currentController != null) {
            currentController.imageClicked(event, p);
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (currentController != null) {
            currentController.mousePressed(event);
        }
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (currentController != null) {
            currentController.mouseDragged(event);
        }
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        if (currentController != null) {
            currentController.mouseReleased(event);
        }
    }

}
