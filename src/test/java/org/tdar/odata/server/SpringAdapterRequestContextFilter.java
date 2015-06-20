package org.tdar.odata.server;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextListener;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class SpringAdapterRequestContextFilter extends Filter
{
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private RequestContextListener contextListener = new RequestContextListener();

    @Override
    public void doFilter(final HttpExchange exchange, final Chain chain) throws IOException {

        ServletContext context = new PlaceholderServletContext();
        ServletRequestEvent requestEvent = null;

        try {
            ServletRequest request = new HttpExchangeServletWrapper(exchange);
            requestEvent = new ServletRequestEvent(context, request);
            contextListener.requestInitialized(requestEvent);
            chain.doFilter(exchange);
        } catch (Throwable throwable)
        {
            getLogger().info("DoFilter on Context Filter.", throwable);
            throw new RuntimeException("DoFilter on RequestContext Filter.", throwable);
        } finally
        {
            contextListener.requestDestroyed(requestEvent);
        }
    }

    @Override
    public String description() {
        return "An adapter that converts between com.sun.net.httpserver.Filter and javax.servlet.Filter";
    }

    public Logger getLogger()
    {
        return logger;
    }

};
