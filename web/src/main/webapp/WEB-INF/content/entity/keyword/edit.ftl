<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>

<h3><@s.text name="${keywordType.localeKey}"/><span class="red">:${keyword.label}</span></h3>

<div class="row">
    <div class="col-12">
    <br/>
        <@s.form  name='keywordForm' id='keywordForm'   cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"} method='post' enctype='multipart/form-data' action='save'>
            <@s.hidden name="id" />
            <@s.hidden name="keywordType" />
            <@s.textfield name="label" value="${keyword.label}" label="Label" cssClass="input-xxlarge"
                labelPosition="left" required=true />
            <@s.textarea name="description" value="${keyword.definition!''}" label="Definition" labelposition="top"
                cssClass="input-xxlarge"  cols="80"  rows="4" />

            <div id="mappingsTable" class="repeatLastRow" addAnother="add another mapping">
                    <#assign maps = keyword.assertions>
                    <#if maps?size == 0>
                        <#assign maps = [blankMapping]>
                    </#if>

                    <#list maps>
                        <h4>External Relations</h4>
                        <#items as map>
                            <div id="mapping_row_${map_index}_" class="repeat-row row">
                                    <label class="col-form-label col-2">Relation / Type</label>
                                    <div class="col-10">
                                        <div class="row">
                                        <div class="col-6">
                                           <@s.hidden name="mappings[${map_index}].id" />
                                           <@s.textfield name="mappings[${map_index}].relation"
                                                placeholder="Relation URL e.g. www.example.com?id=12345"
                                                cssClass="url input-xxlarge"/>
                                        </div>
                                        <div class="col-3">
                                          <@s.textfield  name="mappings[${map_index}].label" maxlength=255 
                                              />
                                        </div>
                                        <div class="col-2">
                                          <@s.select  name="mappings[${map_index}].relationType"
                                            list="relationTypes" listValue="term" cssClass="input-medium" emptyOption="true"
                                              />
                                        </div>
                                           <@nav.clearDeleteButton id="mapping_row" />
                                    </div>
                                </div>
                            </div>
                        </#items>
                    </#list>
            </div>
            <@edit.submit fileReminder=false />
        </@s.form>
    </div>
</div>
</div>
<script>
$(function(){
    TDAR.repeatrow.registerRepeatable(".repeatLastRow");
});

</script>
</#escape>