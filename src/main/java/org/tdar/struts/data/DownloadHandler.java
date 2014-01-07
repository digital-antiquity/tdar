package org.tdar.struts.data;

import java.io.InputStream;

import org.tdar.core.service.workflow.ActionMessageErrorSupport;

public interface DownloadHandler extends ActionMessageErrorSupport {

    public void setFileName(String filename);

    public void setInputStream(InputStream inputStream);

    public InputStream getInputStream();

    public void setContentType(String mimeType);

    public void setContentLength(long length);

    public void setDispositionPrefix(String string);

    boolean isCoverPageIncluded();
}
