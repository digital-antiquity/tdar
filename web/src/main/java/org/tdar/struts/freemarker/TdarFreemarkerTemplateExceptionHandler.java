package org.tdar.struts.freemarker;

import java.io.PrintWriter;
import java.io.Writer;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

import com.google.common.collect.EvictingQueue;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Borrows from the Freemarker ExceptionHandler, it attempts to log out a "friendly" error message and try and continue on. Errors are logged fully to the
 * console. In non-production, show the full stack trace, otherwise, show friendly message.
 * 
 * @author abrin
 *
 */
public class TdarFreemarkerTemplateExceptionHandler implements TemplateExceptionHandler {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());
    private EvictingQueue<String> seenUrls = EvictingQueue.create(10);

    public void handleTemplateException(TemplateException te, Environment env, Writer out)
            throws TemplateException {

        // for structural template issues, only show the error once per request
        String key = ServletActionContext.getRequest() + ServletActionContext.getRequest().getRequestURI();
        if (seenUrls.contains(key)) {
            return;
        }
        seenUrls.add(key);

        if (!env.isInAttemptBlock()) {
            boolean externalPw = out instanceof PrintWriter;
            PrintWriter pw = externalPw ? (PrintWriter) out : new PrintWriter(out);
            try {
                pw.println();
                pw.println("<div class=\"alert alert-important\"><p><b>" + MessageHelper.getMessage("freemarker.error")
                        + "</b><br/><small>TDAR:509</small></p></div>");
                pw.flush();
            } finally {
                if (!externalPw)
                    pw.close();
            }
        }

        if (!TdarConfiguration.getInstance().isProductionEnvironment()) {
            // we may want to always do this, but when I did outside of dev mode, we showed freemarker errors out.
            throw te;
        }
    }
}
