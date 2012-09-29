<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<h2>Basic Search</h2>
<hr/>
<@s.form action="results" method="GET" id='searchForm'>
<@search.queryField />
<@s.submit value="Search" />
</@s.form>
<script type="text/javascript">
    //SEARCH: if shift key down when checking a resource type, uncheck  siblings
    //FIXME: add this to common.js unless Adam really hates this feature.
    $('input[type=checkbox]', '.resourceTypeLimits').click(function(evt) {
        if(evt.shiftKey) {
            $('input[type=checkbox]', '.resourceTypeLimits').not($(this)).prop("checked", false);
        }
    }); 
</script>
