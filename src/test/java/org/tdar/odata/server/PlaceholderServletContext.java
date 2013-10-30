package org.tdar.odata.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class PlaceholderServletContext implements ServletContext {

    @Override
    public ServletContext getContext(String uripath) {
        throw new UnsupportedOperationException("getContext");
    }

    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException("getMajorVersion");
    }

    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException("getMinorVersion");
    }

    @Override
    public String getMimeType(String file) {
        throw new UnsupportedOperationException("getMimeType");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException("getResourcePaths");
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        throw new UnsupportedOperationException("getResource");
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        throw new UnsupportedOperationException("getResourceAsStream");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("getRequestDispatcher");
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException("getNamedDispatcher");
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        throw new UnsupportedOperationException("getServlet");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getServlets() {
        throw new UnsupportedOperationException("getServlets");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getServletNames() {
        throw new UnsupportedOperationException("getServletNames");
    }

    @Override
    public void log(String msg) {
        throw new UnsupportedOperationException("log");
    }

    @Override
    public void log(Exception exception, String msg) {
        throw new UnsupportedOperationException("log");
    }

    @Override
    public void log(String message, Throwable throwable) {
        throw new UnsupportedOperationException("log");
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException("getRealPath");
    }

    @Override
    public String getServerInfo() {
        throw new UnsupportedOperationException("getServerInfo");
    }

    @Override
    public String getInitParameter(String name) {
        throw new UnsupportedOperationException("getInitParameter");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getInitParameterNames() {
        throw new UnsupportedOperationException("getInitParameterNames");
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("getAttribute");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
        throw new UnsupportedOperationException("getAttributeNames");
    }

    @Override
    public void setAttribute(String name, Object object) {
        throw new UnsupportedOperationException("setAttribute");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("removeAttribute");
    }

    @Override
    public String getServletContextName() {
        throw new UnsupportedOperationException("getServletContextName");
    }

}
