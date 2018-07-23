<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="codingSheet"/>
    <#global inheritanceEnabled=true />
    <#global hideAuthors=true/>
    <#global hideRelatedCollections=true/>
    <#global hideKeywordsAndIdentifiersSection=true/>

    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/helptext.ftl" as  helptext>

    <#macro subNavMenu>
    <li class="hidden-tablet hidden-phone"><a href="#enter-data">Coding Rules</a></li>
    </#macro>


    <#macro beforeUpload>

    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym}
        data integration tool.
    </span>

    <div id='divOntology' class="glide ontologyInfo" data-tooltipcontent="#ontologyToolTip" data-tiplabel="Ontology">

        <@edit.categoryVariable />


        <!-- FIXME: This screams to be refactored into a common format; one issue in a cursory try though is the ability to do the assignments of the defaultId and Text-->
        <h4>Map To an Ontology</h4>
        <#assign ontologyId="" />
        <#assign ontologyTxt="" />
        <#if ontology??  && ontology.id??>
            <#assign ontologyId=ontology.id?c />
            <#assign ontologyTxt="${ontology.title} (${ontology.id?c})"/>
        </#if>
        <@s.hidden name="ontology.id" value="${ontologyId}" id="oid" />
        <@common.combobox name="ontology.title"
        label="Ontology Name"
        target="#divOntology"
        value="${ontologyTxt}"
        placeholder="Enter the name of an Ontology"
        autocompleteParentElement="#divOntology"
        autocompleteIdElement="#oid"
        cssClass="input-xxlarge-combo ontologyfield" />

        <div class="control-group">
            <div class="controls">
                    <span class="help-block">
                        Alternately, you can create a new ontology by pressing the button below. TDAR will start the process in a new window, and return here when you are finished.
                    </span>


                <a class="btn btn-small" target="_blank"
                   onclick="TDAR.common.setAdhocTarget(this, '#divOntology');"
                   href='<@s.url value="/ontology/add?returnToResourceMappingId=${resource.id?c}"/>'>Create An Ontology</a>
            </div>
        </div>
    </div>
    </#macro>

    <#macro localSection>

    <div class="">
        <@common.codingRules />
    </div>
        <@edit.manualTextInput
        typeLabel="Coding Sheet"
        type="coding"
        uploadOptionText="Upload an Excel or CSV coding sheet file"
        manualEntryText="Manually enter coding rules into a textarea"; _divid>
            <#if _divid=="upload">
            <p class="help-block">
                To be parsed properly a coding sheet should have <b>Code, Term, Description (optional)</b> columns, in order. For example:
            </p>

            <table class="table">
                <thead>
                <tr>
                    <th>Code</th>
                    <th>Term</th>
                    <th>Description (optional)</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>18</td>
                    <td>Eutamias spp.</td>
                    <td>Tamias spp. is modern term</td>
                </tr>
                <tr>
                    <td>19</td>
                    <td>Ammospermophilus spp.</td>
                    <td></td>
                </tr>
                <tr>
                    <td>20</td>
                    <td>Spermophilus spp.</td>
                    <td></td>
                </tr>
                </tbody>
            </table>
            <#elseif _divid=="manualEntry">
            <p class="help-block">
                Please enter your coding rules in the text area below. Each line can have a maximum of three elements, separated by commas,
                and should be in the form <code>code, term, optional description</code>. Codes can be numbers or arbitrary strings.
                For example:
            </p>
            <blockquote>
                <p>
                    <code>1, Small Mammal, Small mammals are smaller than a breadbox</code><br/>
                    <code>2, Medium Mammal, Medium Mammals are coyote or dog-sized</code>
                </p>
            </blockquote>

            <p class="help-block">If a code, a term, or a description has an embedded comma,
                the whole value must be enclosed in double quotes, e.g.
            </p>
            <blockquote>
                <p><code>3, Large Mammal, &quot;Large mammals include deer, antelope, and bears&quot;</code></p>
            </blockquote>
            </#if>

        </@edit.manualTextInput>
    </#macro>

    <#macro localJavascript>
    var $form = $("#metadataForm");
    TDAR.common.setupSupportingResourceForm(${codingSheet.totalNumberOfFiles?c}, "coding sheet");
    TDAR.autocomplete.applyComboboxAutocomplete($('input.ontologyfield', $form), "ONTOLOGY");
    </#macro>

</body>
</#escape>
