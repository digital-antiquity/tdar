<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>

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

        <div class="control-group">
        <label class="control-label">Allow anyone to view this Integration?</label>
    
            <div class="controls">
                <label for="rdoheaderTrue" class="radio inline"><input type="radio" id="rdoheaderTrue" name="collection.customHeaderEnabled"
                                                                        value="true" <@common.checkedif persistable.hidden false /> />Yes</label>
                <label for="rdoheaderFalse" class="radio inline"><input type="radio" id="rdoheaderFalse" name="collection.customHeaderEnabled"
                                                                         value="false" <@common.checkedif persistable.hidden true /> />No</label>
            </div>
        </div>
        

        <p><b>Users who can view/run this integration</b></p>
        <@edit.listMemberUsers />

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

    <@edit.personAutocompleteTemplate />

</body>

</#escape>