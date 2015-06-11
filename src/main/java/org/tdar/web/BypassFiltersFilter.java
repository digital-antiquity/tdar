package org.tdar.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * $Id$
 * 
 * This servlet filter forwards all requests to their final destination
 * bypassing any other defined filters.
 * 
 * Example from web.xml:
 * 
 * <code>
 * <filter>
 * 	<filter-name>bypass-filters-filter</filter-name>
 *  <filter-class>org.tdar.web.BypassFiltersFilter</filter-class>
 * </filter>
 * <filter-mapping>
 * 	<filter-name>bypass-filters-filter</filter-name>
 * 	<url-pattern>/services/*</url-pattern>
 * </filter-mapping>
 * </code>
 * 
 * The filter mapping which defines the URL patterns which will bypass other
 * filters should come first in the list of filter mappings.
 * 
 * @author <a href='mailto:matt.cordial@asu.edu'>Matt Cordial</a>
 * @version $Rev$
 */
public class BypassFiltersFilter implements Filter {

    private final transient Logger log = LoggerFactory.getLogger(BypassFiltersFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        log.debug("Forwarding to: " + req.getRequestURI());
        request.getRequestDispatcher(req.getRequestURI()).forward(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
