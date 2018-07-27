<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl"  />
<#-- controlheader defines _numColumns (will always be number, default 1) -->

<#--
/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
-->
<#assign _numColumns = (parameters.numColumns!'1')?number>
<#assign itemCount = 0/>
<#assign spanClass = "">
<#assign hasColumns = _numColumns &gt; 1 />
<#if hasColumns>
    <#assign spanClass = "${parameters.spanClass!'col'}" />
    <div class="row">
    <div class="${spanClass}">
</#if>
<#if parameters.list??>
    <#--arrange items in column-major order. if list can't neatly fit in NxM grid, place remaining items in last column -->
    <#assign _numRows = (parameters.list?size / _numColumns)?ceiling />
    
    <@s.iterator value="parameters.list" var="key">
        <#assign itemCount = itemCount + 1/>
    <#if hasColumns>
       <#if itemCount &gt; 1 && itemCount % _numRows == 1>
         </div><div class="${spanClass}">
       </#if>
    </#if>
    <#if parameters.listKey??>
        <#assign itemKey = stack.findValue(parameters.listKey)/>
    <#else>
        <#assign itemKey = stack.findValue('top')/>
    </#if>
    <#if parameters.listValue??>
        <#assign itemValue = stack.findString(parameters.listValue)/>
    <#else>
        <#assign itemValue = stack.findString('top')/>
    </#if>
    <#if parameters.listTitle??>
        <#assign itemTitle = stack.findString(parameters.listTitle)!"" />
    <#elseif parameters.title??>
        <#assign itemTitle = parameters.title />
    </#if>
    
    <#assign itemKeyStr = itemKey.toString() />
    <div class="form-check">
    <label class="form-check-label"
    <#if itemTitle??>
     title="${itemTitle?html}"<#rt/>
    </#if>
    ><#rt/>
    <input type="checkbox"<#rt/>
    <#if parameters.name??>
     name="${parameters.name?html}"<#rt/>
    </#if>
     id="${parameters.id?html}${itemKeyStr?html}"<#rt/>
    <#if tag.contains(parameters.nameValue, itemKey) >
     checked="checked"<#rt/>
    </#if>
    <#if itemKey??>
     value="${itemKeyStr?html}"<#rt/>
    </#if>
    <#if parameters.disabled?default(false)>
     disabled="disabled"<#rt/>
    </#if>
    <#if parameters.tabindex??>
     tabindex="${parameters.tabindex?html}"<#rt/>
    </#if>
<#--     <#if parameters.cssClass??>
     class="${parameters.cssClass?html}"<#rt/>
    </#if>
    <#if parameters.cssStyle??>
     style="${parameters.cssStyle?html}"<#rt/>
    </#if> -->
    <#include "/${parameters.templateDir}/${parameters.expandTheme}/css.ftl" />
    <#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
    <#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
    /><#rt/>
        ${itemValue}<#t/>
    </label></div>
    </@s.iterator>
    <#if hasColumns>
    </div>
    </div>
    </#if>
    <#else>
  &nbsp;
</#if>

<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />