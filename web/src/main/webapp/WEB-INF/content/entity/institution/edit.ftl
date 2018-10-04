<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/content/entity/entity-edit-common.ftl" as entityEdit>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<head>
    <#if ((institution.id)?has_content && institution.id > 0 )>
        <title>Editing ${institution.name}</title>
    <#else>
        <title>Add a new Institution</title>
    </#if>

</head>
<body>
	<div class="row">
    <@s.form  name="institutionForm" id="frmInstitution"  cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} method='post' enctype='multipart/form-data' action='save'>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
    <div class="glide">
        <#if actionName = 'add'>
        <h1>Create New Institution</h1>
        <#else>
        <h1>Institution Information for: ${institution.name}</h1>
        </#if>
        <@s.hidden name="id" />
        <@s.textfield name="name" required=true label="Name" id="txtInstitutionName" cssClass="input-xlarge"  labelposition="left" maxlength=255 />

        <#if editor>
            <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group">
                <@s.select labelposition="left" label="Status" value="institution.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
            </div>
        </#if>

        <@entityEdit.uploadForm />

        <@s.textfield name="institution.url" label="Website" id="txtUrl" cssClass="input-xlarge url"  maxlength=255  labelposition="left"/>

        <@s.textfield name="email" label="Email" id="txtEmail" cssClass="input-xlarge email"  maxlength=255  labelposition="left"/>

        <@s.textarea name="institution.description" label="Description" cssClass="input-xxlarge"  cols="80"  rows="4"/>

        <h3>Address List</h3>
        <@commonr.listAddresses entity=institution entityType="institution" />
		<div class="col-12">
	        <@entityEdit.hidden />
		</div>

    </div>
        <@edit.submit "Save" false />

    </@s.form>
    </div>
    <script type="text/javascript">
        $(function () {
            TDAR.common.initEditPage($('#frmInstitution')[0]);
            $("#clearButton").click(function() {$('#fileUploadField').val('');return false;});
        });
    </script>
</body>
</#escape>
