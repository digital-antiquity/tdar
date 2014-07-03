<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

    <h2>Confirm duplication of ${resource.title}</h2>

    <@s.form name='deleteForm' id='deleteForm'  method='post' action='duplicate-final'>
        <@s.token name='struts.csrf.token' />
        <@s.submit type="submit" name="duplicate" value="duplicate" cssClass="btn button btn-warning"/>
        <@s.hidden name="id" />
    </@s.form>
