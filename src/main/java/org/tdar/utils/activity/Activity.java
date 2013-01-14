package org.tdar.utils.activity;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.web.SessionData;

public class Activity implements Serializable {

    private static final long serialVersionUID = 1078566853797118113L;

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Date startDate;
    private Date endDate;
    private Long totalTime = -1L;
    private String name;

    private Class<?> actionClass;
    private Class<?> manipulationClass;

    private Long id;
    private Person user;

    private String message;

    private String browser;
    private String host;

    public Activity() {
        start();
    }

    public static String formatRequest(HttpServletRequest request) {
        return String.format("%s:%s?%s", request.getMethod(), request.getServletPath(), request.getQueryString() == null ? "" : StringUtils.left(request.getQueryString(), 10));
    }

    public Activity(HttpServletRequest httpServletRequest) {
        this();
        HttpServletRequest request = ServletActionContext.getRequest();
        this.name = String.format("%s:%s?%s [%s]", request.getMethod(), request.getServletPath(),
                request.getQueryString() == null ? "" : request.getQueryString(), request.getHeader("User-Agent"));

        this.setBrowser(request.getHeader("User-Agent"));
        this.setHost(request.getRemoteHost());
        SessionData sessionData = (SessionData) request.getSession().getAttribute("scopedTarget.sessionData");
        if (sessionData != null) {
            setUser(sessionData.getPerson());
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public void start() {
        setStartDate(new Date());
    }

    public void end() {
        setEndDate(new Date());
        totalTime = endDate.getTime() - startDate.getTime();
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getActionClass() {
        return actionClass;
    }

    public void setActionClass(Class<?> actionClass) {
        this.actionClass = actionClass;
    }

    public Class<?> getManipulationClass() {
        return manipulationClass;
    }

    public void setManipulationClass(Class<?> manipulationClass) {
        this.manipulationClass = manipulationClass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s %s - (%s ms)", getName(), getStartDate(), getTotalTime());
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public boolean hasEnded() {
        return (this.endDate == null);
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public boolean hasExpired(long since) {
        if (endDate == null) {
            return false;
        }
        return endDate.getTime() < since;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
