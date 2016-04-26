package org.tdar.core.bean.keyword;

/**
 * An interface to manage suggested keywords
 * 
 * @version $Rev$
 */
public interface SuggestedKeyword extends Keyword {

    public boolean isApproved();

    public void setApproved(boolean approved);
}
