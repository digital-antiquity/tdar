package org.tdar.core.bean;

import org.apache.lucene.search.Explanation;
import javax.persistence.Transient;

public interface Indexable {

    /**
     * @param score
     */
    public void setScore(Float score);

    @Transient
    public Float getScore();

    public Explanation getExplanation();

    public void setExplanation(Explanation ex);

}
