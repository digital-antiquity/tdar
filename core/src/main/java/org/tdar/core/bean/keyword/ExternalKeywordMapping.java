package org.tdar.core.bean.keyword;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.RelationType;

@Entity
@Table(name = "keyword_mapping")
public class ExternalKeywordMapping extends Persistable.Base {

    private static final long serialVersionUID = 7035836586397546286L;

    @Column(name = "relation")
    @Length(max = FieldLength.FIELD_LENGTH_2048)
    private String relation;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private RelationType relationType;

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
    
}
