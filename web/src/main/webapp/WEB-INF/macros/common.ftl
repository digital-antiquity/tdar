<#escape _untrusted as _untrusted?html>




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

        ga('create', '${config.googleAnalyticsId}', 'auto');
        ga('set', 'transport', 'beacon');
        ga('set', 'dimension1', '<#if administrator>administrator<#elseif editor>editor<#elseif authenticatedUser??>user<#else>anonymous</#if>');

        ga('send', 'pageview');

    </script>
    </#noescape>
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
    
    
    <#macro jsErrorLog>
    <textarea style="display:none" name="javascriptErrorLog" id="javascriptErrorLog" class="devconsole oldschool input-block-level" rows="10" cols="20"
              maxlength="${(160 * 80 * 2)?c}">${javascriptErrorLogDefault!'NOSCRIPT'}</textarea>
    <script>document.getElementById('javascriptErrorLog').value = "";</script>
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
        <#if (config.payPerIngestEnabled!false)>
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

    

<#-- a slightly more concise way to emit i18n strings.
    name:String string key name
    parms...?:varargs<String> (optional) any additional arguments are treated as MessageFormat parameters
    
    NOTE: DO NOT CHANGE THE NAME OF THIS MACRO -- IT'S USED IN TESTS TO GREP THROUGH THE CODE
-->
    <#macro localText name parms...>
        <@s.text name="${name}"><#list parms as parm><@s.param>${parm}</@s.param></#list></@s.text>
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


    
<#-- Emit a resource description (replace crlf's with <p> tags-->
    <#macro description description_="No description specified." >
        <#assign description = description_!"No description specified."/>
    <p>
        <#noescape>
    ${(description)?html?replace("[\r\n]++","</p><p>","r")}
  </#noescape>
    </p>
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

<#macro listUsers users=[] span=10 baseUrl="/entity/user/rights" well=true>
        <div class="<#if well>row</#if>" id="sharedPeople">
            <div class="<#if well>well</#if> span-${span}">
                <div class="row">
                    <#assign showMore=false />
                    <#assign listGroups = [users]>
                    <#if (users?size > (span/2-1))><#assign listGroups =  users?chunk(users?size /(span/2-1) )> </#if>
                    <#list listGroups as row>
                        <div  class="col">
                            <#list row>
                            <ul class="unstyled">
                            <#items as item>
                                <li class="<#if (item_index > 3)>hidden<#assign showMore=true /></#if>">
	                           <#if authenticatedUser?has_content>
    	                            <a id="p${item.id?c}" href="${baseUrl}/${item.id?c}">${item.properName}</a>
                                <#else>
	                                ${item.properName}
                                </#if>
                                </li>
                            </#items>
                            </ul>
                            </#list>
                        </div>
                    </#list>
                    </div>
                    <#if showMore>
                        <div span="${span}">
                            <p class="text-center"><a href="#"  onClick="$('#sharedPeople .hidden').removeClass('hidden');$(this).hide()"><b>show more</b></a></p>
                        </div>
                    </#if>
                </div>
            </div>
</#macro>

</#escape>