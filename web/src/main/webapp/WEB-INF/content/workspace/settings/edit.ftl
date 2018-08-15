<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
	<#import "/WEB-INF/macros/common.ftl" as common>
<head>
    <title>${persistable.name!"Your Integration"}</title>
</head>
<body>

<h2>Edit Integration Settings: ${persistable.name} </h2>



<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal tdarvalidate" enctype='multipart/form-data' action='save'
        dynamicAttributes={"data-validate-method":"initBasicForm"}>
        <@edit.hiddenStartTime />
        <@common.jsErrorLog />

        <@s.textfield name="persistable.name" cssClass="input-xlarge" label="Integration Name"/>
        <@s.textarea name="persistable.description" cssClass="input-xlarge" label="Integration Description"  cols="80"  />

        <label class="control-label col-form-label">Allow anyone to view this Integration?</label>
        <br/>
        <div class="form-check form-check-inline">
                <div class="form-check">
                    <input type="radio" id="rdoheaderTrue" name="persistable.hidden" class="form-check-input form-check-inline" value="false" <@commonr.checkedif persistable.hidden false /> />
                    <label for="rdoheaderTrue" class="radio form-check-label">Yes</label>
                </div>
                <div class="form-check">
                    <input type="radio" id="rdoheaderFalse" name="persistable.hidden" class="form-check-input form-check-inline" value="true" <@commonr.checkedif persistable.hidden true /> />
                    <label for="rdoheaderFalse" class="radio form-check-label">No</label>
                </div>
        </div>
        
<br/>
<br/>
<br/>
        <h5>Users who can view/run this integration</h5>
        <@edit.listMemberUsers false />

        <@s.hidden name="id" value="${persistable.id?c!-1}" />
        <@edit.submit fileReminder=false />
        
</@s.form>

<script>
    $(document).ready(function () {
        'use strict';
        TDAR.common.initEditPage($('#MetadataForm')[0]);
        TDAR.autocomplete.delegateCreator('#accessRightsRecords', true, false);
    });
</script>


</body>

</#escape>