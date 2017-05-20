<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>

<head>
    <title>Search ${siteAcronym}</title>
    <style type="text/css">
    </style>

</head>
<body>
<#escape _untrusted as _untrusted?html >
<h1>Search ${siteAcronym}</h1>

<div class="usual">
	<@search.toolbar />

    <div class="tab-content">
        <div id="resource" class="tab-pane active">
            <@s.form action="multi" method="GET" id="searchGroups" cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} >
                <input type="hidden" name="_tdar.searchType" value="advanced">

                <@s.textfield name="query" />

                <div>
                    <div id="error"></div>
                    <@s.submit id="searchButton" value="Search" cssClass="btn btn-primary" />
                </div>


            </@s.form>
        </div>
    </div>
</div>

</body>

</#escape>
