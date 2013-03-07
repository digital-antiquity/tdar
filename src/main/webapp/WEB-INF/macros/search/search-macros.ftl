<#macro queryField freeTextLabel="Search" showAdvancedLink=true showLimits=false submitLabel="Search">

 <@s.textfield placeholder="${freeTextLabel}" id='queryField' name='query' size='81' value="${query!}" cssClass="input-xxlarge"/>
    <#if showAdvancedLink><span class="help-inline"><a style="display:inline" href="<@s.url value="/search/advanced"/>">advanced search</a></span></#if>
    <@s.submit value="${submitLabel}" cssClass="btn btn-primary" />
    <#nested>
    <#if showLimits>
        <br/>
        <@narrowAndSort />
    </#if>
    <br/>
</#macro>

<#macro narrowAndSort>
        <h2>Narrow Your Search</h2>

        <@s.checkboxlist id="includedResourceTypes" numColumns=4 spanClass="span2" name='resourceTypes' list='allResourceTypes'  listValue='label' label="Resource Type"/>

        <#if editor!false>
        <#--FIXME: there seems to be a bug in numColumns when the value is 'too high' (not sure what that number is yet) -->
        <#--FIXME: also,  we need a good,efficient way to emit bootstrap's version of an inline checkboxlist -->
        <@s.checkboxlist id="myincludedstatuses" name='includedStatuses' list='allStatuses'  listValue='label' label="Status" />
        </#if>
        
        <h4>Limit by geographic region:</h4>
        <div id="latlongoptions">
            <div id='large-google-map'></div>     
            <@s.hidden name="groups[0].latitudeLongitudeBoxes[0].maximumLongitude" id="maxx" cssClass="ne-lng" />
            <@s.hidden name="groups[0].latitudeLongitudeBoxes[0].minimumLatitude"  id="miny" cssClass="sw-lat" />
            <@s.hidden name="groups[0].latitudeLongitudeBoxes[0].minimumLongitude" id="minx" cssClass="sw-lng" />
            <@s.hidden name="groups[0].latitudeLongitudeBoxes[0].maximumLatitude"  id="maxy" cssClass="ne-lat" />
        </div>
        
    <h2>Sorting Options and Submit</h2>
    <@sortFields />
</#macro>

<#macro typeSelected type>
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

<#macro sortFields javascriptOn=false label="Sort By">

<@s.select value="sortField" name='sortField'  
        emptyOption='false' listValue='label' label=label list='%{sortOptions}'/>
    <#--FIXME: move this block to tdar.common.js, bind if select has 'autoreload' class -->
    <#if javascriptOn>
      <script type='text/javascript'>
      $("#sortField").change(function() {
        var url = window.location.search.replace(/([?&]+)sortField=([^&]+)/g,"");
        //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'? 
        var prefix = "";
        if (url.indexOf("?") != 0) {
          prefix = "?";
        }
        url = prefix + url +  "&sortField="+$('#sortField').val();
        window.location = url;
        });
      </script>
    </#if>
</#macro>


<#macro headerLinks includeRss=false>
  <meta name="totalResults" content="${totalRecords}" />
  <meta name="startIndex" content="${startRecord}" />
  <meta name="itemsPerPage" content="${recordsPerPage}" />
  <#if includeRss>
  <link rel="alternate" type="application/atom+xml" title="Atom 1.0" href="${rssUrl}" />
  </#if>
    <#-- if id parameter was added to querystring via urlrewrite inbound rule, we need to remove it when rendering relative url's  -->
    <#local path = "">
    <#if (actionName!"")=="results">
    <#local path=actionName>
    </#if>
    
  <#if (nextPageStartRecord < totalRecords) >
      <link rel="next" href="<@searchUrl path><@s.param name="startRecord" value="${nextPageStartRecord?c}"/></@searchUrl>"/>
  </#if>
  <#if  paginationHelper.hasPrevious() >
      <link rel="previous" href="<@searchUrl path ><@s.param name="startRecord" value="${prevPageStartRecord?c}" /></@searchUrl>"/>
  </#if>
</#macro>


<#macro searchLink path linkText>
    <a href="<@searchUrl path><#nested></@searchUrl>">${linkText}</a>
</#macro>

<#macro searchUrl path><@s.url includeParams="all" value="${path}"><#if path?? && path!="results"><@s.param name="id" value=""/></#if><#nested></@s.url></#macro>

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

<#macro pagination path="results" helper=paginationHelper>
<div id="divPaginationSection" class="">
<#if (helper.totalNumberOfItems >0)>
    <table class="pagin">
        <tr>
        <#if helper.hasPrevious()>
        <td class="prev">
            <@paginationLink startRecord=(helper.itemsPerPage * (helper.currentPage - 1)) path=path linkText="Previous" />
        </td>
        </#if>
        <td class="page">
            <ul>
              <#if (0 < helper.minimumPageNumber) >
                <li>
                  <@paginationLink startRecord=0 path=path linkText="First" />
                 </li>
                <li>...</li>
              </#if>
                <#list helper.minimumPageNumber..helper.maximumPageNumber as i>
                <li>
                    <#if i == helper.currentPage>
                        <span class="currentResultPage">${i + 1}</span>
                    <#else>
                        <@paginationLink startRecord=(i * helper.itemsPerPage) path=path linkText=(i + 1) />
                    </#if>
                </li>
                </#list>
            <#if (helper.maximumPageNumber < (helper.pageCount - 1))>
                <li>...</li>
                <li>
                      <@paginationLink startRecord=(helper.itemsPerPage * (helper.pageCount-1)) path=path linkText="Last" />
                </li>
            </#if>
            </ul>
        </td>
            <#if (helper.hasNext()) >
        <td class="next">
                <@paginationLink startRecord=(helper.itemsPerPage * (helper.currentPage + 1)) path=path linkText="Next" />
        </td>
            </#if>
        </tr>
    </table>
</div>
</#if>
</#macro>

<#macro bcad _year>
  <#if (_year < 0)>BC<#else>AD</#if><#t/>
</#macro>


<#macro basicPagination label="Records" showIfOnePage=false helper=paginationHelper>

<#if (helper.pageCount > 1)>
  <div class="glide">
    <div id="recordTotal">${label} ${helper.firstItem + 1} - ${helper.lastItem + 1} of ${helper.totalNumberOfItems}
    </div> 
    <@pagination ""/> 
  </div>
<#elseif (helper.totalNumberOfItems > 0 && showIfOnePage)>
  <div class="glide">
  Displaying ${label} ${helper.firstItem + 1} - ${helper.lastItem + 1} of ${helper.totalNumberOfItems}
  </div>
</#if>
</#macro>

