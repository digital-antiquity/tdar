package org.tdar.core.bean.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.tdar.core.bean.FieldLength;

/**
 * $Id$
 * <p>
 * Represents any type of geospatial object, ShapeFile, GeoDatabase, and Georectified image
 * </p>
 * 
 * @author Adam Brin
 * @version $Revision: 543$
 */
@Entity
// @Indexed
@Table(name = "geospatial")
@XmlRootElement(name = "geospatial")
public class Geospatial extends Dataset {

    /**
     * 
     */
    private static final long serialVersionUID = -7898164729934403851L;

    public Geospatial() {
        setResourceType(ResourceType.GEOSPATIAL);
    }

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String currentnessUpdateNotes;

    @Column(name = "spatial_reference_system", length = FieldLength.FIELD_LENGTH_50)
    private String spatialReferenceSystem;

    @Column(name = "map_source", length = FieldLength.FIELD_LENGTH_500)
    private String mapSource;

    @Column(name = "scale", length = FieldLength.FIELD_LENGTH_100)
    private String scale;

    @Override
    @Transient
    public boolean isSupportsThumbnails() {
        return true;
    }

    @Override
    @Transient
    public boolean isHasBrowsableImages() {
        return true;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getSpatialReferenceSystem() {
        return spatialReferenceSystem;
    }

    public void setSpatialReferenceSystem(String spatialReferenceSystem) {
        this.spatialReferenceSystem = spatialReferenceSystem;
    }

    public String getCurrentnessUpdateNotes() {
        return currentnessUpdateNotes;
    }

    public void setCurrentnessUpdateNotes(String currentnessUpdateNotes) {
        this.currentnessUpdateNotes = currentnessUpdateNotes;
    }

    @Override
    public boolean isValidForController() {
        return super.isValidForController();
    }

    public String getMapSource() {
        return mapSource;
    }

    public void setMapSource(String mapSource) {
        this.mapSource = mapSource;
    }

}
