<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#macro inputTags record >
    <label>
        <input type="radio" name="authorityId" id="rdoAuthorityId-${record.id?c}" value='${record.id?c}' />
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
        <#list selectedDuplicates as dupe >
            <tr>
                <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
                <td>${dupe.name!}</td>
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
        <#list selectedDuplicates as dupe >
            <tr>
                <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
                <td>${dupe.name!}</td>
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
        <#list selectedDuplicates as dupe >
            <tr>
                <td class="datatable-cell-unstyled"><@inputTags dupe/></td>
                <td>${dupe.label!}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#macro>




<div class="glide">
<@s.form action="merge-duplicates">
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

<@s.select labelposition='top' label='De-Dupe Mode' name='mode' 
     list='%{allDupModes}' title="De-Dupe Modes" />

<br />
<@s.submit value="Merge selected duplicates" />
<a href="index" class="button">Go back to duplicate selection page</a>
</@s.form>

