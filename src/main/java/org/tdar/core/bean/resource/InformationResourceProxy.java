package org.tdar.core.bean.resource;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Immutable
@Table(name="information_resource")
@Inheritance(strategy = InheritanceType.JOINED)
public class InformationResourceProxy extends ResourceProxy implements Serializable {

    private static final long serialVersionUID = -6093387188157752280L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Column(name = "date_created")
    private Integer date = -1;

    @ManyToOne(optional = true)
    @JoinColumn(name = "project_id")
    private ResourceProxy projectProxy;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = InformationResourceFileProxy.class)
    @JoinColumn(name = "information_resource_id")
    @Immutable
    private List<InformationResourceFileProxy> informationResourceFileProxies = new ArrayList<>();


    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s %s", getId(), getTitle(), getLatitudeLongitudeBoxes(), getResourceCreators(), getProjectProxy(),
                getInformationResourceFileProxies(), getSubmitter());
    }


    public ResourceProxy getProjectProxy() {
        return projectProxy;
    }

    public void setProjectProxy(InformationResourceProxy project) {
        this.projectProxy = project;
    }

    public List<InformationResourceFileProxy> getInformationResourceFileProxies() {
        return informationResourceFileProxies;
    }

    public void setInformationResourceFileProxies(List<InformationResourceFileProxy> informationResourceFiles) {
        this.informationResourceFileProxies = informationResourceFiles;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Resource> T generateResource() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        logger.trace("begin bean generation: {}", getId());
        T res_ = super.generateResource();
        InformationResource res = (InformationResource)res_;

        res.setDate(getDate());
        logger.trace("recursing down");
        if (projectProxy != null) {
            res.setProject((Project)getProjectProxy().generateResource());
        }
        if (res instanceof InformationResource) {
            InformationResource ir = (InformationResource) res;
            ir.setDate(this.getDate());
            for (InformationResourceFileProxy prox : getInformationResourceFileProxies()) {
                ir.getInformationResourceFiles().add(prox.generateInformationResourceFile());
            }
            Project project = Project.NULL;
            if (getProjectProxy() != null) {
                project = getProjectProxy().generateResource();
            }
            ir.setProject(project);

        }
        logger.trace("done generation");
        return (T)res;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }
}
