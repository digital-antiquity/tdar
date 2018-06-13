<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="project"/>
    <#global inheritanceEnabled=false />
    <#import "/WEB-INF/macros/helptext.ftl" as  helptext>

    <#macro topSection>
        <@helptext.projectInheritance />
    </#macro>

    <#macro localSection>
    <h4>Organize the Resources Within a Project</h4>

        <@s.select labelposition='top' label='When Browsing Sort Resource By' name='project.sortBy'
        listValue='label' list='%{sortOptions}' title="Sort resource by" />

        <@s.select labelposition='top' label='Display Project contents as' name='project.orientation'
        list='%{ResultsOrientations}'  listValue='label'  title="Display as" />
    </#macro>

</#escape>

