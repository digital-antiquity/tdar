
TDAR.namespace("contexthelp");
TDAR.contexthelp = (function() {
    "use strict";
    
    var self = {};
    
    function setToolTipContents(targetElem) {
        var $targetElem = $(targetElem);
        var fieldOff = $targetElem.offset();
        
        // tooltip content can either be in 'tooltipcontent' attribute or in a
        // separate div
        var label = "";
        var content = "";
        if ($targetElem.data('tooltipcontent')) {
            content = $targetElem.data('tooltipcontent');
            // tooltip label can either be in atttribute, otherwise will be set to
            // the first h2
            label = $targetElem.data('tiplabel') || "";
            if (label) {
                label = "<h2>" + label + "</h2>";
            }
            if (content[0] == "#") {
                content = $(content).html();
            }
        } else {
            console.error("unable to bind tooltip - no tooltip element or tooltipcontent found");
        }
        var $notice = $("#notice:visible");
        if ($notice.length > 0 ) {
            var noteOff = $notice.offset();
            $notice.offset({
                left : noteOff.left,
                top : fieldOff.top
            });
        
            $notice.html(label + "<div id='noticecontent'>" + content + "</div>");
            //hack: if h2 in content, move it out.
            $notice.prepend($('#noticecontent h2').first().remove());
            $targetElem.popover("destroy");
        } else {
            $targetElem.popover({
                placement:'top',
                trigger:'hover',
                html:true,
                'title': label,
                'content': content
            });
        }
    }
    
    function initializeTooltipContent(form) {
        if(typeof form === "undefined") return;
        
        //console.debug('delegating tooltips');
        $(form).on("mouseenter focusin", "[data-tooltipcontent]",  function() {
            setToolTipContents(this);
        });
    }
    
    self.initializeTooltipContent = initializeTooltipContent;

    return self;    
})();
