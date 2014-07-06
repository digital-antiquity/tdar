package org.tdar.transform;

import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;

public class OpenUrlFormatter {

    private static final String AMP = "&amp;";

    public static String toOpenURL(Resource resource) {
        StringBuilder sb = new StringBuilder();

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
                    title = doc.getBookTitle();
                    sb.append(AMP);
                    sb.append("rft.btitle=").append(URLEncoder.encode(doc.getBookTitle()));
                    break;
                case CONFERENCE_PRESENTATION:
                    break;
                case JOURNAL_ARTICLE:
                    title = doc.getJournalName();
                    sb.append("rft.atitle=").append(URLEncoder.encode(doc.getTitle()));
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
        return sb.toString();
    }

}
