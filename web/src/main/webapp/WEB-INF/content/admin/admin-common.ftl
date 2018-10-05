<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/settings.ftl" as settings>
<#import "/WEB-INF/macros/common.ftl" as common>
<title>Admin Pages</title>
    <#setting url_escaping_charset="UTF-8">
    <#macro header>
    <div>
        <h3> Menu</h3>
        <ul class="nav nav-tabs">
            <li class="nav-item"><a class="nav-link" href="<@s.url value="/admin/"/>">Admin Home</a></li>
            <li class='dropdown nav-item'>
            <a class='dropdown-toggle nav-link' data-toggle='dropdown' href='#'>Statistics <b class='caret'></b></a>
            <ul class='dropdown-menu'>
                <li class="dropdown-item"><a href="<@s.url value="/admin/resource"/>">Resource Statistics</a></li>
                <li class="dropdown-item"><a href="<@s.url value="/admin/user"/>">User Statistics</a></li>
                <li class="dropdown-item"><a href="<@s.url value="/admin/keyword-stats"/>">Keyword Statistics</a></li>
            </ul>
            </li>
            <li class="nav-item"><a class=" nav-link" href="<@s.url value="/admin/file-info"/>">File Information</a></li>
            <li class="nav-item"><a class=" nav-link" href="<@s.url value="/admin/authority/index"/>">DeDupe</a></li>
            <#if billingManager || editor>
                <li class="dropdown nav-item">
                    <a class='dropdown-toggle nav-link' data-toggle='dropdown' href='#'>Billing <b class='caret'></b></a>
                    <ul class='dropdown-menu'>
                        <li class="dropdown-item"><a href="<@s.url value="/billing/list"/>">List Billing Accounts</a></li>
                        <li class="dropdown-item"><a href="<@s.url value="/billing/listInvoices"/>">List Invoices</a></li>
                    </ul>
                </li>
            </#if>
            <#if administrator >
                <li class="nav-item"><a  class=" nav-link" href="<@s.url value="/admin/system/activity"/>">System Activity</a></li>
                <li class="nav-item"><a class=" nav-link" href="<@s.url value="/admin/searchindex/build"/>">Reindex</a></li>
                <li class="nav-item"><a class=" nav-link" href="<@s.url value="/admin/notifications/index"/>">Notifications</a></li>
            </#if>
            <#if editor >
                <li class="nav-item"><a class=" nav-link" href="<@s.url value="/admin/email"/>">Email</a></li>
            </#if>
        </ul>
    </div>
    </#macro>


    <#macro statsTable statsObj header="HEADER" cssid="CSS_ID" valueFormat="number">
        <#assign height=225/>
    <div class="glide">
        <h3>${header}</h3>

        <div id="graph${cssid}" style="height:${height}px" class="lineGraph" data-table="#table${cssid}"></div>
        <#assign statsObjKeys = statsObj?keys?sort?reverse />
        <#assign numSets = 0/>
        <#assign totalRows = 0/>
        <table class="table table-sm table-striped"" id="table${cssid}">
            <#assign first = true/>
            <#list statsObjKeys as key>
                <#assign vals = statsObj.get(key) />
                <#assign valsKeys = vals?keys />
                <#if first>
                      <thead class="thead-dark">

                    <tr>
                        <th>Date</th>
                        <#list valsKeys as key_>
                            <th>
                            ${key_.label}
                            </th>
                            <#if (numSets < key__index )>
                                <#assign numSets = key__index />
                            </#if>
                        </#list>
                    </tr>
                    </thead>
                <#else>
                    <#assign totalRows = totalRows +1 />
                </#if>
                <#assign first = false/>
                <tr class="<#if (totalRows > 15)>hidden</#if>">
                    <td>
                    ${key?iso_utc}
                    </td>

                    <#list valsKeys as key_>
                        <td>
                            <#if valueFormat == "number">
                   ${vals.get(key_)?default("0")}
                <#elseif valueFormat == "filesize">
                                <@common.convertFileSize filesize=vals.get(key_)?default("0") />
                            </#if>
                        </td>
                    </#list>
                </tr>
            </#list>
            <#if (totalRows > 15)>
                <tr>
                    <td><a href="#"
                           onClick="$(this).parents('table').find('tr').removeClass('hidden');$(this).parent().parent().addClass('hidden');return false;">show
                        all</a></td>
                </tr>
            </#if>

        </table>

    </div>

    </#macro>
    <#macro append v1 delim><#if v1?? && v1 != ''>${delim!""}${v1}</#if></#macro></#escape>
