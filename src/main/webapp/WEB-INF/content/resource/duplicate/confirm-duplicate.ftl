<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

    <h2>Duplicate ${resource.title}</h2>
<div class="row">
<div class="span6">
<p>Duplicating resources in ${siteAcronym} is a great way to simplify data entry by allowing a user to copy an existing resource instead of starting from scratch.  
This is especially valuable when with data sets and GIS resources where there are mapped data tables.  Duplicating a resource will copy almost everything to the
new resource. </p>
<p><b>Parts of the record that are not copied</b></p>
<ul>
    <li>Associated files</li>
    <li>Resource Ownership</li>
    <li>Permissions (if you don't have the right to edit the record)</li>
    <li>Detailed Lat-Long data (if you don't have the right to edit the record)</li>
    <li>Project Association (if you don't have the right to edit the record)</li>
</ul>  
</div>
<div class="span6">
<img src="<@s.url value="/images/duplicate.png"/>" title="Duplicate image" alt="duplicate"/>
</div>
</div>
    <@s.form name='deleteForm' id='deleteForm'  method='post' action='duplicate-final'>
        <@s.token name='struts.csrf.token' />
        <@s.submit type="submit" name="duplicate" value="duplicate" cssClass="btn button btn-warning"/>
        <@s.hidden name="id" />
    </@s.form>
