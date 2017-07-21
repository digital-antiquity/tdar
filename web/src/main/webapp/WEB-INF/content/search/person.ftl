<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>

<#--FIXME: this method for determining active tab won't work if (for example) controller returns INPUT for collection/institution/person search -->

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
        <div id="person" class="tab-pane active">
            <div class="glide">
                <h3>Search For Person</h3>

                <@s.form action="people" method="GET" id='searchForm4'>
                    <@search.queryField freeTextLabel="Person Name" showLimits=false showPersonField=true showAdvancedLink=false />
                </@s.form>
            </div>
            <div id="collection-spacer" style="height:850px"></div>
        </div>

    </div>
   

</div>

<script>
    $(document).ready(function () {
        //switch to the correct tab if coming from collection search

        TDAR.advancedSearch.serializeFormState();

        if ($("#autosave").val() !== '') {
            $("#searchGroups").html($("#autosave").val());
            $('.add-another-control').remove();
        }
    });
</script>

<form name="autosave" style="display:none;visibility:hidden">
    <textarea id="autosave"></textarea>
</form>

</#escape>
