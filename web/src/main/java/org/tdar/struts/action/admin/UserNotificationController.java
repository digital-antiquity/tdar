package org.tdar.struts.action.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.apache.struts2.util.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationType;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.UserNotificationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Preparable;

/**
 * Manages admin CRUD requests for UserNotifications.
 * 
 * FIXME: refactor to use Object / Map for json. E.g.,
 * 
 * @Result(name = SUCCESS, type = JSONRESULT, params = { "jsonObject", "resultObject" }),
 * @Result(name = INPUT, type = JSONRESULT, params = { "jsonObject", "resultObject", "statusCode", "500" }
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin/notifications")
@RequiresTdarUserGroup(TdarGroup.TDAR_ADMIN)
public class UserNotificationController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 5844268857719107949L;

    private List<UserNotification> allNotifications;
    // FIXME: replace with JSONRESULT type
    private String notificationsJson;
    private String allMessageTypesJson;
    private InputStream resultJson;

    private int startRecord = 0;
    private int recordsPerPage = 500;
    // incoming notification fields
    private Long id;
    private UserNotification notification;

    @Autowired
    private transient UserNotificationService userNotificationService;

    @Autowired
    private transient SerializationService serializationService;

    @Override
    public void prepare() {
        getLogger().debug("preparing with id {}", getId());
        notification = userNotificationService.find(getId());
        if (notification == null) {
            notification = new UserNotification();
        }
        getLogger().debug("prepared notification: {}", notification);
    }

    @Actions({
            @Action("index")
    })
    public String execute() {
        
        List<UserNotification> notifications = userNotificationService.findAll(this);
        if (notifications.size() < startRecord) {
            int max = notifications.size();
            if (max > startRecord + recordsPerPage) {
                max = startRecord + recordsPerPage;
            }
            
            for (int i = startRecord; i < max; i++) {
                allNotifications.add(notifications.get(i));
            }
        }
        notificationsJson = serializationService.convertFilteredJsonForStream(allNotifications, null, null);
        allMessageTypesJson = serializationService.convertFilteredJsonForStream(UserNotificationType.values(), null, null);
        return SUCCESS;
    }

    @Action(value = "lookup",
            results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" })
            })
    @SkipValidation
    public String lookup() {
        String messageKey = notification.getMessageKey();
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("message", getText(messageKey));
        String json = serializationService.convertFilteredJsonForStream(jsonMap, null, null);
        this.resultJson = new ByteArrayInputStream(json.getBytes());
        return SUCCESS;
    }

    @Action(value = "update",
            // FIXME: using CSRF with ajax requires a fresh token per request.
            // interceptorRefs = { @InterceptorRef("csrfAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" })
            })
    @WriteableSession
    @PostOnly
    public String update() {
        getLogger().debug("updating notification {} with id {}", notification, notification.getId());
        getGenericService().saveOrUpdate(notification);
        notification.setMessage(this);
        this.resultJson = new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(notification, null, null).getBytes());
        return SUCCESS;
    }

    @Action(value = "delete",
            // FIXME: using CSRF with ajax requires a fresh token per request.
            // interceptorRefs = { @InterceptorRef("csrfAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" })
            })
    @WriteableSession
    @PostOnly
    public String delete() {
        getLogger().debug("deleting notification {}", notification);
        getGenericService().delete(notification);
        this.resultJson = new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(notification, null, null).getBytes());
        return SUCCESS;
    }

    @Action(value = "generate-token",
            results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" })
            })
    public String generateToken() {
        Map<String, Object> context = ActionContext.getContext().getValueStack().getContext();
        Map<String, Object> jsonMap = new HashMap<>();
        Object token = context.get("token");
        if (token == null) {
            token = TokenHelper.setToken("token");
            context.put("token", token);
        }
        jsonMap.put("token", token);
        String json = serializationService.convertFilteredJsonForStream(jsonMap, null, null);
        this.resultJson = new ByteArrayInputStream(json.getBytes());
        return SUCCESS;
    }

    public List<UserNotification> getAllNotifications() {
        return allNotifications;
    }

    public String getNotificationsJson() {
        return notificationsJson;
    }

    public String getAllMessageTypesJson() {
        return allMessageTypesJson;
    }

    public void setAllMessageTypesJson(String allMessageTypesJson) {
        this.allMessageTypesJson = allMessageTypesJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserNotification getNotification() {
        return notification;
    }

    public void setNotification(UserNotification notification) {
        this.notification = notification;
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

}
