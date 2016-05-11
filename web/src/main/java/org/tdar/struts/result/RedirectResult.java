package org.tdar.struts.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.struts2.result.ServletRedirectResult;

public class RedirectResult extends ServletRedirectResult {

    private static final long serialVersionUID = 7921561775626289552L;

    private String ignoreParams = "";

    @Override
    protected List<String> getProhibitedResultParams() {
        ArrayList<String> ignore = new ArrayList<>();
        ignore.add("ignoreParams");
        ignore.addAll(super.getProhibitedResultParams());
        return ListUtils.union(Arrays.asList(ignoreParams.split(",")), ignore);
    }

    public String getIgnoreParams() {
        return ignoreParams;
    }

    public void setIgnoreParams(String ignoreParams) {
        this.ignoreParams = ignoreParams;
    }
}
