<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<h2>Basic Search</h2>
<hr/>
<div class="usual glide">
    <ul class="idTabs" id="idtab">
        <li><a href="#resource">Resource</a></li>
        <li><a href="#collection">Collection</a></li>
    </ul>
    <div id="resource" style="display:none;">
    <@s.form action="results" method="GET" id='searchForm'>
        <@search.queryField showLimits=true />
        <@s.submit value="Search" />
    </@s.form>
    </div>
    <div id="collection" style="display:none;">
    <@s.form action="collections" method="GET" id='searchForm2'>
        <@search.queryField showLimits=false showAdvancedLink=false />
    </@s.form>
    </div>
</div>
<script>
    $(document).ready(function () {
        $("#idtab").idTabs();
        //other view init stuff;
        loadTdarMap();
    });
</script>
