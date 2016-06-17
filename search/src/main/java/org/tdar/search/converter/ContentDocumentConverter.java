package org.tdar.search.converter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
                File file = FILESTORE.retrieveFile(FilestoreObjectType.RESOURCE, irf.getIndexableVersion());
                doc.setField(QueryFieldNames.CONTENT, FileUtils.readFileToString(file));
            } catch (IOException e) {
                if (TdarConfiguration.getInstance().isProductionEnvironment()) {
                    logger.error("{}", e);
                } else {
                    logger.debug("file not found,{} ", irf);
                }
            }
//            logger.debug("{} {} {} {}", irf.getInformationResource().getId(), irf.getFilename(), irf.getRestriction());
        }
        
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        doc.setField(QueryFieldNames.FILENAME, irf.getFilename());
        doc.setField(QueryFieldNames.DATE, dateFormatUTC.format(irf.getFileCreatedDate()));
        doc.setField(QueryFieldNames.RESOURCE_ID, irf.getInformationResource().getId());
        doc.setField(QueryFieldNames.RESOURCE_ACCESS_TYPE, irf.getRestriction());

        return doc;
    }
}
