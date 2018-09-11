package org.tdar.core.bean.page.blocks;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
@DiscriminatorValue("HTML")
public class HtmlBlock extends AbstractBlock  {

    private static final long serialVersionUID = 3554724302122257164L;
    @Lob
    @Column(name="payload")
    @Type(type = "org.hibernate.type.TextType")
    private String html;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }


}
