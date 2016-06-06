package org.tdar.core.bean.resource.sensory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasResource;
import org.tdar.core.bean.resource.SensoryData;

/**
 * represents an image
 * 
 * @author abrin
 * 
 */
@Entity
@Table(name = "sensory_data_image")
public class SensoryDataImage extends AbstractSequenced<SensoryDataImage> implements HasResource<SensoryData> {
    private static final long serialVersionUID = -9115746507586171584L;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String filename;

    @Column
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String description;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @XmlTransient
    public boolean isValid() {
        return StringUtils.isNotBlank(filename);
    }

    @Override
    @XmlTransient
    public boolean isValidForController() {
        return true;
    }
}
