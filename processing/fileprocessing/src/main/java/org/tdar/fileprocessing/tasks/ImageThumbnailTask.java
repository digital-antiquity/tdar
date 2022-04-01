/**
 * 
 */
package org.tdar.fileprocessing.tasks;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.VersionType;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * @author Adam Brin
 * 
 */
public class ImageThumbnailTask extends AbstractTask {

    private static final String UTF_8 = "UTF-8";
    private static final String _SM = "_sm";
    private static final String _MD = "_md";
    private static final String _LG = "_lg";
    private static final long serialVersionUID = -108766461810056577L;
    private static final String JPG_FILE_EXT = ".jpg";
    public static final int LARGE = 600;
    public static final int MEDIUM = 300;
    public static final int SMALL = 96;
    private transient ImagePlus ijSource;
    private boolean jaiImageJenabled = true;

    /*
     * public static void main(String[] args) {
     * ImageThumbnailTask task = new ImageThumbnailTask();
     * String baseDir = "C:\\Users\\Adam Brin\\Downloads\\";
     * String orig = "4759782488_ab3452a4eb_b.jpg";
     * WorkflowContext ctx = new WorkflowContext();
     * File origFile = new File(baseDir, orig);
     * 
     * task.setWorkflowContext(ctx);
     * 
     * InformationResourceFileVersion vers = task.generateInformationResourceFileVersion(new File(baseDir, orig), VersionType.UPLOADED);
     * ctx.getOriginalFiles().add(vers);
     * try {
     * task.run(vers);
     * } catch (Exception e) {
     * throw new TdarRecoverableRuntimeException('an image processing error ocurred', e);
     * }
     * }
     */

    @Override
    public void run() throws Exception {
        ImageIO.scanForPlugins();
        run(getWorkflowContext().getOriginalFile());
    }

    public void run(FileStoreFile version) throws Exception {
        processImage(version, version.getTransientFile());
    }

    public void processImage(FileStoreFile version, File sourceFile) {
        if ((sourceFile == null) || !sourceFile.exists()) {
            getWorkflowContext().setErrorFatal(true);
            throw new TdarRecoverableRuntimeException("error.file_not_found");
        }
        String filename = sourceFile.getName();
        getLogger().debug("sourceFile: {}", sourceFile);

        String ext = FilenameUtils.getExtension(sourceFile.getName());
        List<String> exts = Arrays.asList("jpg", "gif", "tif", "tiff", "png", "jpeg", "bmp");
        /**
         * FIXME: bad, but necessary for Geospatial images, ideally we should be smarter about divvying up these files and preventing
         * invalid files from appearing here
         **/
        if (!exts.contains(ext.toLowerCase())) {
            getLogger().error("skipping file with unmatched extension: {} ", ext);
            return;
        }

        Opener opener = new Opener();
        opener.setSilentMode(true);
        IJ.redirectErrorMessages(true);
        openImageFile(sourceFile, filename, ext, opener);
        if (getWorkflowContext().isHasDimensions()) {
            version.setHeight(ijSource.getHeight());
            version.setWidth(ijSource.getWidth());
            version.setUncompressedSizeOnDisk(ImageThumbnailTask.calculateUncompressedSize(version));
        }
        try {
            Thread.yield();
            createJpegDerivative(version, ijSource, filename, MEDIUM, false);
            Thread.yield();
            createJpegDerivative(version, ijSource, filename, LARGE, false);
            Thread.yield();
            createJpegDerivative(version, ijSource, filename, SMALL, false);
            Thread.yield();
        } catch (Throwable e) {
            getLogger().error("Failed to create ({}) jpeg derivative", sourceFile, e);
            throw new TdarRecoverableRuntimeException("imageThumbnailTask.processingError", e);
        }
    }

    private void openImageFile(File sourceFile, String filename, String ext, Opener opener) {
        ijSource = opener.openImage(sourceFile.getAbsolutePath());

        String msg = IJ.getErrorMessage();

        if (ijSource == null && StringUtils.containsIgnoreCase(ext, "tif")) {
            try {
                GeoTiffReader reader = new GeoTiffReader(sourceFile, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
                GridCoverage2D coverage = reader.read(null);
                RenderedOp renderedImage = (RenderedOp) coverage.getRenderedImage();

                ijSource = new ImagePlus("", renderedImage.getAsBufferedImage());
                msg = null;
            } catch (DataSourceException dse) {
                // we get -1 if it's not a geotiff, so ignore
                if (StringUtils.equals(dse.getMessage(), "-1")) {
                    // ignore, probably not a GeoTiff
                } else if (StringUtils.contains(dse.getMessage(), "Raster to Model Transformation is not available")) {
                    // this is likely something like a Greyscale Image
                } else {
                    getLogger().warn("issue with GeoTiff attempt to process" + sourceFile, dse);
                    msg = dse.getMessage();
                }
            } catch (IOException e) {
                getLogger().warn("issue with GeoTiff attempt to process" + sourceFile, e);
                msg = e.getMessage();
            }
        }
        if (ijSource == null) {
            getLogger().debug("Unable to load source image with ImageJ: {}, trying with ImageIO");
            try {
                BufferedImage image = ImageIO.read(sourceFile);
                ijSource = new ImagePlus("", image);
            } catch (IOException e) {
                getLogger().warn("issue with ImageIO attempt to process" + sourceFile, e);
                msg = e.getMessage();
            }
        }

        if (ijSource == null || ijSource.getProcessor() == null) {
            getLogger().debug("Unable to load source image: {} ({}) ", sourceFile, msg);
            if (msg != null && !msg.contains("Note: IJ cannot open CMYK JPEGs")) {
                getWorkflowContext().setErrorFatal(true);
                getLogger().error(msg);
            }

            throw new TdarRecoverableRuntimeException("imageThumbnailTask.fmt_error_processing_could_not_open", Arrays.asList(filename, msg));
        }
    }

    File generateFilename(String originalFilename, int resolution) {
        String outputFilename = originalFilename;
        outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));

        switch (resolution) {
            case LARGE:
                outputFilename += _LG;
                break;
            case MEDIUM:
                outputFilename += _MD;
                break;
            case SMALL:
                outputFilename += _SM;
                break;
            default:
                outputFilename += "_" + resolution;
        }
        outputFilename += JPG_FILE_EXT;
        try {
            outputFilename = URLEncoder.encode(outputFilename, UTF_8);
        } catch (Exception e) {
            getLogger().debug("exception writing derivative image:", e);
        }
        File outputPath = new File(getWorkflowContext().getWorkingDirectory(), outputFilename);

        return outputPath;
    }

    protected float getScalingRatio(int sourceWidth, int sourceHeight, int resolution) {
        float ratio;
        if (sourceWidth >= sourceHeight) {
            ratio = (float) resolution / (float) sourceWidth;
        } else {
            ratio = (float) resolution / (float) sourceHeight;
        }

        return ratio;
    }

    protected void createJpegDerivative(FileStoreFile originalVersion, ImagePlus ijSource, String origFileName, int resolution,
            boolean canSwitchSource)
            throws Throwable {
        File outputFile = generateFilename(origFileName, resolution);

        VersionType type = null;
        switch (resolution) {
            case LARGE:
                type = VersionType.WEB_LARGE;
                break;
            case MEDIUM:
                type = VersionType.WEB_MEDIUM;
                break;
            case SMALL:
                type = VersionType.WEB_SMALL;
                break;
        }

        ImageProcessor ip = ijSource.getProcessor();
        int sourceWidth = ip.getWidth();
        int sourceHeight = ip.getHeight();
        float scalingRatio = getScalingRatio(sourceWidth, sourceHeight, resolution);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            getLogger().debug("exception writing derivative image:", e);
        }

        try {
            if (scalingRatio > 1.0f) {
                ip = averageReductionScale(ip, ijSource, (sourceWidth >= sourceHeight ? sourceWidth : sourceHeight));
            } else if (scalingRatio != 1.0f) {
                // First interpolate to this size, then use the new image for scaling to all the smaller sizes
                ip.setInterpolate(true);
                ip.smooth();
                ip = ip.resize(Math.round(ip.getWidth() * scalingRatio),
                        Math.round(ip.getHeight() * scalingRatio));
            } else {
                ip = averageReductionScale(ip, ijSource, resolution);
            }

            // ip is now a scaled image processor
            if (resolution != SMALL) {
                ip.sharpen();
            }

            // Get the destination width and height.
            int destWidth = ip.getWidth();
            int destHeight = ip.getHeight();

            BufferedImage bImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
            Graphics g = bImage.createGraphics();
            g.drawImage(ip.createImage(), 0, 0, null);
            g.dispose();

            ip = null;

            // evaluate http://www.universalwebservices.net/web-programming-resources/java/adjust-jpeg-image-compression-quality-when-saving-images-in-java
            // at a later date to see about compression needs
            ImageIO.write(bImage, "jpg", outputFile);
            bImage.flush();
            bImage = null;
            FileStoreFile version = generateInformationResourceFileVersionFromOriginal(originalVersion, outputFile, type);
            version.setHeight(destHeight);
            version.setWidth(destWidth);
            version.setUncompressedSizeOnDisk(ImageThumbnailTask.calculateUncompressedSize(version));
            getWorkflowContext().addVersion(version);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    getLogger().debug("generateImagesIj(): IOException closing FileOutputStream: ", ioe);
                }
            }
        }
    }

    private static Long calculateUncompressedSize(FileStoreFile version) {
        try {
            return version.getHeight() * version.getWidth() * 3L * 8L;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ImageProcessor averageReductionScale(ImageProcessor ip, ImagePlus source, int res) {
        int factor = (ip.getWidth() > ip.getHeight() ? ip.getWidth() : ip.getHeight());
        double dxshrink = (double) factor / (double) res;
        getLogger().trace("factor: " + factor + " res:" + res);
        int xshrink = (int) dxshrink;
        getLogger().trace("\t\txshrink:" + xshrink);
        getLogger().trace("\t\tdxshrink:" + dxshrink);
        double product = xshrink * xshrink;
        int samples;

        if (source.getBitDepth() == 32) {
            return null;
        }

        samples = (ip instanceof ColorProcessor) ? 3 : 1;
        int w = (int) (ip.getWidth() / dxshrink);
        int h = (int) (ip.getHeight() / dxshrink);
        ImageProcessor ip2 = ip.createProcessor(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ip2.putPixel(x, y, getAverage(ip, x, y, xshrink, product, samples));
            }
        }
        ip2.resetMinAndMax();
        getLogger().trace("\twidth:" + ip.getWidth() + " ->" + ip2.getWidth());
        getLogger().trace("\theight:" + ip.getHeight() + " ->" + ip2.getHeight());
        return ip2;
    }

    private int[] getAverage(ImageProcessor ip, int x, int y, int xshrink, double product, int samples) {
        int[] sum = new int[samples];
        int[] pixel = new int[3];

        for (int i = 0; i < samples; i++) {
            sum[i] = 0;
        }

        for (int y2 = 0; y2 < xshrink; y2++) {
            for (int x2 = 0; x2 < xshrink; x2++) {
                pixel = ip.getPixel((x * xshrink) + x2, (y * xshrink) + y2, pixel);
                for (int i = 0; i < samples; i++) {
                    sum[i] += pixel[i];
                }
            }
        }
        for (int i = 0; i < samples; i++) {
            sum[i] = (int) ((sum[i] / product) + 0.5d);
        }
        return sum;
    }

    @Override
    public String getName() {
        return "ImageThumbnailGenerator";
    }

    public boolean isJaiImageJenabled() {
        if (TdarConfiguration.getInstance().isJaiImageJenabled()) {
            return jaiImageJenabled;
        }
        return false;
    }

    public void setJaiImageJenabled(boolean jaiImageJenabled) {
        this.jaiImageJenabled = jaiImageJenabled;
    }

}
