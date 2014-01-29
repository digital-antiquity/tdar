package org.tdar.struts.data;

import java.io.InputStream;

import org.tdar.core.service.workflow.ActionMessageErrorSupport;

public interface DownloadHandler extends ActionMessageErrorSupport {

    void setFileName(String filename);

    void setInputStream(InputStream inputStream);

    InputStream getInputStream();

    void setContentType(String mimeType);

    void setContentLength(long length);

    void setDispositionPrefix(String string);

    boolean isCoverPageIncluded();
}
