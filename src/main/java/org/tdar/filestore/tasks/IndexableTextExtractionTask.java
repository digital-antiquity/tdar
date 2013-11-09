/**
 * 
 */
package org.tdar.filestore.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.service.workflow.workflows.FileArchiveWorkflow;
import org.tdar.filestore.tasks.Task.AbstractTask;
import org.tdar.utils.ExceptionWrapper;

/**
 * @author Adam Brin
 *         NOTE: this class should not be used without more testing ... it has issues with including POI 3.8
 *         calls which is in BETA but we're on 3.7. Also, it seems to kill some tests
 */
public class IndexableTextExtractionTask extends AbstractTask {

    private static final long serialVersionUID = -5207578211297342261L;
    public static final String GPS_MESSAGE = "The image you uploaded appears to have GPS Coordinate in it, please make sure you mark it as confidential if the exact location data is important to protect (%s)";

    @Override
    public void run() throws Exception {
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            run(version);
        }
    }

    public void run(InformationResourceFileVersion version) throws Exception {
        File file = version.getTransientFile();
        FileOutputStream metadataOutputStream = null;
        InputStream stream = null;
        String filename = file.getName();
        File metadataFile = new File(getWorkflowContext().getWorkingDirectory(), filename + ".metadata");
        try {
            InputStream input = new FileInputStream(file);
            Tika tika = new Tika();
            Metadata metadata = new Metadata();
            String mimeType = tika.detect(input);
            metadata.set(Metadata.CONTENT_TYPE, mimeType);

            Parser parser = new AutoDetectParser();
            ParseContext parseContext = new ParseContext();
            
            metadataFile = new File(getWorkflowContext().getWorkingDirectory(), filename + ".metadata");
            metadataOutputStream = new FileOutputStream(metadataFile);
            if (!FileArchiveWorkflow.ARCHIVE_EXTENSIONS_SUPPORTED.contains(FilenameUtils.getExtension(filename).toLowerCase())) {
                File indexFile = new File(getWorkflowContext().getWorkingDirectory(), filename + ".index.txt");
                FileOutputStream indexOutputStream = new FileOutputStream(indexFile);
                BufferedOutputStream indexedFileOutputStream = new BufferedOutputStream(indexOutputStream);
                BodyContentHandler handler = new BodyContentHandler(indexedFileOutputStream);
                //If we're dealing with a zip, read only the beginning of the file
                    stream = new FileInputStream(file);

                parser.parse(stream, handler, metadata, parseContext);
                IOUtils.closeQuietly(indexedFileOutputStream);
                if (indexFile.length() > 0) {
                    addDerivativeFile(version, indexFile, VersionType.INDEXABLE_TEXT);
                }
            }

            List<String> gpsValues = new ArrayList<>();

            for (String name : metadata.names()) {
                StringWriter sw = new StringWriter();
                if (StringUtils.isNotBlank(metadata.get(name))) {
                    sw.append(name).append(":");
                    if (name.matches("(?i).*(latitude|longitude|gps).*")) {
                        gpsValues.add(name);
                    }
                    if (metadata.isMultiValued(name)) {
                        sw.append(StringUtils.join(metadata.getValues(name), "|"));
                    } else {
                        sw.append(metadata.get(name));
                    }
                    sw.append("\r\n");
                    IOUtils.write(sw.toString(), metadataOutputStream);
                }
            }
            if (CollectionUtils.isNotEmpty(gpsValues)) {
                getWorkflowContext().getExceptions().add(new ExceptionWrapper(String.format(GPS_MESSAGE, gpsValues), null));
            }
        } catch (Throwable t) {
            // Marking this as a "warn" as it's a derivative
            getLogger().warn("a tika indexing exception happend ", t);
        } finally {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(metadataOutputStream);
        }

        if (metadataFile != null && metadataFile.exists() && metadataFile.length() > 0) {
            addDerivativeFile(version, metadataFile, VersionType.METADATA);

        }
    }

    @Override
    public String getName() {
        return "IndexableTextExtractionTask";
    }

}
