package org.tdar.core.bean;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MappedSuperclass
public abstract class AbstractSequenced<E extends AbstractSequenced<E>> extends AbstractPersistable implements Sequenceable<E> {
    private static final long serialVersionUID = -2667067170953144064L;

    @Column(name = "sequence_number")
    protected Integer sequenceNumber = 0;

    @Override
    public int compareTo(E other) {
        if ((sequenceNumber == null) || (other.sequenceNumber == null)) {
            return 0;
        }
        return sequenceNumber.compareTo(other.sequenceNumber);
    }

    @Override
    @XmlAttribute
    public Integer getSequenceNumber() {
        if (sequenceNumber == null) {
            setSequenceNumber(0);
        }
        return sequenceNumber;
    }

    @Override
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * set the sequence number for the elements in a list based
     * 
     * @param collection
     */
    public static <T extends Sequenceable<T>> void applySequence(Collection<T> collection) {
        int sequenceNumber = 1;
        for (Sequenceable<T> item : collection) {
            if (item == null) {
                Logger logger = LoggerFactory.getLogger(Sequenceable.class);
                logger.debug("null sequenceable found in collection -- skipping");
                continue;
            }
            item.setSequenceNumber(sequenceNumber);
            sequenceNumber++;
        }
    }
}