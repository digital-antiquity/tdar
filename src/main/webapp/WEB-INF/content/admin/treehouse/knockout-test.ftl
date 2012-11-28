<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>


<@s.form name='ImageMetadataForm' id='ImageMetadataForm'  method='post' cssClass="form-horizontal" action='save'>

    <h2>Misc Elements For Checking Alignment</h2>
    <div class="control-group">
        <label class="control-label">text input</label>
        <div class="controls">
            <input type="text" name="txt1" placeholder="txt1.input-xxlarge" class="input-xxlarge">
        </div>
    </div>

    <div class="control-group">
        <label class="control-label">text input</label>
        <div class="controls controls-row">
            <input type="text" name="txt2" placeholder="txt2.span3" class="span3">
        </div>
    </div>
    
    <h2>Checkboxlist Demo</h2>
    <div class="control-group">
        <label class="control-label">simple checkboxes</label>
        <div class="controls">
            <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
        </div>
    </div>
    
    <div class="control-group">
        <label class="control-label">checkbox columns</label>
        <div class="controls controls-row">
            <div class="span2">
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            </div>
            <div class="span2">
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            </div>
            <div class="span2">
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
                <label class="checkbox"><input type="checkbox" name="cb1">cb1</label>
            </div>
        </div>
    </div>
    
        <@s.checkboxlist 
                name='investigationTypeIds' 
                list='allInvestigationTypes' 
                listKey='id' 
                listValue='label'
                title='hi mom'
                listTitle="definition"
                numColumns="3" 
                spanClass="span3"
                label="Select Type(s)"  />
    

</@s.form>

<script>
$(function(){
TDAR.common.initEditPage("#ImageMetadataForm");
});
</script>

</body>
</#escape>