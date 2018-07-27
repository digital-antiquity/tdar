<#--
/*
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
<#assign partClass><#compress>
<#-- if we have a css class that defines form-control, then ignore it here -->
<#if parameters.cssClass?has_content && parameters.cssClass?contains("form-")>
<#else>
<#switch (parameters.template)>
	<#case "checkbox">
	<#case "checkboxlist">
	<#case "radio">
	<#case "radiomap">
	form-check-input
	<#break>
	form-check-input
	<#break>
	<#case "file">
	form-control-file
	<#break>
	<#default>
	form-control
</#switch>
</#if>
</#compress></#assign>
<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??/>
<#if parameters.cssClass?has_content && !(hasFieldErrors && parameters.cssErrorClass??)>
 class="${parameters.cssClass?html} ${partClass}"<#rt/>
<#elseif parameters.cssClass?has_content && (hasFieldErrors && parameters.cssErrorClass??)>
 class="${parameters.cssClass?html} ${parameters.cssErrorClass?html} ${partClass}"<#rt/>
<#elseif !(parameters.cssClass?has_content) && (hasFieldErrors && parameters.cssErrorClass??)>
 class="${parameters.cssErrorClass?html} ${partClass}"<#rt/>
 <#else>
 class="${partClass}"
</#if>
<#if parameters.cssStyle?has_content && !(hasFieldErrors && (parameters.cssErrorStyle?? || parameters.cssErrorClass??))>
 style="${parameters.cssStyle?html}"<#rt/>
<#elseif hasFieldErrors && parameters.cssErrorStyle??>
 style="${parameters.cssErrorStyle?html}"<#rt/>
</#if>