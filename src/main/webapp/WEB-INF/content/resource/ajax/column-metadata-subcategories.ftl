<#if !subcategories?? || subcategories.isEmpty() >
    <select id='subcategoryId_${index}' name='subcategoryIds[${index}]'>
        <option value='-1'>N/A</option>
    </select>
<#else>
    <@s.select id='subcategoryId_${index}' 
    name='subcategoryIds[${index}]'
    emptyOption='true'
    listKey='id'
    listValue='name'
    list='%{subcategories}' />
</#if>
