/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.index.bridge;

import java.net.URI;
import java.util.List;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.index.field.LazyReaderField;

/**
 * @author Adam Brin
 * 
 */
public class PersistentReaderBridge implements FieldBridge {

    List<URI> input;
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
        // TODO Auto-generated method stub
        if(name.equals("informationResources.content")) {
            logger.trace("not indexing {}", name);
            return;
        }
        if (value != null) {
            input = (List<URI>) value;
            LazyReaderField field = new LazyReaderField(name, input, luceneOptions.getStore(), luceneOptions.getIndex(), luceneOptions.getBoost());
            document.add(field);
        }

    }
}
