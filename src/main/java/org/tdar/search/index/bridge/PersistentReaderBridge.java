/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.index.bridge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.search.index.field.LazyReaderField;

/**
 * @author Adam Brin
 * 
 */
public class PersistentReaderBridge implements FieldBridge {

    private List<URI> input;
    protected final static transient Logger logger = LoggerFactory.getLogger(Resource.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document,
     * org.hibernate.search.bridge.LuceneOptions)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {

        if (name.equals("informationResources.content")) {
            logger.trace("not indexing {}", name);
            return;
        }
        if (value != null) {
            input = new ArrayList<>();
            Filestore filestore = TdarConfiguration.getInstance().getFilestore();
            for (InformationResourceFileVersion version : (List<InformationResourceFileVersion>) value) {
                if (version == null) {
                    continue;
                }
                try {
                    logger.trace("indexing file ... {}", version);
                    input.add(filestore.retrieveFile(ObjectType.RESOURCE, version).toURI());
                } catch (FileNotFoundException e) {
                    if (TdarConfiguration.getInstance().ignoreMissingFilesInFilestore()) {
                        logger.error("File does not exist", e);
                    } else {
                        logger.trace("File does not exist", e);
                    }
                }
            }
            LazyReaderField field = new LazyReaderField(name, input, readerValue(name, input), luceneOptions.getBoost());
            document.add(field);
        }

    }
    

    public Reader readerValue(String name, List<URI> paths) {
        logger.trace("getting reader for: {}", name);

        List<InputStream> streams = new ArrayList<InputStream>();
        for (URI uri : paths) {
            try {
                streams.add(uri.toURL().openStream());
            } catch (Exception e) {
                logger.debug("cannot read url:", e);
            }
        }
        SequenceInputStream stream = new SequenceInputStream(Collections.enumeration(streams));
        logger.trace("returning stream reader {}", streams);

        return new BufferedReader(new InputStreamReader(stream));
    }

}
