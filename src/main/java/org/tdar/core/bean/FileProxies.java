package org.tdar.core.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class FileProxies implements Serializable {

    private static final long serialVersionUID = -5889144349803240012L;
    private List<FileProxy> fileProxies = new ArrayList<FileProxy>();

    public FileProxies() {
        fileProxies = new ArrayList<FileProxy>();
    }

    public FileProxies(List<FileProxy> items) {
        this.fileProxies = items;
    }

    @XmlElement(name = "fileProxy")
    public List<FileProxy> getFileProxies() {
        return fileProxies;
    }

}