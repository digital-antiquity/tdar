/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.enums.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;

/**
 * This class is a proxy class that enables the BulkUpload process to map and manage the import process. The BulkUploadService
 * scans the resource tree and then creates the CellMetadata classes to describe each field.
 * 
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
        sb.append(name).append(" display:").append(displayName).append(" rqrd:").append(required).append(" -- ").append(mappedClass);
        return sb.toString();
    }

    /**
     * @param bulkAnnotation
     * @param labelPrefix
     * @param filename
     */
    public CellMetadata(String name, BulkImportField bulkAnnotation, Class<?> mapped, String labelPrefix) {
        this.name = name;
        this.mappedClass = mapped;
        this.required = bulkAnnotation.required();
        this.displayName = bulkAnnotation.label();
        if (StringUtils.isNotBlank(labelPrefix)) {
            this.displayName = labelPrefix + " " + bulkAnnotation.label();
        }
        this.comment = bulkAnnotation.comment();
        this.order = bulkAnnotation.order();
    }

    public CellMetadata(String name) {
        this.name = name;
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private List<?extends Enum<?>> enumList;

    public CellMetadata(Field field, BulkImportField annotation, Class<?> class2, Stack<List<Class<?>>> stack, String prefix) {
        this.mappedClass = class2;
        if (field.getType().isEnum()) {
            setEnumList((List<? extends Enum<?>>) Arrays.asList(field.getType().getEnumConstants()));
        }
        this.required = annotation.required();
        String fieldPrefix = "";
        if (stack.size() > 1) {
            for (int i = 1; i < stack.size(); i++) {
                List<Class<?>> list = stack.get(i);
                String prefix_ = "";
                for (Class<?> cls : list) {
                    prefix_ += cls.getSimpleName();
                }
                fieldPrefix += prefix_ + ".";
            }
        }
        this.name = fieldPrefix + field.getName();
        this.displayName = StringUtils.trim(prefix + " " + annotation.label());

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

    public List<?extends Enum<?>> getEnumList() {
        return enumList;
    }

    public void setEnumList(List<?extends Enum<?>> enumList) {
        this.enumList = enumList;
    }
}
