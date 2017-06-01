<h1>User Agreements</h1>

<@s.form namespace="/" action="agreement-response" theme="simple">
    <@s.token name='struts.csrf.token' />
    <p>Some of ${siteAcronym}'s Terms have changed. Please review the changed versions of the terms and accept.  If you have any questions or concerns, please <a href="mailto:${contactEmail}">contact us</a>.
    
    </p>
    <#if tosAcceptanceRequired>
    <label class="checkbox">
        <input type="checkbox" name="acceptedAuthNotices" id="cbta" value="TOS_AGREEMENT">
        I have read and accept the ${siteAcronym} <@s.a href="${config.tosUrl}" target="_blank" >Terms Of Service</@s.a>
    </label>
    </#if>

    <#if contributorAgreementAcceptanceRequired>
    <label class="checkbox">
        <input type="checkbox" name="acceptedAuthNotices" id="cbca" value="CONTRIBUTOR_AGREEMENT">
        I have read and accept the ${siteAcronym} <@s.a href="${config.contributorAgreementUrl}" target="_blank" >Contributor Agreement</@s.a>
    </label>
    </#if>

<div class="form-actions">
    <button name="submitAccept" type="submit" value="accept" id="accept" class="btn btn-primary">Accept Agreement And Go To Dashboard</button>
    <button name="submitDecline" type="submit" value="decline" id="decline" class="btn">I Will Decide Later - Logout</button>
</div>

</@s.form>
