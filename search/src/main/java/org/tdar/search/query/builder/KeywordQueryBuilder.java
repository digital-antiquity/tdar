package org.tdar.search.query.builder;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.CoreNames;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class KeywordQueryBuilder extends QueryBuilder {

    public KeywordQueryBuilder() {
        this.setClasses(LookupSource.KEYWORD.getClasses());
    }

    public KeywordQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }

    @Override
    public String getCoreName() {
        return CoreNames.KEYWORDS;
    }
}
