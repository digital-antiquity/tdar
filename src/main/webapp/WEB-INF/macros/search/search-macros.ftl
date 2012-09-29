<#macro queryField freeTextLabel="Search" showAdvancedLink=true>

<label for="queryField">${freeTextLabel}:</label>    <@s.textfield id='queryField' name='query' size='81' value="${query!}" cssClass="longfield"/> 
<#if showAdvancedLink><a style="padding-left:10px" href="<@s.url value="/search/advanced"/>">advanced search</a></#if>

<#nested>
<#if !showAdvancedLink>
<br/>
    <div>
    <label>Limit by<br/> resource type:</label> 
    <@resourceTypeLimits />
    </div>
</#if>
<br/>
</#macro>

<#macro typeSelected type>
    <#if searchProjects?? && searchProjects><#return></#if>
    <#if !resourceTypes??>
       <#if type == 'DOCUMENT' || type == 'DATASET'>checked="checked"</#if>
    <#else>
       <#if resourceTypes.contains(type)>checked="yes"</#if>
    </#if>
</#macro>

<#macro resourceTypeLimits>
<div class="field col3 resourceTypeLimits">
    <input type="checkbox" name="resourceTypes"  <@typeSelected "PROJECT" /> value="PROJECT"  id="resourceTypes_Project" />
    <label for="resourceTypes_Project">Projects</label>
    <input type="checkbox" name="resourceTypes" <@typeSelected "DOCUMENT" /> value="DOCUMENT" id="resourceTypes_Document" />
    <label for="resourceTypes_Document">Documents</label>
    <input type="checkbox" name="resourceTypes" <@typeSelected "DATASET" /> value="DATASET" id="resourceTypes_Dataset" />
    <label for="resourceTypes_Dataset">Datasets</label>
  <br/>
    <input type="checkbox" name="resourceTypes" <@typeSelected "IMAGE" /> value="IMAGE" id="resourceTypes_Image" />
    <label for="resourceTypes_Image">Images</label>
    <input type="checkbox" name="resourceTypes" <@typeSelected "SENSORY_DATA" /> value="SENSORY_DATA" id="resourceTypes_Sensory_Data" />
    <label for="resourceTypes_Sensory_Data">Sensory Data</label>
    <input type="checkbox" name="resourceTypes" <@typeSelected "CODING_SHEET" /> value="CODING_SHEET" id="resourceTypes_Coding_Sheet" />
    <label for="resourceTypes_Coding_Sheet">Coding Sheets</label>
  <br/>
    <input type="checkbox" name="resourceTypes" <@typeSelected "ONTOLOGY" /> value="ONTOLOGY" id="resourceTypes_Ontology" />
    <label for="resourceTypes_Ontology">Ontologies</label>
    <br />
</div>
</#macro>

<#macro sortFields javascriptOn=false>

<@s.select value="sortField" name='sortField'  
        emptyOption='false' listValue='label' list='%{sortOptions}'/>

    <#if javascriptOn>
      <script type='text/javascript'>
      $("#sortField").change(function() {
        var url = window.location.href.replace(/(&+)sortField=([^&]+)/,"");
        url += "&sortField="+$('#sortField').val();
        window.location = url;
        });
      </script>
    </#if>
</#macro>




<#macro initResultPagination>
<#global firstRec = (startRecord + 1) />
<#global curPage = ((startRecord/recordsPerPage)?floor + 1) />
<#global numPages = ((totalRecords/recordsPerPage)?ceiling) />
<#global lastRec = nextPageStartRecord>

<#if (firstRec > totalRecords)>
 <#assign numPages = 0 />
 <#assign firstRec = totalRecords/>
</#if>

<#if (nextPageStartRecord > totalRecords) >
	<#assign lastRec = totalRecords>
</#if>

<#if (firstRec - recordsPerPage) < 1 >
	<#assign prevPageStartRec = 0>
<#else>
	<#assign prevPageStartRec = firstRec - recordsPerPage - 1>
</#if>
</#macro>
<#macro searchLink path linkText>
	<a href="
	<@s.url includeParams="all" value="${path}">
	<#if path?? && path!="results">
	<@s.param name="id" value=""/>
	</#if>
		<#nested>
	</@s.url> 
	">${linkText}</a>
</#macro>

<#macro paginationLink startRecord path linkText>
	<span class="paginationLink">
	<@searchLink path linkText>
		<@s.param name="startRecord" value="${startRecord?c}" />
		<@s.param name="recordsPerPage" value="${recordsPerPage?c}" />
	</@searchLink>
	</span>
</#macro>

<#macro join sequence delimiter=",">
    <#list sequence as item>
        ${item}<#if item_has_next>${delimiter}</#if><#t>
    </#list>
</#macro>

<#macro pagination path="results">
	<div class="pagination">
  <#assign start =0>
  <#assign end =numPages -1>
  <#if numPages &gt; 40 && curPage &gt; 19 >
    <#assign start = curPage - 20>
  </#if>
  <#if numPages &gt; 40 && curPage &lt; numPages -19 >
    <#assign end = curPage + 19>
  </#if> 

  <#if start != 0>
      <@paginationLink startRecord=(0 * recordsPerPage) path="${path}" linkText="first" />
  </#if>
		<#if (firstRec > 1)>
			<@paginationLink startRecord=prevPageStartRec path="${path}" linkText="previous" />
		</#if>
		<#if (numPages > 1)>
			<#list start..end as i>
				<#if (i + 1) = curPage>
                                        <#-- FIXME: there are 2 of these spans with
                                        the same id being generated.  Turn this into
                                        a CSS class instead or is this a bug?
                                        -->
					<span id="currentResultPage">${i + 1}</span>
				<#else>
					<@paginationLink startRecord=(i * recordsPerPage) path="${path}" linkText=(i + 1) />
				</#if>
			</#list>
			<#else>
			1<br/>
		</#if>
		<#if (nextPageStartRecord < totalRecords) >
			<@paginationLink startRecord=nextPageStartRecord path="${path}" linkText="next" />
		</#if>
  <#if (end != numPages && nextPageStartRecord < totalRecords)>
          <@paginationLink startRecord=(totalRecords - totalRecords % 20) path="${path}" linkText="last" />
  </#if>
	</div>
</#macro>

<#macro bcad _year>
  <#if (_year < 0)>BC<#else>AD</#if><#t/>
</#macro>
