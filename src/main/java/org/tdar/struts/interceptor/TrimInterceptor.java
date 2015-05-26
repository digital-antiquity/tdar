package org.tdar.struts.interceptor;

/**
 * TrimInterceptor originally written for s2wad project (https://code.google.com/p/s2wad/) by Dave L Newman, June 2009. Original license text follows
 */
//        Copyright 2009 Dave L Newman
//        Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
//        http://www.apache.org/licenses/LICENSE-2.0
//
//        Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
//

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

public class TrimInterceptor extends MethodFilterInterceptor {

    private static final long serialVersionUID = 4306955419192999607L;
    private List<String> excluded = new ArrayList<String>();

    protected String doIntercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> parameters = invocation.getInvocationContext().getParameters();

        for (String param : parameters.keySet()) {
            if (isIncluded(param)) {
                String[] vals = (String[]) parameters.get(param);
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = vals[i].trim();
                }
            }
        }

        return invocation.invoke();
    }

    private boolean isIncluded(String param) {
        for (String exclude : excluded) {
            if (param.startsWith(exclude)) {
                return false;
            }
        }

        return true;
    }

    public void setExcludedParams(String excludedParams) {
        for (String s : StringUtils.split(excludedParams, ",")) {
            excluded.add(s.trim());
        }
    }

}