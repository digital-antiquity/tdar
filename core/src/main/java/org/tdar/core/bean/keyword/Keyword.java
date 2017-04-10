package org.tdar.core.bean.keyword;

import java.util.Set;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.resource.Addressable;

/**
 * Interface and Abstract Class for all keywords. Unique entities managed outside of resources, and linked to them.
 * 
 * Base Class for all keywords
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@SuppressWarnings("rawtypes")
@XmlTransient
public interface Keyword extends Persistable, Indexable, HasLabel, Dedupable, Addressable {

    @Transient
    public static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "approved", "selectable", "level", "code", "occurrence" }; // fixme: should ID be here
                                                                                                                                 // too?

    @Override
    public String getLabel();

    String getSlug();

    public void setLabel(String label);

    public String getDefinition();

    public String getKeywordType();

    public void setDefinition(String definition);

    public Set<ExternalKeywordMapping> getAssertions();

    public void setAssertions(Set<ExternalKeywordMapping> mappings);

}
