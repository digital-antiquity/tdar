<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "common-account.ftl" as accountcommon>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>

<head>
    <title>${account.name!"Your account"}</title>
</head>
<body>

<h1>Billing Account</h1>

<div>
    <div class="well">
        Note: you may have multiple accounts to simplify billing and allow different people to charge to different accounts within an organization.
    </div>
    <@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal tdarvalidate" enctype='multipart/form-data' action='save'
                dynamicAttributes={"data-validate-method":"initBasicForm"} >
        <@s.token name='struts.csrf.token' />
        <@edit.hiddenStartTime />
        <@common.jsErrorLog />
        <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
        <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"  cols="80"  />


        <#if editor>
        <div class="well">
            <h4>Account File Settings</h4>
            <@s.textfield name="account.daysFilesExpireAfter" cssClass="input-xlarge" label="Expire Files After # days"/>

            <div class="control-group">
                <label class="control-label">Full Service Account?</label>

                <div class="controls">
                    <label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="account.fullService"
                                                                            value="true" <@commonr.checkedif account.fullService true /> />Yes</label>
                    <label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="account.fullService"
                                                                             value="false" <@commonr.checkedif account.fullService false /> />No</label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Enable External Review (if full service)</label>

                <div class="controls">
                    <label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="account.externalReview"
                                                                            value="true" <@commonr.checkedif account.externalReview true /> />Yes</label>
                    <label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="account.externalReview"
                                                                             value="false" <@commonr.checkedif account.externalReview false /> />No</label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Enable Initial Review phase (if full service)</label>

                <div class="controls">
                    <label for="rdoVisibleTrue" class="radio inline"><input type="radio" id="rdoVisibleTrue" name="account.initialReview"
                                                                            value="true" <@commonr.checkedif account.studentReview true /> />Yes</label>
                    <label for="rdoVisibleFalse" class="radio inline"><input type="radio" id="rdoVisibleFalse" name="account.studentReview"
                                                                             value="false" <@commonr.checkedif account.studentReview false /> />No</label>
                </div>
            </div>
        </div>

        </#if>
        <#if billingManager>
            <div class="control-group" id="divSubmitter">
                <label class="control-label">Owner</label>

                <div class="controls controls-row">
                    <#if owner?has_content>
                <@edit.registeredUserRow person=owner isDisabled=disabled   _personPrefix="" _indexNumber=''
                    prefix="owner" includeRights=false includeRepeatRow=false />
	 	        <#else>
                        <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber=''
                        prefix="owner" includeRights=false includeRepeatRow=false />
                    </#if>
                </div>
            </div>
        </#if>

<#--
        <#if billingManager>
            <label class="">Status</label>
            <@s.select theme="tdar" id="statuses" headerKey="" headerValue="Any" name='_tdar.status'  emptyOption='false' listValue='label'
            list='%{statuses}' cssClass="input-block-level"/>
        </#if>
-->
        <@s.hidden name="id" value="${account.id?c!-1}" />
        <@accountcommon.accountInfoForm />
    </@s.form>

</div>
<script>
    $(document).ready(function () {
        'use strict';
        TDAR.common.initEditPage($('#MetadataForm')[0]);
        TDAR.autocomplete.delegateCreator('#accessRightsRecords', true, false);
    });
</script>

    <@edit.personAutocompleteTemplate />

</body>
</#escape>
