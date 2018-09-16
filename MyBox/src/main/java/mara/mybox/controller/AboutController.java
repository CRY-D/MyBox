package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.SoundTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class AboutController extends BaseController {

    @FXML
    private Label version;
    @FXML
    private Label date;
    @FXML
    private Hyperlink licenseLink, sourceLink, issuesLink, releasesLink, userGuideLink, cloudLink;

    @Override
    protected void initializeNext() {
        version.setText(CommonValues.AppVersion);
        date.setText(CommonValues.AppVersionDate);
        userGuideLink.setText("https://github.com/Mararsh/MyBox/releases/download/v"
                + CommonValues.AppVersion + "/MyBox-UserGuide-" + CommonValues.AppVersion + ".pdf");

        task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    File miao = FxmlTools.getUserFile(getClass(), "/sound/miao3.mp3", "miao3.mp3");
                    FloatControl control = SoundTools.getControl(miao);
                    Clip player = SoundTools.playback(miao, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    private void linkLicense() {
        try {
            Desktop.getDesktop().browse(new URI(licenseLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkSource() {
        try {
            Desktop.getDesktop().browse(new URI(sourceLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkIssues() {
        try {
            Desktop.getDesktop().browse(new URI(issuesLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkReleases() {
        try {
            Desktop.getDesktop().browse(new URI(releasesLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkUserGuide() {
        try {
            Desktop.getDesktop().browse(new URI(userGuideLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void linkCloud() {
        try {
            Desktop.getDesktop().browse(new URI(cloudLink.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
