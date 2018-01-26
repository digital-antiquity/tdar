package org.tdar.struts.action;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.StatisticsService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpsOnly
public abstract class AbstractStatisticsAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 7216945559477749480L;

    private StatsResultObject statsForAccount;
    private Long id;
    private DateGranularity granularity = DateGranularity.YEAR;

    @Autowired
    protected SerializationService serializationService;

    @Autowired
    protected StatisticsService statisticsService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String json;

    protected void setupJson() {
        if (getStatsForAccount() == null || getStatsForAccount().empty()) {
            addActionError("abstractStatisticsAction.no_data");
            return;
        }
        try {
            logger.debug("begin json serialization");
            setJson(serializationService.convertToJson(getStatsForAccount().getObjectForJson()));
            logger.debug("done json serialization");
        } catch (IOException e) {
            logger.error("error setting json", e);
        }
    }

    @Action(value = "{id}", results = { @Result(name = SUCCESS, location = "../stats.ftl") })
    @Override
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }

    public StatsResultObject getStatsForAccount() {
        return statsForAccount;
    }

    public void setStatsForAccount(StatsResultObject statsForAccount) {
        this.statsForAccount = statsForAccount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(DateGranularity granularity) {
        this.granularity = granularity;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}
