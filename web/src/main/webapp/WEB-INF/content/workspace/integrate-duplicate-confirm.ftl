<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

    <h2>Duplicate ${workflow.title}</h2>
<div class="row">
<div class="col-6">
<p>Duplicating integrations in ${siteAcronym} allows a user to copy an existing integration instead of starting from scratch.  . </p>

</div>
<div class="col-6">
<img src="<@s.url value="/images/duplicate.png"/>" title="Duplicate image" alt="duplicate"/>
</div>
</div>
    <@s.form name='duplicateForm' id='deleteForm'  method='post' action='duplicate'>
        <@s.token name='struts.csrf.token' />
        <@s.submit type="submit" name="duplicate" value="duplicate" cssClass="btn button btn-warning"/>
        <@s.hidden name="id" />
    </@s.form>
