<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<div class="glide span9">
    <h3><@s.text name="${keywordType.localeKey}"/><span class="red">:${keyword.label}</span></h3>

    <@s.form  name='keywordForm' id='keywordForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>
    <@s.hidden name="id" />
    <@s.hidden name="keywordType" />
    <@s.textfield name="label" value="${keyword.label}" label="Label" cssClass="input-xxlarge" labelPosition="left" required=true />
    <@s.textarea name="description" value="${keyword.definition!''}" label="Definition" labelposition="top" cssClass="input-xxlarge"  cols="80"  rows="4" />
    
    <@edit.submit fileReminder=false />
    </@s.form>

</div>
</#escape>