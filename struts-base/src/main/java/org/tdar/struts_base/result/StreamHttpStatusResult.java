package org.tdar.struts_base.result;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.result.StreamResult;

import com.opensymphony.xwork2.ActionInvocation;

/**
 * StreamResult with the ability to specify http status (default is {@link org.apache.commons.httpclient.HttpStatus#SC_OK}.)
 *
 * //FIXME: make this more consistent with FreemarkerHttpHeaderResult, which allows status code *and* arbitrary http headers
 */
public class StreamHttpStatusResult extends StreamResult {

    private static final long serialVersionUID = 8090795034611811292L;
    Integer status;

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        if (status != null) {
            HttpServletResponse oResponse = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
            oResponse.setStatus(status);
        }

        // call to parent comes last because it writes to the response outputstream (and thus will have sent the status code)
        super.doExecute(finalLocation, invocation);
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
