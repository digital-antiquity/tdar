package org.tdar.core.service.download;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.service.PdfService;

import com.opensymphony.xwork2.TextProvider;

/**
 * Represents a PDF File to be downloaded, generates a cover page as needed
 * @author abrin
 *
 */
public class DownloadPdfFile extends DownloadFile {

    private InformationResourceFileVersion version;
    private PdfService pdfService;
    private static final long serialVersionUID = -6999921596358876042L;
    private TdarUser person;
    private TextProvider provider;
    private Document document;

    public DownloadPdfFile(Document ir, InformationResourceFileVersion irFileVersion, PdfService pdfService, TdarUser person, TextProvider provider) {
        super(null);
        this.version = irFileVersion;
        this.pdfService = pdfService;
        this.person = person;
        this.provider = provider;
        this.document = ir;
    }
    
    @Override
    public InputStream getInputStream() throws Exception {
        getLogger().debug("person: {} version: {} provider: {}", person, version, provider);
        return new BufferedInputStream(pdfService.mergeCoverPage(provider, person, version, document));
    }
    
    public String getFileName() {
        return version.getFilename();
    }



}
