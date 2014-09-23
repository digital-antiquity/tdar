<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "batch-common.ftl" as batchCommon>
<head>
    <title>Batch Upload Template</title>
    <meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h1>Bulk Upload</h1>
<#assign projectId_ = -1 />
<#if project?has_content>
    <#assign projectId_ = project.id />
</#if>
<div class="row">
    <div class="span9">
    <@s.form name='BulkMetadataForm' id='BulkMetadataForm'  cssClass="span8 form-horizontal well"  method='post' enctype='multipart/form-data' action='validate-template'>
        <@s.token name='struts.csrf.token' />
        <h2>Basic</h2>
        <@batchCommon.printTemplate />
        <p>Finally, click the "<em>Validate Template</em>" button.</p>

        <@s.hidden name="projectId" value="${projectId_}" />
        <@edit.submit fileReminder=false span="span8" label="Validate Template" />
    </@s.form>
    </div>


    <#--  not a normal behavior -->
    <#-- 
    <#if editor>
        <div class="span3">
            <h2>Advanced</h2>
    
            <p>If you've used the bulk upload tool before, or already have a template that you know works,
                <@s.a href="add?projectId=${(projectId_!-1)?c}">">skip to the upload form</@s.a>.
            </p>
        </div>
    </#if>
    -->
</div>

</div>
