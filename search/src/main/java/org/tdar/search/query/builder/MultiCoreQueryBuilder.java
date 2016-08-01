package org.tdar.search.query.builder;

import org.tdar.search.service.CoreNames;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class MultiCoreQueryBuilder extends ResourceQueryBuilder {

    public MultiCoreQueryBuilder() {
        setTypeLimit(null);

    }
    

    @Override
    public String getCoreName() {
        return CoreNames.RESOURCES;
    }

}
