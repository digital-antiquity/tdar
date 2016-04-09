package org.tdar.search.query.part;

import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;

public class ContentQueryPart extends FieldQueryPart<String> {

    
    public ContentQueryPart() {
    }

    public ContentQueryPart(String term) {
        getFieldValues().add(term);
    }


    public ContentQueryPart(String text, Operator operator, List<String> contents) {
        super(QueryFieldNames.CONTENT, text,operator, contents);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup subq = new QueryPartGroup(Operator.AND);
        FieldQueryPart<String> content = new FieldQueryPart<String>(QueryFieldNames.CONTENT, getFieldValues());
        content.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        subq.append(content);
        subq.append(new FieldQueryPart<FileAccessRestriction>(QueryFieldNames.RESOURCE_ACCESS_TYPE, FileAccessRestriction.PUBLIC));
        CrossCoreFieldJoinQueryPart join = new CrossCoreFieldJoinQueryPart(QueryFieldNames.ID, QueryFieldNames.RESOURCE_ID, subq, LookupSource.CONTENTS.getCoreName());
        return join.generateQueryString();
    }
}
