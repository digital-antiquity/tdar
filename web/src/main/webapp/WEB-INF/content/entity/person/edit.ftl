<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/content/entity/entity-edit-common.ftl" as entityEdit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
    <#assign pageTitle = "Add a new User">

    <#if (editingSelf)>
        <#assign pageTitle = "Your Profile: ${person.properName!'n/a'}">

    <#elseif person.id != -1>
        <#assign pageTitle = "Editing: ${person.properName!'n/a'}" >
    </#if>
    <title>${pageTitle}</title>

    <style type="text/css">
        label.error {
            display: block;
        }
    </style>
</head>
<body>
<h1>${pageTitle}</h1>

    <@s.form name='personForm' id='frmPerson'  cssClass="form-vertical tdarvalidate"  method='post' enctype='multipart/form-data' action='save'
            dynamicAttributes={"data-validate-method":"initBasicForm"}>
        <@s.token name='struts.csrf.token' />
        <@common.jsErrorLog />
    <div class="">
        <@entityEdit.basicInformation />
    
    </div>

    <@entityEdit.contactInfo />
    <@entityEdit.hidden />

        <@edit.submit "Save" false />

    </@s.form>
<div id="error"></div>
<script type="text/javascript">
    var $frmPerson;
    $(function () {
        $frmPerson = $('#frmPerson');
        TDAR.autocomplete.applyInstitutionAutocomplete($('.institutionAutocomplete'), true);
        TDAR.common.initEditPage($('#frmPerson')[0]);
        //tack on the confirm-password rules
    });
</script>

</body>

</#escape>
