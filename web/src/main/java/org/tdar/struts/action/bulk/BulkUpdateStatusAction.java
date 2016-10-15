package org.tdar.struts.action.bulk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.SimpleBulkUploadService;
import org.tdar.core.service.bulk.BulkUpdateReceiver;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.Pair;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Component
@HttpsOnly
@Scope("prototype")
@Namespace("/bulk")
public class BulkUpdateStatusAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -5855079741655022360L;

    @Autowired
    private transient SimpleBulkUploadService bulkUploadService;

    @Autowired
    private transient SerializationService serializationService;

    private InputStream resultJson;

    private Float percentDone = 0f;
    private String phase;
    private String asyncErrors = "";
    private Long collectionId;
    private List<Pair<Long, String>> details;
    private Long ticketId;

    @Override
    public void prepare() {
        BulkUpdateReceiver reciever = bulkUploadService.checkAsyncStatus(getTicketId());
        if (reciever != null) {
            phase = reciever.getStatus();
            percentDone = reciever.getPercentComplete();
            getLogger().debug("{} {}%", phase, percentDone);
            StringBuffer sb = new StringBuffer();
            if (CollectionUtils.isNotEmpty(reciever.getAsyncErrors())) {
                getLogger().warn("bulkUploadErrors: {}", reciever.getAsyncErrors());
                for (String err : reciever.getHtmlAsyncErrors()) {
                    sb.append("<li>").append(err).append("</li>");
                }
                setAsyncErrors(sb.toString());
            }

            if (percentDone == 100f) {
                List<Pair<Long, String>> details = reciever.getDetails();
                setDetails(details);
                setCollectionId(reciever.getCollectionId());
                // should create revision log
            }
        } else {
            setAsyncErrors("");
            phase = "starting up...";
            percentDone = 0.0f;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("percentDone", percentDone);
        result.put("phase", phase);
        result.put("errors", asyncErrors);
        result.put("collectionId", collectionId);
        setResultJson(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(result, null, null).getBytes()));
    }

    @SkipValidation
    @Action(value = "checkstatus",
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    @PostOnly
    public String checkStatus() {
        return SUCCESS;
    }


    public void setPercentDone(Float percentDone) {
        this.percentDone = percentDone;
    }

    public Float getPercentDone() {
        return percentDone;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }
    

    /**
     * @param asyncErrors
     *            the asyncErrors to set
     */
    public void setAsyncErrors(String asyncErrors) {
        this.asyncErrors = asyncErrors;
    }

    /**
     * @return the asyncErrors
     */
    public String getAsyncErrors() {
        return asyncErrors;
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }


    public void setDetails(List<Pair<Long, String>> details) {
        this.details = details;
    }

    public List<Pair<Long, String>> getDetails() {
        return details;
    }


    public Long getTicketId() {
        return ticketId;
    }


    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

}
