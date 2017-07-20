<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#macro inputTags record >
<label>
    <input type="radio" name="authorityId" id="rdoAuthorityId-${record.id?c}" value='${record.id?c}'/>
    <span>${record.id?c}</span>
</label>
    <@s.hidden name='selectedDupeIds' value='${record.id?c}' />
</#macro>

<#macro renderPersonTable>
<table class="tableFormat table ">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Registered</th>
        <th>email</th>
        <th>Institution</th>
    </tr>
    </thead>
    <tbody>
        <#list selectedDupeValues as dupe >
        <tr>
            <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
            <td><a href="<@s.url value="${dupe.detailUrl}"/>">${dupe.name!}</a></td>
            <td>${dupe.registered?string}</td>
            <td>${dupe.email!}</td>
            <td>${dupe.institutionName!}</td>
        </tr>
        </#list>
    </tbody>
</table>
</#macro>

<#macro renderInstitutionTable>
<table class="tableFormat table ">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
    </tr>
    </thead>
    <tbody>
        <#list selectedDupeValues as dupe >
        <tr>
            <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
            <td><a href="<@s.url value="${dupe.detailUrl}"/>">${dupe.name!}</a></td>
        </tr>
        </#list>
    </tbody>
</table>
</#macro>

<#macro renderKeywordTable>
<table class="tableFormat table ">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
    </tr>
    </thead>
    <tbody>
        <#list selectedDupeValues as dupe >
        <tr>
            <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
            <td><a href="<@s.url value="${dupe.detailUrl}"/>">${dupe.label!}</a></td>
        </tr>
        </#list>
    </tbody>
</table>
</#macro>

<div class="glide">
<@s.form action="merge-duplicates" method="post">
    <@s.token name='struts.csrf.token' />
    <@s.hidden name="entityType" />
<#--Include all of the selected dupe ids,  we will need to send them along in the next request also-->
    <h3>Step 2 - Select an Authority Record</h3>
    <#switch entityType>
        <#case "INSTITUTION">
            <@renderInstitutionTable />
            <#break>
        <#case "PERSON">
            <@renderPersonTable />
            <#break>
        <#default>
            <@renderKeywordTable />
    </#switch>

    <h3> Choose Mode</h3>
    <p><label for='MARK_DUPS_ONLY'><input type="radio" name="mode" value='MARK_DUPS_ONLY' id='MARK_DUPS_ONLY' checked="checked" /><span>&nbsp;Mark Dups Only</span></label>
        <blockquote>Use this for almost every case.  In this case, the duplicate is marked as a duplicate, and records that refereced it are discoverable by the original and the dup.  This is useful if someone moves organizations to keep track of what they did where.</blockquote>
    </p>
    <p><label for='MARK_DUPS_AND_CONSOLDIATE'><input type="radio" name="mode" value='MARK_DUPS_AND_CONSOLDIATE' id='MARK_DUPS_AND_CONSOLDIATE' /><span>&nbsp;Mark Dups and consolidate on master</span></label>
    <blockquote>Use this for cases where there's a common misspelling that we want to keep for searching, but all of the records really should point at the same person.</blockquote>
    </p>
    <p><label for='DELETE_DUPLICATES'><input type="radio" name="mode" value='DELETE_DUPLICATES' id='DELETE_DUPLICATES' /><span>&nbsp;Merge &amp; Delete Duplicates</span></label>
    <blockquote>Use this extremely sparingly. This will consolidate resources on the "master" and then delete the duplicates.  This is useful in cases where there's a one-time mistake.</blockquote>
    </p>

    <br/>
    <@s.submit value="Merge selected duplicates" cssClass="btn-primary btn" />
    <a href="index" class="btn">Go back to duplicate selection page</a>
</@s.form>

