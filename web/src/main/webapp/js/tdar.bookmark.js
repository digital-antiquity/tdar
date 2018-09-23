const core = require("./tdar.core");
const common = require("./tdar.common");

var _init = function() {
    if ($("body").data("userid") != undefined) {
        _applyCollectionMenu();
        $("body").on("click", ".bookmark-link", _ajaxBookmark); // was TDAR.bookmark.ajaxBookmark
    }
};

var _applyCollectionMenu = function() {
    $('.bookmark-menu .dropdown-toggle').click(function() {
        var $menu = $(this);
        if (!$menu.hasClass('open')) {
            var $next = $menu.next("ul.dropdown-menu");
            $(".dropdown-menu").children(".extra").remove();
            $next.append($("<li class='extra'><img src='" + core.uri('images/ui-anim_basic_16x16.gif') + "' class='waiting' /></li>"));
            $.getJSON("/api/collection/tree?type=LIST", function(data) {
                $(".dropdown-menu").children(".extra").remove();
                var items = _buildCollectionTree(data);
                $next.append(items);

            });
        }
    });

};

var _buildCollectionTree = function(data) {
    var items = [];
    $.each(data, function(key, val) {
        var children;
        if (val.children) {
            var _child = _buildCollectionTree(val.children);
            children = $("<ul/>", {
                html : _child.join("")
            })
        }
        var item = "<li id='" + val.id + "' class='extra'><input type='radio' name='collectionId' value='" + val.id + "'> <a href='#'>" +
                common.htmlEncode(core.ellipsify(val.name, 80)) + "</a>";
        if (children != undefined) {
            // http://stackoverflow.com/questions/9758587/twitter-bootstrap-multilevel-dropdown-menu
            // https://vsn4ik.github.io/bootstrap-submenu/
        }
        item += "</li>"
        items.push(item);
    });
    return items;
}

/**
 * Click event handler used when user clicks on the "bookmark" icon beside a resource. If the resource is "bookmarked" it is tagged as a potential integration
 * source on the "integrate" page. This function shows the correct state (clicking the button togges the state on/off) and sends an ajax request to update the
 * bookmark status on the server-side
 * 
 * @returns {boolean}
 * @private
 */
var _ajaxBookmark = function() {
    var $this = $(this);
    var resourceId = $this.attr("resource-id");
    var state = $this.attr("bookmark-state");
    var $waitingElem = $("<img src='" + core.uri('images/ui-anim_basic_16x16.gif') + "' class='waiting' />");
    $this.prepend($waitingElem);
    var $icon = $(".bookmarkicon", $this);
    if ($icon == undefined || $icon.length == 0) {
        $icon = $this.parents(".bookmark-container").find(".bookmarkicon");
    }
    $icon.hide();
    // console.log(resourceId + ": " + state);
    var oldclass = "icon-star-empty";
    var newtext = "un-bookmark";
    var newstate = "bookmarked";
    var action = "bookmarkAjax";
    var newUrl = "/resource/removeBookmark?resourceId=" + resourceId;
    var newclass = "icon-star";

    if (state == 'bookmarked') {
        newtext = "bookmark";
        newstate = "bookmark";
        action = "removeBookmarkAjax";
        oldclass = "icon-star";
        newclass = "icon-star-empty";
        newUrl = "/resource/bookmark?resourceId=" + resourceId;
    }
    // var newclass = "tdar-icon-" + newstate;

    $.post(core.uri() + "api/resource/" + action + "?resourceId=" + resourceId, function(data) {
        if (data.success) {
            $(".bookmark-label", $this).text(newtext);
            $icon.removeClass(oldclass).addClass(newclass).show();
            $this.attr("bookmark-state", newstate);
            $this.attr("href", newUrl);
            $(".waiting", $this).remove();
            $this.find('.bookmark-label').text(newstate === 'bookmarked' ? 'Un-bookmark' : 'Bookmark');
        }
    });

    return false;
}

// expose public elements
module.exports = {
    "init" : _init,
    "ajaxBookmark" : _ajaxBookmark,

    // Main entrypoint - to be called by TDAR.main()
    "main" : _init
};