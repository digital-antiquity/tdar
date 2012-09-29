package org.tdar.core.bean.entity;

import java.util.Set;

import org.tdar.core.bean.Persistable;

/**
 * An interface used to indicate classes whose instances are potentially 'de-dupable' under authority management service.
 * 
 * @author jimdevos
 * 
 */
public interface Dedupable extends Persistable {
    public boolean isDedupable();

    public String getSynonymFormattedName();

    public Set<String> getSynonyms();

    public <D extends Dedupable> void addSynonym(D synonym);
}
