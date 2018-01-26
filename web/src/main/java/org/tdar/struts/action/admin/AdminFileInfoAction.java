package org.tdar.struts.action.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.StatisticsService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;

/**
 * $Id$
 * 
 * Administrative actions (that shouldn't be available for wide use).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/admin")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminFileInfoAction extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 4550037457679814164L;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    @Autowired
    private transient StatisticsService StatisticsService;

    private Map<String, Long> extensionStats;
    private List<InformationResourceFile> files;
    private Map<String, List<Number>> fileAverageStats;
    private Map<String, Long> fileStats;
    private Map<String, List<Number>> fileUploadedAverageStats;

    @Action("file-info")
    public String fileInfo() {
        setFileAverageStats(StatisticsService.getFileAverageStats(Arrays.asList(VersionType.values())));
        setFileStats(StatisticsService.getFileStats(Arrays.asList(VersionType.values())));
        setFileUploadedAverageStats(StatisticsService.getFileAverageStats(
                Arrays.asList(VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL, VersionType.UPLOADED_TEXT, VersionType.ARCHIVAL)));
        setExtensionStats(informationResourceFileService.getAdminFileExtensionStats());
        setFiles(informationResourceFileService.findFilesWithStatus(FileStatus.PROCESSING_ERROR, FileStatus.PROCESSING_WARNING));
        return SUCCESS;
    }

    public Map<String, Long> getExtensionStats() {
        return extensionStats;
    }

    public void setExtensionStats(Map<String, Long> map) {
        this.extensionStats = map;
    }

    public Map<String, List<Number>> getFileAverageStats() {
        return fileAverageStats;
    }

    public void setFileAverageStats(Map<String, List<Number>> fileAverageStats) {
        this.fileAverageStats = fileAverageStats;
    }

    public Map<String, List<Number>> getFileUploadedAverageStats() {
        return fileUploadedAverageStats;
    }

    public void setFileUploadedAverageStats(Map<String, List<Number>> fileUploadedAverageStats) {
        this.fileUploadedAverageStats = fileUploadedAverageStats;
    }

    public List<InformationResourceFile> getFiles() {
        return files;
    }

    public void setFiles(List<InformationResourceFile> files) {
        this.files = files;
    }

    public Map<String, Long> getFileStats() {
        return fileStats;
    }

    public void setFileStats(Map<String, Long> map) {
        this.fileStats = map;
    }

}
