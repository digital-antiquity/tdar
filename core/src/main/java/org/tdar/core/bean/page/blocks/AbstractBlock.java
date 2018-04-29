package org.tdar.core.bean.page.blocks;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Sequenceable;

@Entity
@Table(name = "page_block")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "block_type", length = FieldLength.FIELD_LENGTH_50, discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractBlock extends AbstractPersistable implements Sequenceable<AbstractBlock> {

    private static final long serialVersionUID = 791268282925316933L;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int compareTo(AbstractBlock o) {
        return this.sequenceNumber.compareTo(o.getSequenceNumber());
    }

}
