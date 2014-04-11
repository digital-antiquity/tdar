<h1>User Agreements</h1>

<@s.form namespace="/" action="agreement-response" theme="simple">
    <#if tosAcceptanceRequired>
    <label class="checkbox">
        <input type="checkbox" name="acceptedAuthNotices" id="cbta" value="TOS_AGREEMENT">
        I have read and accept the ${siteAcronym} <@s.a href="${tosUrl}" target="_blank" >Terms Of Service</@s.a>
    </label>
    </#if>

    <#if contributorAgreementAcceptanceRequired>
    <label class="checkbox">
        <input type="checkbox" name="acceptedAuthNotices" id="cbca" value="CONTRIBUTOR_AGREEMENT">
        I have read and accept the ${siteAcronym} <@s.a href="${contributorAgreementUrl}" target="_blank" >Contributor Agreement</@s.a>
    </label>
    </#if>

    <div class="form-actions">
        <button name="submit" type="submit" value="accept" id="accept" class="btn btn-primary">Accept Agreement And Go To Dashboard</button>
        <button name="submit" type="submit" value="decline" id="decline" class="btn">I Will Decide Later - Logout</button>
    </div>

</@s.form>
