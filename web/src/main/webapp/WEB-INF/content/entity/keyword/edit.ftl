<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>

    <h3><@s.text name="${keywordType.localeKey}"/><span class="red">:${keyword.label}</span></h3>
    <br>
    <br>
<div class="glide row">
    <@s.form  name='keywordForm' id='keywordForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>
    <@s.hidden name="id" />
    <@s.hidden name="keywordType" />
    <@s.textfield name="label" value="${keyword.label}" label="Label" cssClass="input-xxlarge" labelPosition="left" required=true />
    <@s.textarea name="description" value="${keyword.definition!''}" label="Definition" labelposition="top" cssClass="input-xxlarge"  cols="80"  rows="4" />
    <div id="mappingsTable" class="control-group repeatLastRow" addAnother="add another mapping">
    <div class="span12">
    <#assign maps = keyword.externalMappings>
    <#if maps?size == 0>
        <#assign maps = [blankMapping]>
    </#if>

    <#list maps>
    <h4>External Relations</h4>
        <#items as map>
            <div id="mapping_row_${map_index}_" class="repeat-row controls control-row">
               <div class="span6">
                   <@s.hidden name="mappings[${map_index}].id" /> 
                   <@s.textfield name="mappings[${map_index}].relation"       label="Relation (url)" cssClass="url input-xxlarge"/>            
               </div>
               <div class="span4">
                  <@s.select theme="tdar" name="mappings[${map_index}].relationType" list="relationTypes" listValue="term"   label='Type'  emptyOption="true" />
               </div>
               <div class="span1"><br/>
                   <@nav.clearDeleteButton id="mapping_row" />
               </div>
            </div>
        </#items>
        </#list>
</div>
    </div>    

    
    <@edit.submit fileReminder=false />
    </@s.form>
</div>
</div>
<script>
$(document).ready(function(){
TDAR.repeatrow.registerRepeatable(".repeatLastRow");
});
</script>
</#escape>