package org.tdar.struts.action.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.action.TdarBaseActionSupport;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("default")
@Namespace("/files")
@Component
@Scope("prototype")
/** a controller to manage file requests for creators and collections. At such point that these become more complex, such as needing to handle cover pages, 
 * or multiple files, this should be folded into our existing download controller using the FileStoreFile abstraction
 * 
 * @author abrin
 *
 */
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = "stream",
                params = {
                        "contentType", "${contentType}",
                        "inputName", "stream",
                        "contentDisposition", "filename=\"${filename}\""
                // ,"contentLength", "${downloadTransferObject.contentLength}"
                }
        ),
        @Result(name = TdarActionSupport.ERROR, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.FORBIDDEN, type = TdarActionSupport.HTTPHEADER, params = { "error", "403" }) })
public class NonFilestoreDownloadController extends TdarBaseActionSupport implements Preparable, Serializable {

    private static final long serialVersionUID = 520143306023106607L;
    private String contentType;
    private String filename;
    private FilestoreObjectType type;
    private String typeString;
    private String versionString;
    private InputStream stream;
    private Long id;
    private VersionType version;

    @Action("/creator/{versionString}/{id}/logo")
    public String creator() throws IOException {
        filename = "logo" + version.toPath() + ".jpg";
        type = FilestoreObjectType.CREATOR;
        FileStoreFile proxy = new FileStoreFile(type, version, getId(), getFilename());
        File file = TdarConfiguration.getInstance().getFilestore().retrieveFile(type, proxy);
        if (file != null) {
            setContentType(Files.probeContentType(file.toPath()));
            setFilename(file.getName());
            setStream(new FileInputStream(file));
            return SUCCESS;
        }
        return INPUT;
    }

    @Action("/collection/{versionString}/{id}/logo")
    public String collection() throws IOException {
        filename = "logo" + version.toPath() + ".jpg";
        type = FilestoreObjectType.COLLECTION;
        FileStoreFile proxy = new FileStoreFile(type, version, getId(), getFilename());
        File file = TdarConfiguration.getInstance().getFilestore().retrieveFile(type, proxy);
        if (file != null) {
            setContentType(Files.probeContentType(file.toPath()));
            setFilename(file.getName());
            setStream(new FileInputStream(file));
            return SUCCESS;
        }
        return INPUT;
    }

    @Override
    public void prepare() throws TdarActionException {
        getLogger().debug("{} - {} - {} - {}", versionString, typeString, id, filename);
        version = VersionType.forName(versionString);
        if (version == null || id == null) {
            abort(StatusCode.BAD_REQUEST, "bad request");
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public VersionType getVersion() {
        return version;
    }

    public void setVersion(VersionType version) {
        this.version = version;
    }

}