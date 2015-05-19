<!-- checkboxlist.ftl -->
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
<#if parameters.numColumns??>
   <table class="field checkboxtable <#if parameters.get("cssClass")??>${parameters.get("cssClass")?html}<#rt/></#if>"
   ><#rt><tr>
</#if>
<#if parameters.list??>
    <@s.iterator value="parameters.list" var="key">
        <#assign itemCount = itemCount + 1/>
    <#if parameters.numColumns??>
       <#if itemCount &gt; 1 && itemCount % parameters.numColumns?number == 1>
         </tr><tr>
       </#if>
    </#if>

        <#if parameters.listKey??>
            <#assign itemKey = stack.findValue(parameters.listKey)/>
        <#else>
            <#assign itemKey = stack.findValue('top')/>
        </#if>
		<#if parameters.listValueKey??>
			<#assign itemValue = stack.findString(parameters.listValueKey)/>
			<#assign itemValue><@s.text name="${itemValue}"/></#assign>
	    <#elseif parameters.listValue??>
            <#assign itemValue = stack.findString(parameters.listValue)?default("")/>
        <#else>
            <#assign itemValue = stack.findString('top')/>
        </#if>
        <#if parameters.listTitle??>
            <#assign itemTitle = stack.findString(parameters.listTitle)?default("") />
        </#if>
<#assign itemKeyStr=itemKey.toString() />
<td><input type="checkbox" name="${parameters.name?html}" value="${itemKeyStr?html}" id="${parameters.name?html}-${itemCount}"<#rt/>
        <#if tag.contains(parameters.nameValue, itemKey) >
 checked="checked"<#rt/>
        </#if>
        <#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
        </#if>
        <#if itemTitle??>
 title="${itemTitle}"<#rt/>
        <#elseif parameters.title??>
 title="${parameters.title?html}"<#rt/>
        </#if>
        <#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
/>
<label for="${parameters.name?html}-${itemCount}" class="checkboxLabel" <#rt>
<#if itemTitle??>
 title="${itemTitle}"<#rt/>
</#if>
>${itemValue?html}</label>
</td>    </@s.iterator>
<#else>
  &nbsp;
</#if>
<input type="hidden" id="__multiselect_${parameters.id?html}" name="__multiselect_${parameters.name?html}" value=""<#rt/>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
 /> 
 </table>
 <#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
