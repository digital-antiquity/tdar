package org.tdar.test.web;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public class JavaScriptErrorListenerImplementation implements JavaScriptErrorListener {

    transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void loadScriptError(HtmlPage arg0, URL scriptUrl, Exception exception) {
        logger.error("load script Error: {} {}", scriptUrl, exception);

    }

    @Override
    public void malformedScriptURL(HtmlPage arg0, String scriptUrl, MalformedURLException exception) {
        logger.error("malformed url Error: {} {}", scriptUrl, exception);

    }

    @Override
    public void scriptException(HtmlPage scriptUrl, ScriptException exception) {
        logger.error("script exception: {} {}", scriptUrl, exception);

    }

    @Override
    public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
        logger.error("timeout Error: {} ", arg0);

    }
}
