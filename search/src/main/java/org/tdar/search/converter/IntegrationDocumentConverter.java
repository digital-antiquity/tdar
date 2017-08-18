package org.tdar.search.converter;

import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

public class IntegrationDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(DataIntegrationWorkflow integration) {

        /*
         * See solr/configsets/default/conf/collections-schema.xml
         */
        SolrInputDocument doc = convertPersistable(integration);
        doc.setField(QueryFieldNames.NAME, integration.getName());
        doc.setField(QueryFieldNames.NAME_SORT, Sortable.getTitleSort(integration.getName()));
        doc.setField(QueryFieldNames.STATUS, Status.ACTIVE.name());

        doc.setField(QueryFieldNames.DESCRIPTION, integration.getDescription());
        StringBuilder sb = new StringBuilder();
        sb.append(integration.getTitle()).append(" ").append(integration.getDescription()).append(" ");

        doc.setField(QueryFieldNames.ALL, sb.toString());
        doc.setField(QueryFieldNames.SUBMITTER_ID, integration.getSubmitter().getId());

        Set<TdarUser> users = new HashSet<>();
        users.add(integration.getSubmitter());
        integration.getAuthorizedUsers().forEach(au -> {
            users.add(au.getUser());
        });
        doc.setField(QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY, PersistableUtils.extractIds(users));
        doc.setField(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, PersistableUtils.extractIds(users));
        doc.setField(QueryFieldNames.GENERAL_TYPE, LookupSource.INTEGRATION.name());
        doc.setField(QueryFieldNames.OBJECT_TYPE, ObjectType.INTEGRATION.name());
        doc.setField(QueryFieldNames.OBJECT_TYPE_SORT, ObjectType.INTEGRATION.getSortName());
        return doc;
    }

}
