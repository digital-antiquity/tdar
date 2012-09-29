/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.util;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;

/**
 * @author Adam Brin
 * 
 */
public class CellMetadata implements Comparable<CellMetadata> {

    private String name;
    private String displayName;
    private String comment;
    private Class<?> mappedClass;
    private boolean required = false;
    private int order = 0;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" display:(").append(displayName).append(" rqrd:").append(required).append(") -- ").append(mappedClass);
        return sb.toString();
    }

    /**
     * @param bulkAnnotation
     * @param labelPrefix
     * @param filename
     */
    public CellMetadata(String name, BulkImportField bulkAnnotation, Class<?> mapped, String labelPrefix) {
        this.name = name;
        this.required = bulkAnnotation.required();
        this.mappedClass = mapped;
        this.displayName = labelPrefix + " " + bulkAnnotation.label();
        this.comment = bulkAnnotation.comment();
        this.order = bulkAnnotation.order();
    }

    public CellMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object cm) {
        if (cm.getClass().isAssignableFrom(CellMetadata.class)) {
            return getName().equals(((CellMetadata) cm).getName());
        } else {
            return getName().equals((String) cm);
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * @return the mappedClass
     */
    public Class<?> getMappedClass() {
        return mappedClass;
    }

    /**
     * @param mappedClass
     *            the mappedClass to set
     */
    public void setMappedClass(Class<?> mappedClass) {
        this.mappedClass = mappedClass;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required
     *            the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return
     */
    public String getOutputName() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isBlank(getDisplayName())) {
            sb.append(getDisplayName());
        } else {
            sb.append(getName());
        }
        if (isRequired()) {
            sb.append("*");
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CellMetadata o) {
        if (o.getMappedClass().equals(getMappedClass()) || o.getOrder() < 1) {
            if (o.getOrder() > getOrder()) {
                return -1;
            } else if (o.getOrder() < getOrder()) {
                return 1;
            }
        }
        try {
            if (o.getMappedClass() != getMappedClass()) {
                if (o.getMappedClass().equals(Institution.class) && getMappedClass().equals(Person.class)) {
                    return -1;
                }
                if (getMappedClass().equals(Institution.class) && o.getMappedClass().equals(Person.class)) {
                    return 1;
                }
                return getMappedClass().getSimpleName().compareTo(o.getMappedClass().getSimpleName());
            }
        } catch (Exception e) {
            // do nothing
        }
        return getName().compareTo(o.getName());
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }
    
    public String getPropertyName() {
        if (name.indexOf(".") != -1) {
            return name.substring(name.lastIndexOf(".") + 1);
        }
        return name;
    }
}
