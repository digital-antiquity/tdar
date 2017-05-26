<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>Invite a user to edit this collection</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Invite to Edit: ${persistable.title}</span></h1>
</div>
<div class="row">
    <div class="span12">
        <@edit.shareSection formAction="/resource/share/save?id=${id?c}" />
    </div>
</div>


<div id="customIncludes" parse="true">
    <script src="/js/tdar.manage.js"></script>
</div>


</#escape>
