package org.tdar.search.converter;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.search.query.QueryFieldNames;

public class ContentDocumentConverter extends AbstractSolrDocumentConverter {

    private static final Filestore FILESTORE = TdarConfiguration.getInstance().getFilestore();

	public static SolrInputDocument convert(InformationResourceFile irf) {
        	if (irf == null || irf.getIndexableVersion() == null) {
        		return null;
        	}
        SolrInputDocument doc = convertPersistable(irf);
        if (irf.getIndexableVersion() != null && irf.isPublic()) {
            try {
                doc.setField(QueryFieldNames.CONTENT, FileUtils.readFileToString(FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, irf.getIndexableVersion())));
            } catch (FileNotFoundException fnf) {
                
            } catch (IOException e) {
                logger.error("{}", e);
            }
	        doc.setField(QueryFieldNames.FILENAME, irf.getFilename());
        }

        return doc;
    }
}
