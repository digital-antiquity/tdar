package org.tdar.transform;

import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;

public class OpenUrlFormatter {

    private static final String UTF_8 = "UTF-8";
    private static final String AMP = "&amp;";
    private final static transient Logger logger = LoggerFactory.getLogger(OpenUrlFormatter.class);

    @SuppressWarnings("deprecation")
    public static String toOpenURL(Resource resource) {
        StringBuilder sb = new StringBuilder();
        try {
            TdarConfiguration conf = TdarConfiguration.getInstance();
            sb.append("ctx_ver=Z39.88-2004&amp;rfr_id=info:sid/").append(conf.getBaseUrl());
            sb.append(AMP);
            if (StringUtils.isNotBlank(resource.getExternalId())) {
                sb.append("rft.doi=").append(resource.getExternalId());
                sb.append(AMP);
            }
            String genre = resource.getResourceType().getOpenUrlGenre();
            String title = resource.getName();
            if (resource instanceof Document) {
                Document doc = ((Document) resource);
                switch (doc.getDocumentType()) {
                    case BOOK_SECTION:
                        if (StringUtils.isNotBlank(doc.getBookTitle())) {
                            title = doc.getBookTitle();
                        }
                        sb.append(AMP);
                        sb.append("rft.btitle=").append(URLEncoder.encode(doc.getBookTitle(), UTF_8));
                        break;
                    case CONFERENCE_PRESENTATION:
                        break;
                    case JOURNAL_ARTICLE:
                        if (StringUtils.isNotBlank(doc.getJournalName())) {
                            title = doc.getJournalName();
                        }
                        sb.append("rft.atitle=").append(URLEncoder.encode(doc.getTitle(), UTF_8));
                        sb.append(AMP);
                        break;
                    default:
                        break;
                }
                genre = doc.getDocumentType().getOpenUrlGenre();
                if (StringUtils.isNotBlank(doc.getIssn())) {
                    sb.append("rft.issn=").append(doc.getIssn());
                    sb.append(AMP);
                }
                if (StringUtils.isNotBlank(doc.getIsbn())) {
                    sb.append("rft.isbn=").append(doc.getIsbn());
                    sb.append(AMP);
                }
            }
            sb.append("rft_val_fmt=info:ofi/fmt:kev:mtx:").append(genre);
            sb.append(AMP);
            sb.append("rft.genre=").append(genre);
            sb.append(AMP);
            sb.append("rft.title=").append(URLEncoder.encode(title));
        } catch (Exception e) {
            logger.error("exception in urlFormatter", e);
        }
        return sb.toString();
    }

}
