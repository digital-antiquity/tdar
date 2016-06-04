package org.tdar.core.service.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * Handles the actual merging of the PDFs through a piped-output-stream.
 * 
 * @author abrin
 *
 */
public class PDFMergeTask implements Runnable {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private PDFMergeWrapper wrapper;
    private PipedOutputStream pipedOutputStream;
    private long start = System.currentTimeMillis();
    public PDFMergeTask(PDFMergeWrapper wrapper, PipedOutputStream pipedOutputStream) {
        this.wrapper = wrapper;
        this.pipedOutputStream = pipedOutputStream;
    }

    @Override
    protected void finalize() throws Throwable {
        logger.debug("download took: {}", System.currentTimeMillis() -  start);
        super.finalize();
    }
    
    @Override
    public void run() {
        try {
            wrapper.getMerger().mergeDocuments(TdarConfiguration.getInstance().getPDFMemoryWriteSetting(wrapper.getDocument()));
            wrapper.setSuccessful(true);
        } catch (IOException ioe) {
            // downgrade broken pipe exceptions
            if (isBrokenPipeException(ioe)) {
                logger.warn("broken pipe", ioe);
            } else {
                logger.error("PDF Converter Exception:", ioe);
                // if IO exception was due to encrypted document, try again without the cover page
                attemptTransferWithoutMerge(wrapper.getDocument(), pipedOutputStream);
            }
            wrapper.setFailureReason(ioe.getMessage());

        } catch (Exception e) {
            logger.error("exception when processing PDF cover page: {}", e.getMessage(), e);
            wrapper.setFailureReason(e.getMessage());
            // if some other kind of error occured during the merge, try to send without cover page.
            attemptTransferWithoutMerge(wrapper.getDocument(), pipedOutputStream);
        } finally {
            IOUtils.closeQuietly(pipedOutputStream);
        }
    }

    /**
     * Java has no built-in broken pipe exception, however, it's sometimes convenient to treat them differently from other types
     * of IO Exception (e.g. log them at different lower level, because they are inevetible in a web-serving environment).
     * 
     * @param exception
     * @return
     */
    private boolean isBrokenPipeException(IOException exception) {
        return
        // the tomcat implementation of this exception
        exception.getClass().getSimpleName().contains("ClientAbortException")
                // if not tomcat, maybe it has "pipe closed" in the error message?
                || StringUtils.contains(exception.getMessage(), "Pipe Closed");

    }

    private void attemptTransferWithoutMerge(File document, OutputStream os) {
        try {
            logger.warn("attempting to send pdf without cover page: {}", document);
            IOUtils.copyLarge(new FileInputStream(document), os);
        } catch (Exception ex) {
            logger.error("cannot attach PDF, even w/o cover page", ex);
        }
    }
}
