<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="image"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />

    <#macro basicInformation>
    <div id="t-located" data-tooltipcontent="Actual physical location of a copy of the image, e.g. an agency, repository,
        or library." data-tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='image.copyLocation' cssClass="longfield"  maxlength=255/>
    </div>


    </#macro>

</#escape>