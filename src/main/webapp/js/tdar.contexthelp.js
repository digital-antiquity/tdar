/**
 * Contextual help service.
 */
TDAR.contexthelp = (function () {
    "use strict";

    /**
     * Display contextual help UI for the specified target element.  When displaying help-text, the service will
     * attempt to display the help-text near the target element without occluding the target element.
     *
     * While it can be any element, the expectation is that the target element represents a form field or a form
     * section. If the user- agent has enough screen space, the context-help appears in the right gutter beside the
     * target element.  If there is not enough horizontal space,  the context-help will appear as a "pop-up"  text box
     * above the target element -- occluding the elements that appear before the target.
     *
     * The content of the helptext is determined by the value of the target element's "data-tooltipcontent" attribute.
     * The value of the attribute can either be the help text to display, or  can specify element id prefixed by
     * hash e.g."#mytooltipdiv".  If an element ID, this this method will copy the html of the specified elemnent
     * into the context help UI.  Similarly,the target element can specify a label for the context help by supplying a
     * 'data-tiplabel' attribute.
     *
     * @param targetElem the form field or div described by the help text.
     */
    function setToolTipContents(targetElem) {
        var $targetElem = $(targetElem);
        var fieldOff = $targetElem.offset();
        var label;
        var content;

        // tooltip content can either be in 'data-tooltipcontent' attribute or in a
        if ($targetElem.data('tooltipcontent')) {
            content = $targetElem.data('tooltipcontent');
            // tooltip label can either be in atttribute, otherwise will be set to
            // the first h2
            label = $targetElem.data('tiplabel') || "";
            if (label) {
                label = "<h2>" + label + "</h2>";
            }
            if (content[0] === "#") {
                content = $(content).html();
            }
        } else {
            console.error("unable to bind tooltip - no tooltip element or tooltipcontent found");
        }
        var $notice = $("#notice:visible");
        if ($notice.length > 0) {
            var noteOff = $notice.offset();
            $notice.offset({
                left: noteOff.left,
                top: fieldOff.top
            });

            $notice.html(label + "<div id='noticecontent'>" + content + "</div>");
            //hack: if h2 in content, move it out.
            $notice.prepend($('#noticecontent h2').first().remove());
            $targetElem.popover("destroy");
        } else {
            $targetElem.popover({
                placement: 'top',
                trigger: 'hover',
                html: true,
                'title': label,
                'content': content
            });
        }
    }

    /**
     * initialize context-help functionality.
     *
     * @param form
     */
    function initializeTooltipContent(form) {
        if (typeof form === "undefined") {
            return;
        }
        $(form).on("mouseenter focusin", "[data-tooltipcontent]", function () {
            setToolTipContents(this);
        });
    }

    return {
        initializeTooltipContent: initializeTooltipContent
    };
})();
