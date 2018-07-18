package org.tdar.search.query.part;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.bean.DataValue;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

/*
 * Looks in the linked-data value index for vals (e..g key-value-pairs froma Mimbres record
 */
public class DataValueQueryPart extends FieldQueryPart<String> {

    private boolean escaped = false;
    private Long projectId;
    private Long fieldId;

    public DataValueQueryPart() {
    }

    public DataValueQueryPart(String term) {
        getFieldValues().add(term);
    }

    public DataValueQueryPart(String term, boolean escaped) {
        getFieldValues().add(term);
        this.escaped = escaped;
    }

    public DataValueQueryPart(String text, Operator operator, List<String> contents) {
        super(QueryFieldNames.VALUE, text, operator, contents);
    }

    public DataValueQueryPart(DataValue val) {
        this.projectId = val.getProjectId();
        this.fieldId = val.getColumnId();
        setFieldName(val.getName());
        getFieldValues().add(val.getValue());
    }

    @Override
    public String generateQueryString() {

        QueryPartGroup kvp = new QueryPartGroup(Operator.AND);
        QueryPartGroup subq = new QueryPartGroup(Operator.OR);
        FieldQueryPart<String> content = new FieldQueryPart<String>(QueryFieldNames.VALUE, getFieldValues());
        content.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        if (escaped) {
            content.setPhraseFormatters(PhraseFormatter.EMBEDDED);
        } else {
            content.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        }
        subq.append(content);
        FieldQueryPart<String> content2 = new FieldQueryPart<String>(QueryFieldNames.VALUE_PHRASE, getFieldValues());
        if (escaped) {
            content2.setPhraseFormatters(PhraseFormatter.EMBEDDED);
        } else {
            content2.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        }
        if (StringUtils.isNotBlank(getFieldName())) {
            FieldQueryPart<String> field = new FieldQueryPart<String>(QueryFieldNames.NAME, getFieldName());
            kvp.append(field);
        }
        if (PersistableUtils.isNotNullOrTransient(getFieldId())) {
            FieldQueryPart<Long> field = new FieldQueryPart<Long>(QueryFieldNames.COLUMN_ID, getFieldId());
            kvp.append(field);
        }
        if (PersistableUtils.isNotNullOrTransient(getProjectId())) {
            FieldQueryPart<Long> projectId = new FieldQueryPart<Long>(QueryFieldNames.PROJECT_ID, getProjectId());
            kvp.append(projectId);
        }
        subq.append(content2);

        kvp.append(subq);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossCoreFieldJoinQueryPart join = new CrossCoreFieldJoinQueryPart(QueryFieldNames.ID, QueryFieldNames.ID, kvp, LookupSource.DATA.getCoreName());
        return join.generateQueryString();
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long fieldId) {
        this.projectId = fieldId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
}
