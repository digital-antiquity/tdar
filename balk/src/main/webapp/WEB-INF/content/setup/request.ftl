hi, go here <a href="${authorizedUrl}" target="_blank">${authorizedUrl}</a>

<@s.form action="response">
    <@s.textfield name="code" label="Dropbox code"/>
    <@s.submit name="submit"/>
</@s.form>