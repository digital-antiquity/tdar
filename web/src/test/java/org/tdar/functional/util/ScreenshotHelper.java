package org.tdar.functional.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestName;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.filestore.BaseFilestore;
import org.tdar.utils.TestConfiguration;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class ScreenshotHelper {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private long previousScreenshotSize = -1;
    private int screenidx = 0;

    public void takeScreenshot(WebDriver driver, TestName testName, String filename) {
        try {
            screenshotMultipage(driver, testName, filename);
        } catch (Throwable t) {
            logger.warn("{}", t, t);
        }
        // screenshotSinglePage(filename)
    }

    private void incrementCounter() {
        if (!TestConfiguration.getInstance().screenshotsEnabled() ||
                screenidx > TestConstants.MAX_SCREENSHOTS_PER_TEST) {
            return;
        }

        screenidx++;

    } 
    
    public void screenshotSinglePage(WebDriver driver, TestName testName, String filename) throws IOException {
        incrementCounter();
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String scrFilename = "target/screenshots/" + getClass().getSimpleName() + "/" + testName.getMethodName();
        if (scrFile != null && Objects.equals(scrFile.length(), previousScreenshotSize)) {
            logger.debug("skipping screenshot, size identical: {}", scrFilename);
            return;
        }
        previousScreenshotSize = scrFile.length();
        // Now you can do whatever you need to do with it, for example copy somewhere
        File dir = new File(scrFilename);
        dir.mkdirs();
        String finalFilename = screenshotFilename(driver, testName, filename, "png");
        logger.debug("saving screenshot: dir:{}, name:", dir, finalFilename);
        FileUtils.copyFile(scrFile, new File(dir, finalFilename));

    }

    public void screenshotMultipage(WebDriver driver, TestName testName, String filename) throws IOException {
        incrementCounter();
        
        Screenshot takeScreenshot = new AShot()
                .shootingStrategy(ShootingStrategies.scaling(0.5f))
                .takeScreenshot(driver);
        String scrFilename = "target/screenshots/" + getClass().getSimpleName() + "/" + testName.getMethodName();
        File dir = new File(scrFilename);
        dir.mkdirs();
        String finalFilename = screenshotFilename(driver, testName, filename, "png");

        File scrFile = File.createTempFile(finalFilename, ".png");
        ImageIO.write(takeScreenshot.getImage(), "png", scrFile);

        if (scrFile != null && Objects.equals(scrFile.length(), previousScreenshotSize)) {
            logger.debug("skipping screenshot, size identical: {}", scrFilename);
            return;
        }
        previousScreenshotSize = scrFile.length();
        // Now you can do whatever you need to do with it, for example copy somewhere
        logger.debug("saving screenshot: dir:{}, name:", dir, finalFilename);
        FileUtils.copyFile(scrFile, new File(dir, finalFilename));

    }

    private String screenshotFilename(WebDriver driver, TestName testName, String filename, String ext) {
        // try to use url path for title otherwise testname
        String name = null;
        try {
            URL url = new URL(driver.getCurrentUrl());
            name = url.getPath();
            if ("".equals(name) || "/".equals(name)) {
                name = "(root)";
            }
        } catch (MalformedURLException ignored) {
            name = testName.getMethodName();
        }

        if (filename != null) {
            name = filename;
        }

        String fullname = String.format("%03d-%s.%s", screenidx, BaseFilestore.sanitizeFilename(name), ext);
        return fullname;
    }

}
