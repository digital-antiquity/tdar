<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<h2>Basic Search</h2>
<hr/>
<@s.form action="results" method="GET" id='searchForm'>
<@search.queryField />
<@s.submit value="Search" />
</@s.form>
