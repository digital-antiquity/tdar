<#macro queryField freeTextLabel="Search" showAdvancedLink=true>

<label for="queryField">${freeTextLabel}:</label>    <@s.textfield id='queryField' name='query' size='81' value="${query!}" cssClass="longfield"/> 
<#if showAdvancedLink><a style="padding-left:10px" href="<@s.url value="/search/advanced"/>">advanced search</a></#if>

<br/>
<br/>

<#nested>

<div>
<label>Limit by<br/> resource type:</label> 
<@resourceTypeLimits />
</div>
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
<div class="field col3">
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
  <#assign sortFld = ""/>
  <#if sortField??>
    <#assign sortFld = sortField />
    <#if (sortOrder?? && sortOrder)>
      <#assign sortFld = "-"+sortFld />
    </#if>
  </#if>
    <select name="sortField" id="sortField">
        <option value="relevance" <#if sortFld=='relevance'>selected</#if>>Relevance</option>
        <option value="title_sort" <#if sortFld=='title_sort'>selected</#if>>Title (A-Z)</option>
        <option value="-title_sort" <#if sortFld=='-title_sort'>selected</#if>>Title (Z-A)</option>
        <option value="dateCreated" <#if sortFld=='dateCreated'>selected</#if>>Year (A-Z)</option>
        <option value="-dateCreated" <#if sortFld=='-dateCreated'>selected</#if>>Year (Z-A)</option>
    </select>
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
