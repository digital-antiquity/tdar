<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common> 
<#macro selectYearType yearTypeOption>
	<#if yearType?? && (yearType == yearTypeOption)>
		selected="selected"
	</#if>
</#macro>

<#macro userAutoCompleteTable namePrefix='user' label="User" registeredUser=true nameFieldsOnly=false>
<#assign _autocompleteType = 'userAutoComplete' />
<#if !registeredUser>
<#assign _autocompleteType='nameAutoComplete'>
</#if>

<h4>${label}</h4>
<table id="${namePrefix}SearchTable" class="tableFormat width99percent" >
<tbody>
<tr id='${namePrefix}_0_'>
<td>
    <div class="width30percent marginLeft10" >
        <@s.hidden name='${namePrefix}Ids[0]' id="${namePrefix}id_0_" cssClass="" onchange="this.valid()"  />
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Last Name"  autocompleteParentElement="#${namePrefix}_0_" autocompleteName="lastName"
        autocompleteIdElement="#${namePrefix}id_0_"
            name="${namePrefix}.lastName" maxlength="255"  /> 
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="First Name" autocompleteParentElement="#${namePrefix}_0_" autocompleteName="firstName"
         autocompleteIdElement="#${namePrefix}id_0_"   name="${namePrefix}.firstName" maxlength="255"  />
        <#if !nameFieldsOnly>
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Email" autocompleteParentElement="#${namePrefix}_0_" autocompleteName="email"
           autocompleteIdElement="#${namePrefix}id_0_" name="${namePrefix}.email" maxlength="255"/>
        <br />
        </#if>
    </div>
    <#if !nameFieldsOnly>
    <div class="width99percent marginLeft10">
        <@s.textfield cssClass="watermarked ${_autocompleteType}" watermark="Institution Name" autocompleteParentElement="#${namePrefix}_0_" autocompleteName="institution"
           autocompleteIdElement="#${namePrefix}id_0_" name="${namePrefix}.institution.name" maxlength="255"  />
    </div>
    </#if>
</td>
<td>
<@edit.clearDeleteButton id="readUserRow" />
</td>
</tr>
</tbody>
</table>
<script>
<#if authenticated>
$(document).ready(function() {
    delegateCreator("#${namePrefix}SearchTable",${registeredUser?string},false);
});
</#if>
$(window).bind("pageshow", function() {
  $("#searchButton").removeAttr('disabled');
});

</script>
</#macro>

<head>
<title>Advanced Search</title>
<script type="text/javascript" src="<@s.url value='/includes/advanced-search.js'/>"></script>
<script type="text/javascript">
    $(document).ready(function() {
        //necessary for users that get to this page via the back button
        $("#searchButton").removeAttr('disabled');
    });

</script>
<style type="text/css">
    h3 {font-weight:bold; color:#333; margin-top:2em}
</style>

<@edit.resourceJavascript "#searchForm" />

</head>


<body>

<@edit.showControllerErrors />

<div class="glide">
       


    <@s.form id='searchForm' method='GET' action='results'>
    <p>Find resources in tDAR by providing information in the fields below and clicking the &quot;Search&quot; button. </p>
    <@search.queryField freeTextLabel="Search all fields" showAdvancedLink=false>
    <br/>
      <label for="title_s">Title:</label> <input id="title_s" type="text" name="title" value="${title!}" class="longfield" />
      <br/>
      <label for="tdarid">TDAR id:</label><input id="tdarid" type="text" class="number" name="id" value="<@common.safenum id/>" />
      <br/>
      <label for="projectList">Project:</label>
      <select name="projectIds[0]" id="projectList">
          <option value="" selected='selected'>All Projects</option>
          <@s.iterator value='projects' status='projectRowStatus' var='project'>
            <option value="${project.id?c}"><@common.truncate project.title 70 /></option>
          </@s.iterator>
      </select>
      <br/>
    </@search.queryField>
    <br/>
    
    
        <div id="divTemporalLimits" tiplabel="Temporal Limits" tooltipcontent="#divTemporalLimitTooltip">
        	<h3>Temporal Limits</h3>
        	
            <@edit.coverageDatesSection false />
            
    	</div>
    
        <div id="divTemporalLimitTooltip" class="hidden">
            <div>
                Limit search results to a date range.
                <dl>
                    <dt>Calendar Date</dt>
                    <dd>Enter a beginning and ending Julian calendar year.  Use negative numbers for BC dates</dd>
                    <dt>Radiocarbon Date </dt>
                    <dd>Enter a maximum/minimum radiocarbon age.</dd>
                </dl>
            </div>
        </div>
        <br/>   
    
    	<h3>Spatial Limits</h3>
        <@s.textfield name='geographicKeywords[0]' cssClass="longfield" label="Geographic term" labelposition="left" />
    	<div><#-- placeholder div to make sitemesh parser not pickup div id to replace -->
    	<div id='large-google-map' style="height:450px;"></div> 	
    	<input type="hidden" name="minx" id="minx" value="${(minx?c)!}">
    	<input type="hidden" name="maxx" id="maxx" value="${(maxx?c)!}">
    	<input type="hidden" name="miny" id="miny" value="${(miny?c)!}">
    	<input type="hidden" name="maxy" id="maxy" value="${(maxy?c)!}">
        </div>
    
    <h3>Investigation Types</h3>
    <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns=2 cssClass="smallIndent" 
        listTitle="definition" />
        
        
    <h3>Site Information</h3>
    <div id="divSiteInformation" 
            tiplabel="About Your Site(s)" 
            tooltipcontent="Keyword list: Enter site name(s) and select feature types discussed in the document. Use the Other field if needed.">
        <label>Site Name</label>
        <table id="siteNameKeywordTable" class="field" addAnother="add another site name">
        <tbody>
        <@s.iterator status='rowStatus' value='siteNameKeywords'>
        <tr id='siteNameKeywordRow_${rowStatus.index}_'>
        <td>
        <@s.textfield name='siteNameKeywords[${rowStatus.index}]' cssClass="longfield"/>
        </td><td>
        </td>
        </tr>
        </@s.iterator>
        </tbody>
        </table>
        
        <br/>
        <label>Site Type</label>
        
        
        <table id="siteTypeKeywordTable" class="field">
        <tbody>
            <tr><td><@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" /></td></tr>
        </tbody>
        </table>
        
        <label>Other</label>
        <@s.iterator status='rowStatus' value='uncontrolledSiteTypeKeywords'>
        <@s.textfield name='uncontrolledSiteTypeKeywords[${rowStatus.index}]' cssClass="longfield"/>
        </@s.iterator>
    </div>
     
        
        
    
    <h3>Material Type(s)</h3>
    <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"
        numColumns=3 cssClass="smallIndent" />
    
    <div 
        tiplabel="Cultural Terms"
        tooltipcontent="Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed.">
        <h3>Cultural Term(s)</h3>
        <div id="divCulturalInformation">
            <label>Culture</label>
            <table id="cultureKeywordTable" class="field">
                <tbody>
                <tr><td><@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" /></td></tr>
                </tbody>
            </table>
            
            <br />
            <label>Other</label>
            <@s.iterator status='rowStatus' value='uncontrolledCultureKeywords'>
            <@s.textfield name='uncontrolledCultureKeywords[${rowStatus.index}]' cssClass='longfield' />
            </@s.iterator>
        </div>
    </div>
    

    <h3>Temporal Information</h3>
    <@s.textfield name='temporalKeywords[0]' cssClass="longfield" label="Keyword" labelposition="left" />
    
    
    <h3>General Keywords</h3>
    <@s.textfield name='otherKeywords[0]' cssClass="longfield" label="Keyword" labelposition="left" />
    
    
    <br/>
    <div id="divPersonSearch">
        <h3>Search by Submitter / Author / Contributor</h3>
        <@userAutoCompleteTable label="Submitter" namePrefix="searchSubmitter"  />
        <br />
        <@userAutoCompleteTable label="Author or Contributor" namePrefix="searchContributor" registeredUser=false nameFieldsOnly=!authenticated />
    </div>
    
    <#if editor!false>
    <div id="divAdminSearchOptions">
        <h3 tiplabel="Curator Specific"   
            tooltipcontent="The search options in this section are only available to digital curators and administrators"
            >Curator-specific Search Options</h3>
        
        <h4>Status</h4>
        <@s.checkboxlist name='includedStatuses' list='allStatuses'  listValue='label' numColumns=4 />
        
        <div id="divCuratorDateFilters" tiplabel="Registration/Updated Dates" tooltipcontent="#divCuratorDateFiltersTip">
        <h4>Registration Date</h4>
        <@s.textfield cssClass="watermarked datepicker" watermark="m/d/yy" labelposition="left" name="dateRegisteredStart" label="After"/>
        <@s.textfield cssClass="watermarked datepicker" watermark="m/d/yy" labelposition="left" name="dateRegisteredEnd" label ="Before"/>

        <h4>Updated Date</h4>    
        <@s.textfield cssClass="watermarked datepicker" watermark="m/d/yy" labelposition="left" name="dateUpdatedStart" label="After"/>
        <@s.textfield cssClass="watermarked datepicker" watermark="m/d/yy" labelposition="left" name="dateUpdatedEnd" label="Before"/>
        </div>
        <div class="hidden" id="divCuratorDateFiltersTip">
            <dl>
                <dt>Date Registered</dt>
                <dd>This is the date that the user created DAR Resource.  Not to be confused with <em>Date Created</em>, 
                which is the creation date of the object that the tDAR resource describes</dd>
                
                <dt>Date Updated</dt>
                <dd>The most recent date that a user updated the metadata of the tDAR resource</dd>
            </dl>
            
        </div>
        
        
    </div>
    </#if>
    
    <h3>Sorting Options and Submit</h3>
    <label for="sortField">Sort By:</label>
     <@search.sortFields />
    <br/>
    <div id="error">
    </div>
    <div>
        <@s.submit id="searchButton" value="Search" /> <input type='button' value='Reset' id='formResetButton' />
    </div>
    </@s.form>
</div>

<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the advanced search page.  Hover over a specific field for more information.
    </div>
</div>

