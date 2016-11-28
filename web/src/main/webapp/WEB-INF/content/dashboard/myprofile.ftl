<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/content/entity/entity-edit-common.ftl" as entityEdit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/content/dashboard/dashboard-common.ftl" as dash>
<head>
    <#assign pageTitle = "Your Profile: ${person.properName!'n/a'}">

    <title>${pageTitle}</title>

</head>
<body>

    <h1>${pageTitle}</h1>
    <div class="row">
        <div class="span2">
            <@dash.sidebar current="myprofile"/>
        </div>
        <div class="span10">
            <@entityEdit.userEditForm person/>
        </div>
    </div>
</body>

</#escape>
