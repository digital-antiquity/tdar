package org.tdar.struts.data;

import java.io.InputStream;

import org.tdar.core.service.workflow.ActionMessageErrorSupport;

import com.opensymphony.xwork2.TextProvider;

public interface DownloadHandler extends ActionMessageErrorSupport, TextProvider {

    void setFileName(String filename);

    void setInputStream(InputStream inputStream);

    InputStream getInputStream();

    void setContentType(String mimeType);

    void setContentLength(long length);

    void setDispositionPrefix(String string);

    public boolean isEditor();
    
    boolean isCoverPageIncluded();
}
