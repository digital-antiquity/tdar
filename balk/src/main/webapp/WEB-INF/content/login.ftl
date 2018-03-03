
<form id="loginForm" name="loginForm" action="login/process" method="post" class="form-horizontal" novalidate="novalidate">
    <fieldset>

   <textarea name="h.comment" class="tdarCommentDescription" style="display:none"></textarea>

<div class="control-group success"><label class="control-label" for="loginUsername">Username</label>    <div class="controls">

<input type="text" name="userLogin.loginUsername" value="" id="loginUsername" class="required valid" spellcheck="false" autofocus="autofocus" aria-required="true" aria-invalid="false"><label id="loginUsername-error" class="help-inline" for="loginUsername"></label></div>
</div>

<div class="control-group success"><label class="control-label" for="loginPassword">Password</label>    <div class="controls">
<input type="password" name="userLogin.loginPassword" id="loginPassword" class="required valid" aria-required="true" aria-invalid="false"><label id="loginPassword-error" class="help-inline" for="loginPassword"></label></div>
</div>

<input type="hidden" name="url" value="" id="loginForm_url">    <div class="form-actions">
        <button type="submit" class="button btn btn-primary input-small submitButton" name="_tdar.Login" id="btnLogin">Login</button>
    </div>

    </fieldset>
</form>