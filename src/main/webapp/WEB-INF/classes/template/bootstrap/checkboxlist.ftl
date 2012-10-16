<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />
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
<#assign itemCount = 0/>
<#assign numCols = (parameters.numColumns!0)?number />
<#assign span = ""/>
<#if numCols !=0>
	<#assign span = "span${parameters.spanSize!4?number}" />
</#if>
<#list 0 .. numCols as place>
	<#if numCols != 0>
         <div class="controls ${span}">
     </#if>
<@s.iterator value="parameters.list" status='checkboxNumber'>
        <#assign itemCount = itemCount + 1/>
    <#if ( numCols == 0 || itemCount % numCols - place == 0)>
    <#if parameters.listKey??>
        <#assign itemKey = stack.findValue(parameters.listKey)/>
    <#else>
        <#assign itemKey = stack.findValue('top')/>
    </#if>

    <#assign itemKeyStr = itemKey.toString() />
    <#if parameters.listValue??>
        <#assign itemValue = stack.findString(parameters.listValue)/>
    <#else>
        <#assign itemValue = stack.findString('top')/>
    </#if>
<label class="checkbox <#if ! parameters.numColumns?has_content>inline</#if>" for="${parameters.id?html}${itemKeyStr?html}"><#rt/>
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
<#if parameters.cssClass??>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle??>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
/><#rt/>
    ${itemValue}<#t/>
</label>
</#if>
</@s.iterator>
<#if numCols != 0>
		</div>
</#if>
</#list>

<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
