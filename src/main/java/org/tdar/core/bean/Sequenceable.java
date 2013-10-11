package org.tdar.core.bean;

/**
 * $Id$
 * 
 * Persistable entity that is ordered by sequence.
 * 
 * @author <a href='james.t.devos@asu.edu'>Jim deVos</a>
 * @version $Revision$
 */

public interface Sequenceable<E extends Sequenceable<E>> extends Comparable<E> {

    void setSequenceNumber(Integer sequenceNumber);

    Integer getSequenceNumber();

}