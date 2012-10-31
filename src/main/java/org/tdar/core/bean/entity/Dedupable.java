package org.tdar.core.bean.entity;

import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.configuration.JSONTransient;

/**
 * An interface used to indicate classes whose instances are potentially 'de-dupable' under authority management service.
 * 
 * @author jimdevos
 * 
 */
public interface Dedupable<Dedupable> extends Persistable, HasStatus {

    public boolean isDedupable();

    @JSONTransient
    @XmlTransient
    @Deprecated
    public String getSynonymFormattedName();

    public Set<Dedupable> getSynonyms();

}
