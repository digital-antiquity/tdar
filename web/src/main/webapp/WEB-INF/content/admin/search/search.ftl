<@s.form name='MetadataForm' id='MetadataForm'  method='post' cssClass="form-horizontal" enctype='multipart/form-data' action='lookup'>
    <@s.token name='struts.csrf.token' />
    <@s.select labelposition='top' label='QueryBuilder:' name='queryBuilder'
    list='%{allQueryBuilders}' />
    <br/>
    <@s.textarea labelposition='top' label='Raw Search Query' name='rawQuery'
    cssClass='resizable input-xxlarge' title="Please enter the query" cols="80" />

    <@s.select value="sortField" name='sortField'
    emptyOption='false' listValue='label' list='%{sortOptions}'/>

    <@s.submit name="search" value="search"/>
</@s.form>
