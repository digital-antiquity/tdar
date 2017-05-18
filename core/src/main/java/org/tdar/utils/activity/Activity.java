package org.tdar.utils.activity;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.external.session.SessionData;

public class Activity implements Serializable {

    public static final String USER_AGENT = "User-Agent";

    private static final long serialVersionUID = 1078566853797118113L;

    @SuppressWarnings("unused")
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private static final String MOZILLA = "Mozilla/5.0 (compatible;";
    static Pattern pattern = Pattern.compile("(bot|googlebot|crawler|spider|robot|crawling)");
    private Date startDate;
    private Date freemarkerHandoffDate;
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

    private boolean indexingActivity = false;

    private Float percentDone;

    private Object shortName;

    public Activity() {
        start();
    }

    public static String formatRequest(HttpServletRequest request) {
        return String.format("%s:%s%s", request.getMethod(), request.getServletPath(), StringUtils.left(getQueryString(request), 10));
    }

    private static String getQueryString(HttpServletRequest request) {
        String qs = "";
        if (StringUtils.isNotBlank(request.getQueryString())) {
            qs += "?" + request.getQueryString();
        }
        return qs;
    }

    public Activity(HttpServletRequest request, TdarUser user) {
        this();
        this.setShortName(String.format("%s:%s%s", request.getMethod(), request.getServletPath(), getQueryString(request)));
        this.setBrowser(request.getHeader(USER_AGENT));
        this.name = String.format("%s [%s]", getShortName(), getSimpleAgent(request.getHeader(USER_AGENT)));

        this.setHost(request.getRemoteHost());
        SessionData sessionData = (SessionData) request.getSession().getAttribute("scopedTarget.sessionData");
        if (sessionData != null) {
            setUser(user);
        }
    }

    private String getSimpleAgent(String header) {
        if (isBot()) {
            return "[bot]";
        } 
            return header;
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

    public String getEndString() {
        // total time = action time + render time
        return String.format("e» %sms %s", getTotalTime(), getFreemarkerFormattedTime());
    }

    public String getStartString() {
        return String.format("«b %s ", getName());
    }

    @Override
    public String toString() {
        return String.format("%s %s - (%s ms)", getName(), getStartDate(), getTotalTime());
    }

    private String getFreemarkerFormattedTime() {
        if (freemarkerHandoffDate != null && endDate != null) {
            return String.format(" | a:%sms; r:%sms", getActionTime(), getResultTime());
        }
        return "";
    }

    public long getResultTime() {
        if (getFreemarkerHandoffDate() != null && getEndDate() != null) {
            return getEndDate().getTime() - getFreemarkerHandoffDate().getTime();
        }
        return -1l;
    }

    public long getActionTime() {
        if (getFreemarkerHandoffDate() != null && getStartDate() != null) {
            return getFreemarkerHandoffDate().getTime() - getStartDate().getTime();
        }
        return -1l;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public boolean hasEnded() {
        return (this.endDate != null);
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser_) {
        this.browser = browser_;
    }

    public String getSimpleBrowserName() {
        String browser_ = this.browser;
        if (StringUtils.isNotBlank(browser_)) {
            browser_ = browser_.trim();
            if (browser_.contains(MOZILLA) && browser_.endsWith(")")) {
                browser_ = browser_.replace(MOZILLA, "");
                browser_ = browser_.substring(0, browser_.length() - 2);
            }
        }
        return browser_;

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

    public void setIndexingActivity(boolean b) {
        indexingActivity = b;
    }

    public boolean isIndexingActivity() {
        return indexingActivity;
    }

    public Float getPercentComplete() {
        return getPercentDone();
    }

    public Float getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(Float percentDone) {
        this.percentDone = percentDone;
    }

    public Date getFreemarkerHandoffDate() {
        return freemarkerHandoffDate;
    }

    public void setFreemarkerHandoffDate(Date freemarkerHandoffDate) {
        this.freemarkerHandoffDate = freemarkerHandoffDate;
    }

    public Object getShortName() {
        return shortName;
    }

    public void setShortName(Object shortName) {
        this.shortName = shortName;
    }
    
    public boolean isBot() {
        return Activity.testUserAgent(getBrowser());
    }

    public static boolean testUserAgent(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return false;
        }
        return pattern.matcher(userAgent).find();
    }
    
    
}
