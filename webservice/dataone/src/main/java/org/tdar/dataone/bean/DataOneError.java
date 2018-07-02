
package org.tdar.dataone.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Error object is the only object that's not packaged as part of the DataOne package.
 * It has an error code, detail, name, and description.
 * 
 * @author abrin
 *
 */
@XmlType(name = "error")
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataOneError implements Serializable {

    private static final long serialVersionUID = 7779217712561356330L;

    @XmlAttribute
    private int detailCode;

    @XmlAttribute
    private int errorCode;

    @XmlAttribute
    private String name;

    private String description;

    public DataOneError() {

    }

    public DataOneError(int errorCode, String name, String description, int detailCode) {
        this.errorCode = errorCode;
        this.detailCode = detailCode;
        this.description = description;
        this.name = name;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDetailCode() {
        return detailCode;
    }

    public void setDetailCode(int detailCode) {
        this.detailCode = detailCode;
    }
}
