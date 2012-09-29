package org.tdar.struts.result;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

import edu.asu.lib.jaxb.JaxbDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;

public class JaxbDocumentResult implements Result {

	private static final long serialVersionUID = -8983164414828858380L;

	private static final Logger LOG = LoggerFactory.getLogger(JaxbDocumentResult.class);

    public static final String DEFAULT_PARAM = "documentName";

    private String documentName;
    private Boolean formatOutput;
    private transient JaxbDocument jaxbDocument;
    
    public JaxbDocumentResult() {
    	super();
    } 
    
    public JaxbDocumentResult(String documentName) {
    	this();
    	this.documentName = documentName;
    	formatOutput = false;
    }
    
	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public Boolean getFormatOutput() {
		return formatOutput;
	}

	public void setFormatOutput(Boolean formatOutput) {
		this.formatOutput = formatOutput;
	}

	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		
		jaxbDocument = (JaxbDocument) invocation.getStack().findValue(documentName);
        
		if (jaxbDocument == null) {
            String msg = ("Can not find a edu.asu.lib.jaxb.JaxbDocument " +
            	"with the name [" + documentName + "] in the invocation stack. " +
                "Check the <param name=\"documentName\"> tag specified for this action.");
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
		
		Writer writer = null;
		try {
			HttpServletResponse resp = ServletActionContext.getResponse();
			writer = resp.getWriter();
            resp.setContentType("application/xml");
            JaxbDocumentWriter.write(jaxbDocument, writer, formatOutput);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Serving Jaxb result [" + documentName + "]");
            }
            
        } finally {
        	if (writer != null) writer.close();
        }
		
	}

}
