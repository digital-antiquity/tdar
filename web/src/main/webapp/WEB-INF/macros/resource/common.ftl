<#escape _untrusted as _untrusted?html>
    <#import "/${themeDir}/settings.ftl" as settings />
<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#-- opting out of code formatting: begin -->
<#--@formatter:off-->
<#-- opting out of code formatting: end -->
<#--@formatter:on-->


<#-- emit specified number of bytes in human-readable form  -->
<#-- @parm filesize:number? file size in bytes -->
    <#macro convertFileSize filesize=0><#t>
        <#compress>
            <#local mb = 1048576 />
            <#local kb = 1024 />
            <#if (filesize > mb)>
            ${(filesize / mb)?string(",##0.00")}mb
            <#elseif (filesize > kb)>
            ${(filesize / kb)?string(",##0.00")}kb
            <#else>
            ${filesize?string(",##0.00")}b
            </#if>
        </#compress><#t>
    </#macro>
<#-- string representing current tdar version/build number -->
    <#assign tdarBuildId>
        <#attempt><#include  "/version.txt" parse=false/><#recover></#attempt>
    </#assign>
    <#assign tdarBuildId = (tdarBuildId!'unknown')?trim?replace("+", ".001") />

<#--
//Emit Javascript intended for every page in tDAR (regardless of login status)
-->
    <#macro globalJavascript>
    <script type="text/javascript">
    </script>
    <@googleAnalyticsJavascript />
    </#macro>

<#-- emit the javascript necessary for google analytics -->
<#-- FIXME: replace this embed and wrapper functions as part of upgrade to Universal Analytics (TDAR-3515) -->
    <#macro googleAnalyticsJavascript>
    <#noescape>
    <script>
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

        ga('create', '${googleAnalyticsId}', 'auto');
        ga('send', 'pageview');

    </script>
    </#noescape>
    </#macro>

<#--
    Emit login button link.
    If current page is home page, link has no querystring arguments.  Otherwise,  include the current url in the
    querystring (in parameter named 'url).
-->
    <#macro loginButton class="" returnUrl="">
        <#noescape>
        <#local _current = (currentUrl!'/') >
        <#if returnUrl != ''><#local _current = returnUrl /></#if>
        <#if _current == '/' || currentUrl?starts_with('/login')>
        <a class="${class}" href="<@s.url value='/login'/>" rel="nofollow" id="loginButton">Log In</a>
        <#else>
        <a class="${class}" rel="nofollow" href="<@s.url value='/login'><@s.param name="url">${_current}</@s.param></@s.url>" id="loginButton">Log In</a>
        </#if>
        </#noescape>
    </#macro>

<#--Render the "Access Permissions" section of a resource view page.  Specifically, this section shows
  the collections associated with the resource and the users + permission assigned to the resource. -->
<#-- @param collections:list? a list of resourceCollections -->
<#-- @param owner:object? Person object representing the collection owner
<#-- FIXME:  both of these parameters have invalid defaults. consider making them mandatory  -->
    <#macro resourceCollectionsRights collections=effectiveResourceCollections_ owner="">
        <#if collections?has_content>
        <h3>Access Permissions</h3>
            <#nested />
        <table class="tableFormat table">
            <thead>
            <tr>
                <th>Collection</th>
                <th>User</th>
                <#list availablePermissions as permission>
                    <th>${permission.label}</th>
                </#list>
            </tr>
                <#if owner?has_content>
                <tr>
                    <td>Local Resource</td>
                    <td>${owner.properName} (Submitter)</td>
                    <td><i class="icon-ok"></i></td>
                    <td><i class="icon-ok"></i></td>
                    <td><i class="icon-ok"></i></td>
                </tr>
                </#if>
                <#list collections as collection_ >
                    <#if collection_.authorizedUsers?has_content >
                        <#list collection_.authorizedUsers as user>
                        <tr>
                            <td>
                                <#if !collection_.internal>
                                    <a href="<@s.url value="${collection_.detailUrl}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
                                <#else>
                                    Local Resource
                                </#if>
                            </td>
                            <td>
                            ${user.user.properName} <!-- ${user.user.properName}:${user.generalPermission} -->
                            </td>
                            <#list availablePermissions as permission>
                                <td>
                                    <#if (user.generalPermission.effectivePermissions >= permission.effectivePermissions )>
                                        <i class="icon-ok"></i>
                                    <#else>
                                        <i class="icon-remove"></i>
                                    </#if>
                                </td>
                            </#list>
                        </tr>
                        </#list>
                    </#if>
                </#list>
        </table>
        </#if>
    </#macro>

<#--emit the specified string, truncating w/ ellipses if length exceeds specified max -->
<#-- @param text:string the text to render -->
<#-- @param len:number? maximum length of the string-->
    <#macro truncate text len=80>
        <#compress>
            <#if text??>
            <#-- if text if greater than length -->
                <#if (text?length > len)>
                <#-- set pointer to last space before length (len) -->
                    <#local ptr=text?last_index_of(" ",len) />
                <#-- if pointer to last space is greater than 1/2 of the max length, truncate at the pointer,
                      otherwise truncate at 3 before length -->
                    <#if (ptr > len / 2)>
                    ${text?substring(0,ptr)}...
                    <#else>
                    ${text?substring(0,len -3)}...
                    </#if>
                <#else>
                ${text}
                </#if>
            </#if>
        </#compress>
    </#macro>

<#--function version of #truncate. -->
    <#function fnTruncate text len=80>
        <#if (text?length <= len)><#return text></#if>

        <#local subtext = text?substring(0, len-3)?trim>
        <#local words = subtext?split(' ')>
        <#--if substring has several words,  cut on the last word -->
        <#if (words?size > 3)>
            <#local subtext = text?substring(0,text?last_index_of(' '))>
        </#if>
        <#return "${subtext}...">
    </#function>

<#-- Emit the container and script for a bar graph -->
    <#macro resourceBarGraph>
		<div id="resourceBarGraph" style="height:400px" data-source="#homepageResourceCountCache" class="barChart"
		data-x="label" data-values="count" data-click="resourceBarGraphClick" data-yaxis="log" data-colorcategories="true" >
		</div>
		<#noescape>
		<script id="homepageResourceCountCache" type="application/json">
		${homepageGraphs.resourceTypeJson}
		</script>
		</#noescape>
	
    </#macro>

<#-- Emit container div and script for the worldmap control. The worldmap control shows the number of registeresd
 resources for a country as the user hovers their mouse over a country.  If the user clicks on a country,
 the browser redirects to a search results that limits results to show only ressources that have geographic boundaries
 that lie within the selected country -->
<#-- @param forceAddSchemeHostAndPort:boolean if true, clickhandler always includes hostname and port when bulding
            the redirect url.  If false,   the clickhandler builds a url based on the current hostname and port -->
    <#macro renderWorldMap forceAddSchemeHostAndPort=false mode="horizontal">
    <div class=" <#if mode == 'vertical'>span7<#elseif mode == 'horizontal'>span6 map mapcontainer<#else> mapcontainer</#if> ${mode}">
            <h3>${siteAcronym} Worldwide</h3>
        <script type="application/json" data-mapdata>
			<#noescape>${homepageGraphs.mapJson}</#noescape>
        </script>
        <script type="application/json" data-locales>
			<#noescape>${homepageGraphs.localesJson}</#noescape>
        </script>

             <div id="worldmap" style="height:<#if mode == 'mini'>150<#else>350</#if>px" data-max="">
             </div>
        <#if mode =='vertical'></div></#if>
             <div id="mapgraphdata"  <#if mode == 'vertical'>data-mode="vertical" class="span4 offset1"<#else>style="width:100%"</#if>>
        <#if mode =='vertical'><br/><br/></#if>
                 <h5 id="mapGraphHeader"></h5>
                 <div id='mapgraphpie'>                 
                 </div>
             </div>
        <#if mode !='vertical'></div></#if>
	<script>
	$(function() {
    	TDAR.worldmap.initWorldMap("worldmap","${mode}");
	});
	</script>
    </#macro>

    <#macro cartouche persistable useDocumentType=false>
        <#local cartouchePart><@upperPersistableTypeLabel persistable /></#local>
    <span class="cartouche">
    <#if persistable.resourceType?has_content>
        <#local type>svg-icons_icon-${persistable.resourceType?lower_case}</#local>
    <#elseif persistable.type?has_content><#t>
        <#local type>svg-icons_collection</#local>
    </#if>
    <svg class="svgicon white svg-cartouche"><use xlink:href="/images/svg/symbol-defs.svg#${type}"></use></svg>
    <#--        <#if (persistable.status)?? && !persistable.active>
            ${persistable.status} <#t>
        </#if>  -->
        <@upperPersistableTypeLabel persistable />
     </span>
        <#nested />
    </#macro>

<#--FIXME: persistable getType() doesn't exist. define it, then override  in Resource.  -->
<#-- Emit the specified persistable's 'type' in uppercase (e.g. DOCUMENT, SENSORYDATA, COLLECTION...) -->
<#-- @param persistable:Persistable either a persistable instance or object instance or dedupable instance -->
    <#macro upperPersistableTypeLabel persistable>
        <#if persistable.resourceType?has_content><#t>
        ${persistable.resourceType?replace("_", " ")?upper_case} <#t>
        <#elseif persistable.type?has_content><#t>
        COLLECTION<#t>
        <#else> <#t>
        PERSISTABLE<#t>
        </#if>
    </#macro>

<#-- emit login menu list items -->
<#-- @param showMenu:boolean if true,  wrap list items in UL tag, otherwise just emit LI's -->
    <#macro loginMenu showMenu=false>
        <#if showMenu>
        <ul class="subnav-rht hidden-phone hidden-tablet">
        </#if>
        <#if !(authenticatedUser??) >
            <li><a href="<@s.url value="/account/new" />" class="button" rel="nofollow">Sign Up</a></li>
            <li><@loginButton class="button" /></li>
        <#else>
            <#--<li><a href="<@s.url value="/logout" />" class="button">Logout</a></li>-->
            <li>
            <form class="form-unstyled seleniumIgnoreForm logoutForm" id="frmLogout" name="logoutForm" method="post" action="/logout">
                    <input type="submit" class="tdar-button" name="logout" value="Logout" id="logout-button">
            </form>
            </li>
        </#if>
        <#if showMenu>
        </ul>
        </#if>
    </#macro>

<#-- Render the "Resourse Usage" section of a view page.   -->
    <#macro resourceUsageInfo>
        <#local _isProject =  ((persistable.resourceType)!'') == "PROJECT" >
        <#if uploadedResourceAccessStatistic?has_content >
        <table class="table tableFormat">
            <tr>
                <#if _isProject >
                    <th>Total # of Resources</th></#if>
                <th>Total # of Files</th>
                <th>Total Space (Uploaded Only)</th>
            </tr>
            <tr>
                <#if _isProject>
                    <td>${uploadedResourceAccessStatistic.countResources!0}</td></#if>
                <td>${uploadedResourceAccessStatistic.countFiles!0}</td>
                <td><@convertFileSize uploadedResourceAccessStatistic.totalSpace!0 /></td>
            </tr>
        </table>
        </#if>
    </#macro>

<#-- emit a div that lists the addresses for the specified person.-->
<#-- @param entity:Person person object containing addresses to render -->
<#-- @param entityType:string type of entity ("person" or "institution" )-->
<#-- @param choiceField:string FIXME: no clue what this means.  parsed as boolean,  caller supplies a number, compared to a string -->
<#-- @addressId:number id of the address to be considered the 'current' address in the context of the page displaying this macro  -->
    <#macro listAddresses entity=person entityType="person" choiceField="" addressId=-1>
    <div class="row">
        <#list entity.addresses  as address>
            <div class="span3">
                <#local label = ""/>
                <#if address.type?has_content>
                    <#local label = address.type.label>
                </#if>
                <#if choiceField?has_content>
                <label class="radio inline">
                    <input type="radio" name="invoice.address.id" label="${label}"
                           value="${address.id}"
                           <#if address.id==addressId || (!addressId?has_content || addressId == -1) && address_index==0>checked=checked</#if>/>
                </#if>

                <@printAddress  address=address creatorId=entity.id creatorType=entityType modifiable=true showLabel=false >
                    <b><#if address.type?has_content>${address.type.label!""}</#if></b>
                </label><br/>
                </@printAddress>
            </div>
        </#list>
        <div class="span3">
            <#local retUrl><@s.url includeParams="all"/></#local>
            <a class="button btn btn-primary submitButton" href="/entity/address/${entity.id?c}/add?returnUrl=${retUrl?url}">Add Address</a>
        </div>
    </div>
    </#macro>

<#-- emit a single formatted address -->
<#-- @param address:Address? address object to render (default: valueStack.address) -->
<#-- @param creatorId:number? id for persisted value (default: -1) -->
<#-- @param creatorType:string?  either "person" or "institution"  (default: "person") -->
<#-- @param modifiable:boolean? render as a editable form fields (default:false)-->
<#-- @param deletable:boolean? render a delete button  (default:false) -->
<#-- @param showLabel:boolean? show the address.addressType.label value (default:false) -->
    <#macro printAddress address=address creatorId=-1 creatorType='person'  modifiable=false deletable=false showLabel=true>
    <p>
        <#if address.type?has_content && showLabel><b>${address.type.label!""}</b><br></#if>
        <span>${address.street1}<br/>
        ${address.street2}</span><br/>
        <span>${address.city}</span>, <span>${address.state}</span>, <span
            >${address.postal}</span><br/>
        <span>${address.country}</span><#if modifiable><br/>
        <a href="<@s.url value="/entity/address/${creatorId?c}/${address.id?c}"/>"><@s.text name="menu.edit" /></a>
    </#if><#if deletable && modifiable> |</#if>
        <#if deletable>
        	<@s.form method="POST" action="/entity/address/delete-address">
				<@s.hidden name="id" value="${creatorId?c}"/>
				<@s.hidden name="addressId" value="${address.id?c}"/>
            	<@s.button name="menu.delete" />
			</@s.form>
        </#if>
    </p>
    </#macro>

<#-- emit "checked" if arg1==arg2 -->
    <#macro checkedif arg1 arg2><#t>
        <@valif "checked='checked'" arg1 arg2 />
    </#macro>

<#-- emit "selected" if arg1==arg2 -->
    <#macro selectedif arg1 arg2>
        <@valif "selected='selected'" arg1 arg2 />
    </#macro>

<#-- emit val if arg1==arg2 -->
    <#macro valif val arg1 arg2><#t>
        <#if arg1=arg2>${val}</#if><#t>
    </#macro>

<#-- emit a "combobox" control.  A combobox is essentially text field element that features both autocomplete support as
 as the ability to view a list of all possible values (by clicking on a 'dropdown' button beside the text box)-->
    <#macro combobox name target autocompleteIdElement placeholder  cssClass value=false autocompleteParentElement="" label="" bootstrapControl=true id="" addNewLink="">
        <#if bootstrapControl>
        <div class="control-group">
            <label class="control-label">${label}</label>
        <div class="controls">
        </#if>
        <div class="input-append">
        <#--if 'value' is not a string,  omit the 'value' attribute so that we don't override the
        s.textfield default (i.e. the value described by the 'name' attribute) -->
            <#if value?is_string>
                <@s.textfield theme="simple" name="${name}"  target="${target}"
                label="${label}"
                autocompleteParentElement="${autocompleteParentElement}"
                autocompleteIdElement="${autocompleteIdElement}"
                placeholder="${placeholder}"
                value="${value}" cssClass="${cssClass}" />
            <#else>
                <@s.textfield theme="simple" name="${name}"  target="${target}"
                label="${label}"
                autocompleteParentElement="${autocompleteParentElement}"
                autocompleteIdElement="${autocompleteIdElement}"
                placeholder="${placeholder}" cssClass="${cssClass}" />
            </#if>
            <button type="button" class="btn show-all"><i class="icon-chevron-down"></i></button>
            <#if addNewLink?has_content>
                <a href="${addNewLink}" onClick="TDAR.common.setAdhocTarget(this, '${autocompleteParentElement?js_string}');" class="btn show-all"
                   target="_blank">add new</a>
            </#if>
        </div>
        <#if bootstrapControl>
        </div>
        </div>
        </#if>
    </#macro>

<#-- emit the actionmessage section.  If many action messages present,  then wrap them in a collapsed accordian div  -->
    <#macro actionmessage>
        <#if (actionMessages?size>5) >
        <div class="alert alert-info">
            <span class="badge badge-info">${actionMessages?size}</span>
            <a href="#" data-toggle="collapse" data-target="#actionmessageContainer">System Notifications</a>
        </div>
        <div id="actionmessageContainer" class="collapse">
            <@s.actionmessage />
        </div>
        <#else>
            <@s.actionmessage  />
        </#if>
    </#macro>

<#-- emit the billing account list section -->
<#-- @param accountList:List<Account> list of accounts to render -->
    <#macro billingAccountList accountList>
        <#if (payPerIngestEnabled!false)>
        <h2 id="billingSection">Billing Accounts</h2>
        <ul>
            <#list accountList as account>
                <#if account.active>
                <li>
                    <a href="<@s.url value="/billing/${account.id?c}"  />">${account.name!"unamed"}</a>
                </li>
                </#if>
            </#list>
<#--
            <#if billingManager>
                <li><a href="<@s.url value="/billing/list" />">All Accounts</a></li>
            </#if> -->
            <li><a href="/cart/add">Create a new account or add more to an existing one</a></li>
        </ul>
        </#if>
    </#macro>

<#-- emit notice indicating that the system is currently reindexing the lucene database -->
    <#macro reindexingNote>
        <#if reindexing!false >
        <div class="reindexing alert">
            <p><@localText "notifications.fmt_system_is_reindexing", siteAcronym /></p>
        </div>
        </#if>
    </#macro>

<#-- Emit a resource description (replace crlf's with <p> tags-->
    <#macro description description_="No description specified." >
        <#assign description = description_!"No description specified."/>
    <p>
        <#noescape>
    ${(description)?html?replace("[\r\n]++","</p><p>","r")}
  </#noescape>
    </p>
    </#macro>

<#-- emit html for single collection list item -->
    <#macro collectionListItem depth=0 collection=collection showOnlyVisible=false >
        <#if !collection.hidden || collection.viewable || showOnlyVisible==false >
            <#local clsHidden = "", clsClosed="" >
            <#if (depth < 1)><#local clsHidden = "hidden"></#if>
            <#if ((depth < 1) && (collection.transientChildren?size > 0))><#local clsClosed = "closed"></#if>
        <li class="${clsClosed}">
            <@s.a href="${collection.detailUrl}">${(collection.name)!"No Title"}</@s.a>
            <#if collection.transientChildren?has_content>
                <ul class="${clsHidden}">
                    <#list collection.transientChildren as child>
                    <@collectionListItem depth= (depth - 1) collection=child showOnlyVisible=showOnlyVisible  />
                </#list>
                </ul>
            </#if>
        </li>
        </#if>
    </#macro>

<#-- emit the collections list in a 'treeview' control -->
    <#macro listCollections collections=resourceCollections_ showOnlyVisible=false>
    <ul class="collection-treeview">
        <#list collections as collection>
            <@collectionListItem collection=collection showOnlyVisible=showOnlyVisible depth=3/>
        </#list>
  <#nested>
    </ul>
    </#macro>

    <#macro jsErrorLog>
    <textarea style="display:none" name="javascriptErrorLog" id="javascriptErrorLog" class="devconsole oldschool input-block-level" rows="10" cols="20"
              maxlength="${(160 * 80 * 2)?c}">${javascriptErrorLogDefault!'NOSCRIPT'}</textarea>
    <script>document.getElementById('javascriptErrorLog').value = "";</script>
    </#macro>


<#-- a slightly more concise way to emit i18n strings.
    name:String string key name
    parms...?:varargs<String> (optional) any additional arguments are treated as MessageFormat parameters
    
    NOTE: DO NOT CHANGE THE NAME OF THIS MACRO -- IT'S USED IN TESTS TO GREP THROUGH THE CODE
-->
    <#macro localText name parms...>
        <@s.text name="${name}"><#list parms as parm><@s.param>${parm}</@s.param></#list></@s.text>
    </#macro>

<#-- emit the coding rules section for the current coding-sheet resource. Used on view page and edit page -->
    <#macro codingRules>
        <#if resource.id != -1>
            <#nested>
        <h3 onClick="$(this).next().toggle('fast');return false;">Coding Rules</h3>
            <#if resource.codingRules.isEmpty() >
            <div>
                No coding rules have been entered for this coding sheet yet.
            </div>
            <#else>
            <div id='codingRulesDiv'>
                <table width="60%" class="table table-striped tableFormat">
                    <thead class='highlight'>
                    <tr>
                        <th>Code</th>
                        <th>Term</th>
                        <th>Description</th>
                        <th>Mapped Ontology Node</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#assign hasSpecial=false />
                        <#list resource.sortedCodingRules as codeRule>
                        	<#if !(codeRule.code?starts_with("__")) >
	                        <tr>
	                            <td>${codeRule.code}</td>
	                            <td>${codeRule.term}</td>
	                            <td>${codeRule.description!""}</td>
	                            <td><#if codeRule.ontologyNode?has_content>${codeRule.ontologyNode.displayName!'Unlabeled'}</#if></td>
	                        </tr>
                            <#else>
                              <#assign hasSpecial=true />
	                    	</#if>
                        </#list>

                        <tr>
                        <td colspan=4><b>Special Coding Rules:</b> These entries are not in the coding-sheet, but represent edge-cases in Data Integration that may benefit from custom mappings</td>
                        </tr>
                       <#if hasSpecial>
                        <#list resource.sortedCodingRules as codeRule>
                        	<#if codeRule.code?starts_with("__") >
	                        <tr>
	                            <td></td>
	                            <td>${codeRule.term}</td>
	                            <td>${codeRule.description!""}</td>
	                            <td><#if codeRule.ontologyNode?has_content>${codeRule.ontologyNode.displayName!'Unlabeled'}</#if></td>
	                        </tr>
	                    	</#if>
                        </#list>
                        </#if>

                        <#list missingCodingKeys![]>
                        <tr>
                        <td colspan=4><b>The following coding rules exist in datasets mapped to this coding sheet, but are not in the coding sheet</b></td>
                        </tr>
                        <#items as missing>
                        <tr>
                            <td class="red">${missing}</td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                        </#items>
                        </#list>
                        	

                    </tbody>
                </table>
            </div>
            </#if>
        </#if>
    </#macro>

<#--FIXME:  there has to be a better way here -->
    <#macro antiSpam>
        <#if h.recaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${h.recaptcha_public_key}"></script>
        </#if>
    
        <@s.hidden name="h.timeCheck"/>
        <textarea name="h.comment" class="tdarCommentDescription" style="display:none"></textarea>

        <#if h.reCaptchaText?has_content>
            ${h.reCaptchaText}
        </#if>
    </#macro>

    <#macro embeddedAntiSpam  bean="downloadRegistration">
        <#local actual = bean?eval />
        <#if actual.srecaptcha_public_key??>
        <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=${actual.h.recaptcha_public_key}"></script>
        </#if>
    
        <@s.hidden name="${bean}.h.timeCheck"/>
        <textarea name="${bean}.h.comment" class="tdarCommentDescription" style="display:none"></textarea>

        <#if actual.h.reCaptchaText?has_content>
            ${actual.h.reCaptchaText}
        </#if>
    </#macro>


<#-- remove chrome autofill hack when no longer necessary TDAR-4043 -->
<#--starting w/ Chrome 34, chrome ignores the autocomplete=off directive in password fields.  This in itself is not so bad (really), but it leads to
tdar usability issues when combined with Chrome's "login form detection".  Specifically, chrome always autofills the first password input it encounters (regardless
if there are multiple password inputs, e.g. a confirm-password-change form),  and then assumes that the preceeding text field is a username field (which is not
true for our registration page or our profile page).-->
<#macro chromeAutofillWorkaround>
<input type="text"  name="_cr42-1" value="" style="display:none">
<input type="password" name="_cr42-2" value="" style="display:none">
</#macro>


<#-- Create a search-link for a keyword -->
    <#macro searchFor keyword=keyword asList=true showOccurrence=false>
        <#if asList><li class="bullet"></#if>
            <a href="<@s.url value="${keyword.detailUrl}" />">${keyword.label}
            <#if showOccurrence && keyword.occurrence?has_content && keyword.occurrence != 0 >(${keyword.occurrence?c})</#if>
            </a>
        <#if asList></li></#if>
    </#macro>

    <#macro featuredCollection featuredCollection>
        <h3>Featured Collection</h3>
        <p>
    <#if logoAvailable>
        <img class="pull-right collection-logo" src="/files/collection/sm/${featuredCollection.id?c}/logo"
        alt="logo" title="logo" /> 
    </#if>
    <a href="${featuredCollection.detailUrl}"><b>${featuredCollection.name}</b></a>: ${featuredCollection.description}</p>
    </#macro>

</#escape>

