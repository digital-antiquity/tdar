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
import org.tdar.filestore.tasks.Task.AbstractTask;

/**
 * @author Adam Brin
 *         NOTE: this class should not be used without more testing ... it has issues with including POI 3.8
 *         calls which is in BETA but we're on 3.7. Also, it seems to kill some tests
 */
public class IndexableTextExtractionTask extends AbstractTask {

    private static final long serialVersionUID = -5207578211297342261L;

    @Override
    public void run() throws Exception {
        for (InformationResourceFileVersion version : getWorkflowContext().getOriginalFiles()) {
            run(version);
        }
    }

    public void run(InformationResourceFileVersion version) throws Exception {
        File file = version.getFile();
        FileOutputStream metadataOutputStream = null;
        FileInputStream stream = null;
        File metadataFile = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + ".metadata");
        try {
            InputStream input = new FileInputStream(file);
            Tika tika = new Tika();
            Metadata metadata = new Metadata();
            String mimeType = tika.detect(input);
            metadata.set(Metadata.CONTENT_TYPE, mimeType);

            Parser parser = new AutoDetectParser();
            ParseContext parseContext = new ParseContext();

            File indexFile = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + ".index.txt");
            metadataFile = new File(getWorkflowContext().getWorkingDirectory(), file.getName() + ".metadata");
            FileOutputStream indexOutputStream = new FileOutputStream(indexFile);
            BufferedOutputStream indexedFileOutputStream = new BufferedOutputStream(indexOutputStream);
            metadataOutputStream = new FileOutputStream(metadataFile);
            BodyContentHandler handler = new BodyContentHandler(indexedFileOutputStream);
            stream = new FileInputStream(file);
            parser.parse(stream, handler, metadata, parseContext);
            IOUtils.closeQuietly(indexedFileOutputStream);
            if (indexFile.length() > 0) {
                addDerivativeFile(version, indexFile, VersionType.INDEXABLE_TEXT);
            }

            for (String name : metadata.names()) {
                StringWriter sw = new StringWriter();
                if (StringUtils.isNotBlank(metadata.get(name))) {
                    sw.append(name).append(":");
                    if (metadata.isMultiValued(name)) {
                        sw.append(StringUtils.join(metadata.getValues(name), "|"));
                    } else {
                        sw.append(metadata.get(name));
                    }
                    sw.append("\r\n");
                    IOUtils.write(sw.toString(), metadataOutputStream);
                }
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
