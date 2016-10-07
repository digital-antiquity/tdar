/**
 * 
 */
package org.tdar.filestore.tasks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;

/**
 * @author Adam Brin
 */

public class PDFDerivativeTask extends ImageThumbnailTask {

    private static final long serialVersionUID = -1138753863662695849L;

    public static void main(String[] args) {
        PDFDerivativeTask task = new PDFDerivativeTask();
        String baseDir = "C:\\Users\\abrin\\Desktop\\";
        String orig = "SCIDD_Storage_Basin_Phase_2DR_redacted_pages.pdf";
        File origFile = new File(baseDir, orig);
        WorkflowContext ctx = new WorkflowContext();
        task.setWorkflowContext(ctx);
        InformationResourceFileVersion vers = new InformationResourceFileVersion(VersionType.UPLOADED, origFile.getName(), 1, -1L, -1L);
        ctx.getOriginalFiles().add(vers);
        try {
            task.run(vers);
        } catch (Throwable e) {
            throw new TdarRecoverableRuntimeException("pdfDerivativeTask.processing_error", e);
        }
    }


    @Override
    public void run() throws Exception {
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            run(version);
        }
    }

    @Override
    public void run(InformationResourceFileVersion version) throws Exception {
        File originalFile = version.getTransientFile();
        try {
            PDDocument document = openPDF("", originalFile);
            File imageFile = new File(extractPage(1, version, document));
            getLogger().warn("output file is: {} {}", imageFile, imageFile.length());
            // extractText(originalFile, document);
            closePDF(document);
            if (imageFile.exists()) {
                if (imageFile.length() > 2) {
                    processImage(version, imageFile);
                } else {
                    try {
                        imageFile.delete();
                    } catch (Exception e) {
                        getLogger().debug("could not delete image file that was missprocessed");
                    }
                }
            }
        } catch (Throwable t) {
            throw new TdarRecoverableRuntimeException("pdfDerivativeTask.processing_error", t);
        }
    }

    protected String extractPage(int pageNum, InformationResourceFileVersion originalFile, PDDocument document) {
        // File pdfFile = new File(sourceFile);
        @SuppressWarnings("unused")
        File pdfFile = originalFile.getTransientFile();
        String imageFormat = "jpg";
        ImageType color = ImageType.RGB;

        String fn = originalFile.getFilename();
        String outputPrefix = fn.substring(0, fn.lastIndexOf('.'));
        outputPrefix = new File(getWorkflowContext().getWorkingDirectory(), outputPrefix).toString();
        String outputFilename = outputPrefix + pageNum + "." + imageFormat;
        File outputFile = new File(outputFilename);
        outputFile.deleteOnExit();

        if (document != null) {
//            ImageType imageType = determineImageType(color);

            try {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageNum, 300, color);
                boolean success = ImageIOUtil.writeImage(bim, imageFormat, new FileOutputStream(outputFile), 300);

                if (!success) {
                    getLogger().info("Error: no writer found for image format '" + imageFormat + "'");
                }
                getLogger().debug("output file is: {} {}", outputFile, outputFile.length());
                // if (outputFile.exists() && outputFile.length() < 50) {
                //
                // }
            } catch (Throwable e) {
                getLogger().debug("PDF image extraction failed", e);
            }
        }

        return outputFilename;
    }

    private void closePDF(PDDocument document) {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                getLogger().warn("cannot close PDF", e);
            } finally {
            }
        }
    }

    private PDDocument openPDF(String password, File pdfFile) {
        PDDocument document = null;
        try {
            document = PDDocument.load(pdfFile, TdarConfiguration.getInstance().getPDFMemoryReadSetting());

//            if (document.isEncrypted()) {
//                getLogger().info("access permissions: " + document.getCurrentAccessPermission());
//                getLogger().info("security manager: " + document.getCurrentAccessPermission());
//            }

            // try {
            // document.decrypt(password);
            // } catch (InvalidPasswordException e) {
            // getLogger().debug("Error: The document is encrypted.");
            // }
            // }
            getWorkflowContext().setNumPages(document.getNumberOfPages());
        } catch (InvalidPasswordException ipe) {
            getWorkflowContext().setErrorFatal(true);
            throw new TdarRecoverableRuntimeException("pdfDerivativeTask.encryption_warning");
        } catch (IOException e) {
            getWorkflowContext().setErrorFatal(true);
            getLogger().info("IO Exception ocurred", e);
            // } catch (CryptographyException ce) {
            // getLogger().info(ce);
            // ce.printStackTrace();
        }
        return document;
    }

//    private int determineImageType(ImageType color) {
//        int imageType = 24;
//        ImageType.valueOf(name)
//        if ("bilevel".equalsIgnoreCase(color)) {
//            imageType = BufferedImage.TYPE_BYTE_BINARY;
//        } else if ("indexed".equalsIgnoreCase(color)) {
//            imageType = BufferedImage.TYPE_BYTE_INDEXED;
//        } else if ("gray".equalsIgnoreCase(color)) {
//            imageType = BufferedImage.TYPE_BYTE_GRAY;
//        } else if ("rgb".equalsIgnoreCase(color)) {
//            imageType = BufferedImage.TYPE_INT_RGB;
//        } else if ("rgba".equalsIgnoreCase(color)) {
//            imageType = BufferedImage.TYPE_INT_ARGB;
//        } else {
//            getLogger().debug("Error: the number of bits per pixel must be 1, 8 or 24.");
//        }
//        return imageType;
//    }

    @Override
    public String getName() {
        return "PDFDerivativeTask";
    }

}
