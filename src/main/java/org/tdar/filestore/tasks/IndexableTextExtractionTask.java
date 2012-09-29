/**
 * 
 */
package org.tdar.filestore.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.tasks.Task.AbstractTask;

/**
 * @author Adam Brin
 *         NOTE: this class should not be used without more testing ... it has issues with including POI 3.8
 *         calls which is in BETA but we're on 3.7. Also, it seems to kill some tests
 */
public class IndexableTextExtractionTask extends AbstractTask {

    public static void main(String[] args) {
        IndexableTextExtractionTask task = new IndexableTextExtractionTask();
        String baseDir = "C:\\Users\\Adam Brin\\Downloads\\";
        String orig = "4759782488_ab3452a4eb_b.jpg";
        WorkflowContext ctx = new WorkflowContext();
        File origFile = new File(baseDir, orig);

        task.setWorkflowContext(ctx);

        InformationResourceFileVersion vers = task.generateInformationResourceFileVersion(new File(baseDir, orig), VersionType.UPLOADED);
        ctx.setOriginalFile(vers);
        try {
            task.run(origFile);
        } catch (Exception e) {
            throw new TdarRecoverableRuntimeException("processing error", e);
        }
        String outXML = task.getWorkflowContext().toXML();
        System.out.println(outXML);
    }

    @Override
    public void run() throws Exception {
        run(getWorkflowContext().getOriginalFile().getFile());
    }

    public void run(File file) throws Exception {
        InputStream input = new FileInputStream(file);
        Tika tika = new Tika();
        Metadata metadata = new Metadata();
        Exception exception = null;
        String mimeType = tika.detect(input);
        metadata.set(Metadata.CONTENT_TYPE, mimeType);

        Parser parser = new AutoDetectParser();
        ParseContext parseContext = new ParseContext();

        BodyContentHandler handler = new BodyContentHandler(-1);
        parser.parse(new FileInputStream(file), handler, metadata, parseContext);

        try {
            addDerivativeFile(file, "txt", handler.toString(), VersionType.INDEXABLE_TEXT);
        } catch (Exception e) {
            exception = e;
        }
        StringBuilder sw = new StringBuilder();
        for (String name : metadata.names()) {
            if (StringUtils.isNotBlank(metadata.get(name))) {
                sw.append(name).append(":");
                if (metadata.isMultiValued(name)) {
                    sw.append(StringUtils.join(metadata.getValues(name), "|"));
                } else {
                    sw.append(metadata.get(name));
                }
                sw.append("\r\n");
            }
        }
        addDerivativeFile(file, "metadata", sw.toString(), VersionType.METADATA);
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public String getName() {
        return "IndexableTextExtractionTask";
    }

}
