<div class="form-check">
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
<#if parameters.numColumns??>
   <div class="row">
   <div class="field col${parameters.numColumns} col-sm "><#rt>
</#if>

<@s.iterator value="parameters.list" status='radioNumber'>
<div class="form-check <#if parameters.inline?has_content && parameters.inline == true>form-check-inline</#if>">
    <#if parameters.listKey??>
        <#assign itemKey = stack.findValue(parameters.listKey)/>
    <#else>
        <#assign itemKey = stack.findValue('top')/>
    </#if>
    <#if parameters.numColumns??>
       <#if (radioNumber.index &gt; 0 && (radioNumber.index + 1) % parameters.numColumns?number == 1) || parameters.numColumns?number == 1 >
         <br/>
       </#if>
    </#if>

    <#assign itemKeyStr = itemKey.toString() />
	<#if parameters.listValueKey??>
		<#assign itemValue = stack.findString(parameters.listValueKey)/>
		<#assign itemValue><@s.text name="${itemValue}"/></#assign>
    <#elseif parameters.listValue??>
        <#assign itemValue = stack.findString(parameters.listValue)/>
    <#else>
        <#assign itemValue = stack.findString('top')/>
    </#if>
<input type="radio"<#rt/>
<#if parameters.name??>
 name="${parameters.name?html}"<#rt/>
</#if>
 id="${parameters.id?html}${itemKeyStr?html}"<#rt/>
<#if tag.contains(parameters.nameValue?default(''), itemKeyStr)>
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
<#if parameters.title??>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/${parameters.theme}/css.ftl" />
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
/><#rt/>
<label for="${parameters.id?html}${itemKeyStr?html}"  class="form-check-label"><#rt/>
    ${itemValue}<#t/>
</label>
</div>
</@s.iterator>
<#if parameters.numColumns??>
   </div>
   </div>
</#if>
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
</div>