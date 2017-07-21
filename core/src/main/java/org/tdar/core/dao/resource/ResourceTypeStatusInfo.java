package org.tdar.core.dao.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.utils.MessageHelper;

public class ResourceTypeStatusInfo implements Serializable {

    private static final long serialVersionUID = -5499873582720227457L;

    private Map<Status, Integer> statusMap = new HashMap<>();
    private Map<ResourceType, Integer> resourceMap = new HashMap<>();

    private Integer total = 0;


    public Map<ResourceType, Integer> getResourceMap() {
        return resourceMap;
    }

    public Map<Status, Integer> getStatusMap() {
        return statusMap;
    }

    public void increment(Status status, ResourceType type, int count) {
        increment(type, resourceMap, count);
        increment(status, statusMap, count);
        total += count;
    }

    private <C> void increment(C type, Map<C, Integer> map, int count_) {
        Integer count = map.get(type);
        if (count == null) {
            count = 0;
        }
        map.put(type, count + count_);

    }


    public Integer getTotal() {
        return total ;
    }

    public List<List<Object>> getStatusData() {
        return createOutput(statusMap);
    }

    private <C extends Localizable> List<List<Object>> createOutput(Map<C, Integer> imap) {
        List<List<Object>> toReturn = new ArrayList<>();
        for (C stat : imap.keySet()) {
            List<Object> list = new ArrayList<>();
            toReturn.add(list);
            list.add(MessageHelper.getMessage(stat.getLocaleKey()));
            list.add(imap.get(stat));
            list.add(stat);
        }
        return toReturn;
    }

    public List<List<Object>>getResourceTypeData() {
        return createOutput(resourceMap);
    }
}
