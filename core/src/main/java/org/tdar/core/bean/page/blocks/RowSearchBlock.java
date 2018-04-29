package org.tdar.core.bean.page.blocks;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
@DiscriminatorValue("ROW_SEARCH")
public class RowSearchBlock extends AbstractBlock {

    private static final long serialVersionUID = 2574287623180597024L;
    @Lob
    @Column(name = "payload")
    @Type(type = "org.hibernate.type.TextType")
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
