package org.tdar.struts.action.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.FileProxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * transport object for our FileUploadWidget
 * 
 * 
 * {"files":[<#list fileProxies as f>
 * <#local val = ""/>
 * <#if (f.fileCreatedDate)?has_content>
 * <#local val = f.fileCreatedDate?string["MM/dd/yyyy"]>
 * </#if>
 * 
 * {"name":"${f.filename?js_string}","sequenceNumber":${f.sequenceNumber?c}, "action":"NONE", "fileId":${(f.fileId!-1)?c}, "restriction":
 * "${f.restriction}","dateCreated":"${val}","description":"${(f.description!'')?js_string}"}<#sep>,</#sep>
 * </#list>],
 * "url":"/upload/upload",
 * "ticketId": ${(ticket.id?c)!-1},
 * "resourceId": ${(id!-1)?c},
 * <#if multipleUpload??>
 * "multipleUpload" : ${multipleUpload?string},
 * </#if>
 * <#if ableToUploadFiles??>
 * "ableToUpload" : ${ableToUploadFiles?string},
 * </#if>
 * "dataTableEnabled" : ${resource.resourceType.dataTableSupported?string},
 * "userId": ${authenticatedUser.id?c},
 * "validFormats":[<#list validFileExtensions as ext>".${ext}"<#sep>,</#sep></#list>],
 * "sideCarOnly":false,
 * "maxNumberOfFiles":${maxUploadFilesPerRecord},
 * "requiredOptionalPairs":[]
 * }
 * 
 * @author abrin
 *
 */
@JsonInclude(value=Include.NON_ABSENT)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class FileUploadSettings implements Serializable {

    private static final long serialVersionUID = -2111318200411438432L;
    private Long ticketId;
    private Long resourceId;
    private Long userId;
    private boolean multipleUpload;
    private boolean ableToUpload;
    private boolean dataTableEnabled;
    private List<String> validFormats = new ArrayList<>();
    private boolean sideCarOnly;
    private String url = "/upload/upload";
    private List<FileProxy> files = new ArrayList<>();
    private Integer maxNumberOfFiles;
    private List<RequiredOptionalPairs> requiredOptionalPairs = new ArrayList<>();

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isMultipleUpload() {
        return multipleUpload;
    }

    public void setMultipleUpload(boolean multipleUpload) {
        this.multipleUpload = multipleUpload;
    }

    public boolean isAbleToUpload() {
        return ableToUpload;
    }

    public void setAbleToUpload(boolean ableToUpload) {
        this.ableToUpload = ableToUpload;
    }

    public boolean isDataTableEnabled() {
        return dataTableEnabled;
    }

    public void setDataTableEnabled(boolean dataTableEnabled) {
        this.dataTableEnabled = dataTableEnabled;
    }

    public List<String> getValidFormats() {
        return validFormats;
    }

    public void setValidFormats(List<String> validFormats) {
        this.validFormats = validFormats;
    }

    public boolean isSideCarOnly() {
        return sideCarOnly;
    }

    public void setSideCarOnly(boolean sideCarOnly) {
        this.sideCarOnly = sideCarOnly;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElementWrapper(name = "files")
    @XmlElement(name = "file")
    public List<FileProxy> getFiles() {
        return files;
    }

    public void setFiles(List<FileProxy> files) {
        this.files = files;
    }

    public Integer getMaxNumberOfFiles() {
        return maxNumberOfFiles;
    }

    public void setMaxNumberOfFiles(Integer maxNumberOfFiles) {
        this.maxNumberOfFiles = maxNumberOfFiles;
    }

    @XmlElementWrapper(name = "requiredOptionalPairs")
    @XmlElement(name = "requiredOptionalPair")
    public List<RequiredOptionalPairs> getRequiredOptionalPairs() {
        return requiredOptionalPairs;
    }

    public void setRequiredOptionalPairs(List<RequiredOptionalPairs> requiredOptionalPairs) {
        this.requiredOptionalPairs = requiredOptionalPairs;
    }

}
