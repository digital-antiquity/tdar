package org.tdar.struts.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class UsageStats implements Serializable {

    private static final long serialVersionUID = -3532666633416306513L;

    private List<AggregateViewStatistic> view;
    private Map<String, List<AggregateDownloadStatistic>> download;

    public UsageStats(List<AggregateViewStatistic> view, Map<String, List<AggregateDownloadStatistic>> download) {
        this.setView(view);
        this.setDownload(download);
    }

    public List<AggregateViewStatistic> getView() {
        return view;
    }

    public void setView(List<AggregateViewStatistic> view) {
        this.view = view;
    }

    public Map<String, List<AggregateDownloadStatistic>> getDownload() {
        return download;
    }

    public void setDownload(Map<String, List<AggregateDownloadStatistic>> download) {
        this.download = download;
    }

}
