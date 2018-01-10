    <h2>Confirm Account Deletion</h2>

    <@s.form name='deleteForm' id='deleteForm'  method='post' action='delete'>
        <@s.token name='struts.csrf.token' />
        <h4>Let us know why you're deleting your account</h4>
        <textarea name="deletionReason" cols='60' rows='3' class="input-xxlarge" maxlength='255'></textarea>

        <@s.submit type="submit" name="delete" value="delete" cssClass="btn button btn-warning"/>
    </@s.form>
