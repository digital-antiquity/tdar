<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/content/entity/entity-edit-common-resource.ftl" as entityEdit>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<head>
    <#assign pageTitle = "Add a new User">

    <#if (editingSelf)>
        <#assign pageTitle = "Your Profile: ${person.properName!'n/a'}">

    <#elseif person.id != -1>
        <#assign pageTitle = "Editing: ${person.properName!'n/a'}" >
    </#if>
    <title>${pageTitle}</title>

</head>
<body>

    <h1>${pageTitle}</h1>
    <@entityEdit.userEditForm person/>
</body>

</#escape>
