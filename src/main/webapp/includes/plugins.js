//TODO: this is just a bundle of plugins in one file.   Remove this file and bundle using exisiting conventions (i.e. header.ftl) 
/**
 * plugins included:
 * - bxSlider 3.0
 * - Easing v1.3 (required by bxSlider, i think)
 * - NiceIt v1.0 
 */





/**
 * jQuery bxSlider v3.0
 * http://bxslider.com
 *
 * Copyright 2010, Steven Wanderski
 * http://bxcreative.com
 *
 * Free to use and abuse under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 * 
 */

(function(a){a.fn.bxSlider=function(b){function Z(b,c,d,e){var f=[];var g=d;var h=false;if(e=="backward"){b=a.makeArray(b);b.reverse()}while(g>0){a.each(b,function(b,d){if(g>0){if(!h){if(b==c){h=true;f.push(a(this).clone());g--}}else{f.push(a(this).clone());g--}}else{return false}})}return f}function Y(){var a=i.outerHeight()*b.displaySlideQty;return a}function X(){var a=i.outerWidth()*b.displaySlideQty;return a}function W(b,c){if(c=="left"){var d=a(".page",h).eq(b).position().left}else if(c=="top"){var d=a(".page",h).eq(b).position().top}return d}function V(){if(!b.infiniteLoop&&b.hideControlOnEnd){if(x==F){a(".bx-prev",h).hide()}else{a(".bx-prev",h).show()}if(x==G){a(".bx-next",h).hide()}else{a(".bx-next",h).show()}}}function U(c,e,f,g){p=a('<a href="" class="bx-start"></a>');if(c=="text"){r=e}else{r='<img src="'+e+'" />'}if(f=="text"){s=g}else{s='<img src="'+g+'" />'}if(b.autoControlsSelector){a(b.autoControlsSelector).append(p)}else{h.append('<div class="bx-auto"></div>');a(".bx-auto",h).html(p)}p.click(function(){if(b.ticker){if(a(this).hasClass("stop")){d.stopTicker()}else if(a(this).hasClass("start")){d.startTicker()}}else{if(a(this).hasClass("stop")){d.stopShow(true)}else if(a(this).hasClass("start")){d.startShow(true)}}return false})}function T(){var c=a("img",g.eq(x)).attr("title");if(c!=""){if(b.captionsSelector){a(b.captionsSelector).html(c)}else{a(".bx-captions",h).html(c)}}else{if(b.captionsSelector){a(b.captionsSelector).html(" ")}else{a(".bx-captions",h).html(" ")}}}function S(c){var e=g.length;if(b.moveSlideQty>1){if(g.length%b.moveSlideQty!=0){e=Math.ceil(g.length/b.moveSlideQty)}else{e=g.length/b.moveSlideQty}}var f="";if(b.buildpage){for(var i=0;i<e;i++){f+=b.buildpage(i,g.eq(i*b.moveSlideQty))}}else if(c=="full"){for(var i=1;i<=e;i++){f+='<a href="" class="page-link page-'+i+'">'+i+"</a>"}}else if(c=="short"){f='<span class="bx-page-current">'+(b.startingSlide+1)+"</span> "+b.pageShortSeparator+' <span class="bx-page-total">'+g.length+"</span>"}if(b.pageSelector){a(b.pageSelector).append(f);n=a(b.pageSelector)}else{var j=a('<div class="bx-page"></div>');j.append(f);if(b.pageLocation=="top"){h.prepend(j)}else if(b.pageLocation=="bottom"){h.append(j)}n=a(".bx-page",h)}n.children().click(function(){if(b.pageType=="full"){var a=n.children().index(this);if(b.moveSlideQty>1){a*=b.moveSlideQty}d.goToSlide(a)}return false})}function R(c,e,f,g){var i=a('<a href="" class="bx-next"></a>');var j=a('<a href="" class="bx-prev"></a>');if(c=="text"){i.html(e)}else{i.html('<img src="'+e+'" />')}if(f=="text"){j.html(g)}else{j.html('<img src="'+g+'" />')}if(b.prevSelector){a(b.prevSelector).append(j)}else{h.append(j)}if(b.nextSelector){a(b.nextSelector).append(i)}else{h.append(i)}i.click(function(){d.goToNextSlide();return false});j.click(function(){d.goToPreviousSlide();return false})}function Q(c){if(b.pageType=="full"&&b.page){a("a",n).removeClass(b.pageActiveClass);a("a",n).eq(c).addClass(b.pageActiveClass)}else if(b.pageType=="short"&&b.page){a(".bx-page-current",n).html(x+1)}}function P(){g.not(":eq("+x+")").fadeTo(b.speed,0).css("zIndex",98);g.eq(x).css("zIndex",99).fadeTo(b.speed,1,function(){E=false;if(jQuery.browser.msie){g.eq(x).get(0).style.removeAttribute("filter")}b.onAfterSlide(x,g.length,g.eq(x))})}function O(){e.hover(function(){if(t){d.stopTicker(false)}},function(){if(t){d.startTicker(false)}})}function N(){h.find(".bx-window").hover(function(){if(t){d.stopShow(false)}},function(){if(t){d.startShow(false)}})}function M(){if(b.startImage!=""){startContent=b.startImage;startType="image"}else{startContent=b.startText;startType="text"}if(b.stopImage!=""){stopContent=b.stopImage;stopType="image"}else{stopContent=b.stopText;stopType="text"}U(startType,startContent,stopType,stopContent)}function L(a,c,d){if(b.mode=="horizontal"){if(b.tickerDirection=="next"){e.animate({left:"-="+c+"px"},d,"linear",function(){e.css("left",a);L(a,A,b.tickerSpeed)})}else if(b.tickerDirection=="prev"){e.animate({left:"+="+c+"px"},d,"linear",function(){e.css("left",a);L(a,A,b.tickerSpeed)})}}else if(b.mode=="vertical"){if(b.tickerDirection=="next"){e.animate({top:"-="+c+"px"},d,"linear",function(){e.css("top",a);L(a,B,b.tickerSpeed)})}else if(b.tickerDirection=="prev"){e.animate({top:"+="+c+"px"},d,"linear",function(){e.css("top",a);L(a,B,b.tickerSpeed)})}}}function K(){if(b.auto){if(!b.infiniteLoop){if(b.autoDirection=="next"){o=setInterval(function(){x+=b.moveSlideQty;if(x>G){x=x%g.length}d.goToSlide(x,false)},b.pause)}else if(b.autoDirection=="prev"){o=setInterval(function(){x-=b.moveSlideQty;if(x<0){negativeOffset=x%g.length;if(negativeOffset==0){x=0}else{x=g.length+negativeOffset}}d.goToSlide(x,false)},b.pause)}}else{if(b.autoDirection=="next"){o=setInterval(function(){d.goToNextSlide(false)},b.pause)}else if(b.autoDirection=="prev"){o=setInterval(function(){d.goToPreviousSlide(false)},b.pause)}}}else if(b.ticker){b.tickerSpeed*=10;a(".page",h).each(function(b){A+=a(this).width();B+=a(this).height()});if(b.tickerDirection=="prev"&&b.mode=="horizontal"){e.css("left","-"+(A+y)+"px")}else if(b.tickerDirection=="prev"&&b.mode=="vertical"){e.css("top","-"+(B+z)+"px")}if(b.mode=="horizontal"){C=parseInt(e.css("left"));L(C,A,b.tickerSpeed)}else if(b.mode=="vertical"){D=parseInt(e.css("top"));L(D,B,b.tickerSpeed)}if(b.tickerHover){O()}}}function J(){if(b.nextImage!=""){nextContent=b.nextImage;nextType="image"}else{nextContent=b.nextText;nextType="text"}if(b.prevImage!=""){prevContent=b.prevImage;prevType="image"}else{prevContent=b.prevText;prevType="text"}R(nextType,nextContent,prevType,prevContent)}function I(){if(b.mode=="horizontal"||b.mode=="vertical"){var c=Z(g,0,b.moveSlideQty,"backward");a.each(c,function(b){e.prepend(a(this))});var d=g.length+b.moveSlideQty-1;var f=g.length-b.displaySlideQty;var h=d-f;var i=Z(g,0,h,"forward");if(b.infiniteLoop){a.each(i,function(b){e.append(a(this))})}}}function H(){I(b.startingSlide);if(b.mode=="horizontal"){e.wrap('<div class="'+b.wrapperClass+'" style="width:'+l+'px; position:relative;"></div>').wrap('<div class="bx-window" style="position:relative; overflow:hidden; width:'+l+'px;"></div>').css({width:"999999px",position:"relative",left:"-"+y+"px"});e.children().css({width:j,"float":"left",listStyle:"none"});h=e.parent().parent();g.addClass("page")}else if(b.mode=="vertical"){e.wrap('<div class="'+b.wrapperClass+'" style="width:'+v+'px; position:relative;"></div>').wrap('<div class="bx-window" style="width:'+v+"px; height:"+m+'px; position:relative; overflow:hidden;"></div>').css({height:"999999px",position:"relative",top:"-"+z+"px"});e.children().css({listStyle:"none",height:w});h=e.parent().parent();g.addClass("page")}else if(b.mode=="fade"){e.wrap('<div class="'+b.wrapperClass+'" style="width:'+v+'px; position:relative;"></div>').wrap('<div class="bx-window" style="height:'+w+"px; width:"+v+'px; position:relative; overflow:hidden;"></div>');e.children().css({listStyle:"none",position:"absolute",top:0,left:0,zIndex:98});h=e.parent().parent();g.not(":eq("+x+")").fadeTo(0,0);g.eq(x).css("zIndex",99)}if(b.captions&&b.captionsSelector==null){h.append('<div class="bx-captions"></div>')}}var c={mode:"horizontal",infiniteLoop:true,hideControlOnEnd:false,controls:true,speed:500,easing:"swing",page:false,pageSelector:null,pageType:"full",pageLocation:"bottom",pageShortSeparator:"/",pageActiveClass:"page-active",nextText:"next",nextImage:"",nextSelector:null,prevText:"prev",prevImage:"",prevSelector:null,captions:false,captionsSelector:null,auto:false,autoDirection:"next",autoControls:false,autoControlsSelector:null,autoStart:true,autoHover:false,autoDelay:0,pause:3e3,startText:"start",startImage:"",stopText:"stop",stopImage:"",ticker:false,tickerSpeed:5e3,tickerDirection:"next",tickerHover:false,wrapperClass:"bx-wrapper",startingSlide:0,displaySlideQty:1,moveSlideQty:1,randomStart:false,onBeforeSlide:function(){},onAfterSlide:function(){},onLastSlide:function(){},onFirstSlide:function(){},onNextSlide:function(){},onPrevSlide:function(){},buildpage:null};var b=a.extend(c,b);var d=this;var e="";var f="";var g="";var h="";var i="";var j="";var k="";var l="";var m="";var n="";var o="";var p="";var q="";var r="";var s="";var t=true;var u=false;var v=0;var w=0;var x=0;var y=0;var z=0;var A=0;var B=0;var C=0;var D=0;var E=false;var F=0;var G=g.length-1;this.goToSlide=function(a,c){if(!E){E=true;x=a;b.onBeforeSlide(x,g.length,g.eq(x));if(typeof c=="undefined"){var c=true}if(c){if(b.auto){d.stopShow(true)}}slide=a;if(slide==F){b.onFirstSlide(x,g.length,g.eq(x))}if(slide==G){b.onLastSlide(x,g.length,g.eq(x))}if(b.mode=="horizontal"){e.animate({left:"-"+W(slide,"left")+"px"},b.speed,b.easing,function(){E=false;b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="vertical"){e.animate({top:"-"+W(slide,"top")+"px"},b.speed,b.easing,function(){E=false;b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="fade"){P()}V();if(b.moveSlideQty>1){a=Math.floor(a/b.moveSlideQty)}Q(a);T()}};this.goToNextSlide=function(a){if(typeof a=="undefined"){var a=true}if(a){if(b.auto){d.stopShow(true)}}if(!b.infiniteLoop){if(!E){var c=false;x=x+b.moveSlideQty;if(x<=G){V();b.onNextSlide(x,g.length,g.eq(x));d.goToSlide(x)}else{x-=b.moveSlideQty}}}else{if(!E){E=true;var c=false;x=x+b.moveSlideQty;if(x>G){x=x%g.length;c=true}b.onNextSlide(x,g.length,g.eq(x));b.onBeforeSlide(x,g.length,g.eq(x));if(b.mode=="horizontal"){var f=b.moveSlideQty*k;e.animate({left:"-="+f+"px"},b.speed,b.easing,function(){E=false;if(c){e.css("left","-"+W(x,"left")+"px")}b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="vertical"){var h=b.moveSlideQty*w;e.animate({top:"-="+h+"px"},b.speed,b.easing,function(){E=false;if(c){e.css("top","-"+W(x,"top")+"px")}b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="fade"){P()}if(b.moveSlideQty>1){Q(Math.ceil(x/b.moveSlideQty))}else{Q(x)}T()}}};this.goToPreviousSlide=function(c){if(typeof c=="undefined"){var c=true}if(c){if(b.auto){d.stopShow(true)}}if(!b.infiniteLoop){if(!E){var f=false;x=x-b.moveSlideQty;if(x<0){x=0;if(b.hideControlOnEnd){a(".bx-prev",h).hide()}}V();b.onPrevSlide(x,g.length,g.eq(x));d.goToSlide(x)}}else{if(!E){E=true;var f=false;x=x-b.moveSlideQty;if(x<0){negativeOffset=x%g.length;if(negativeOffset==0){x=0}else{x=g.length+negativeOffset}f=true}b.onPrevSlide(x,g.length,g.eq(x));b.onBeforeSlide(x,g.length,g.eq(x));if(b.mode=="horizontal"){var i=b.moveSlideQty*k;e.animate({left:"+="+i+"px"},b.speed,b.easing,function(){E=false;if(f){e.css("left","-"+W(x,"left")+"px")}b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="vertical"){var j=b.moveSlideQty*w;e.animate({top:"+="+j+"px"},b.speed,b.easing,function(){E=false;if(f){e.css("top","-"+W(x,"top")+"px")}b.onAfterSlide(x,g.length,g.eq(x))})}else if(b.mode=="fade"){P()}if(b.moveSlideQty>1){Q(Math.ceil(x/b.moveSlideQty))}else{Q(x)}T()}}};this.goToFirstSlide=function(a){if(typeof a=="undefined"){var a=true}d.goToSlide(F,a)};this.goToLastSlide=function(){if(typeof a=="undefined"){var a=true}d.goToSlide(G,a)};this.getCurrentSlide=function(){return x};this.getSlideCount=function(){return g.length};this.stopShow=function(a){clearInterval(o);if(typeof a=="undefined"){var a=true}if(a&&b.autoControls){p.html(r).removeClass("stop").addClass("start");t=false}};this.startShow=function(a){if(typeof a=="undefined"){var a=true}K();if(a&&b.autoControls){p.html(s).removeClass("start").addClass("stop");t=true}};this.stopTicker=function(a){e.stop();if(typeof a=="undefined"){var a=true}if(a&&b.ticker){p.html(r).removeClass("stop").addClass("start");t=false}};this.startTicker=function(a){if(b.mode=="horizontal"){if(b.tickerDirection=="next"){var c=parseInt(e.css("left"));var d=A+c+g.eq(0).width()}else if(b.tickerDirection=="prev"){var c=-parseInt(e.css("left"));var d=c-g.eq(0).width()}var f=d*b.tickerSpeed/A;L(C,d,f)}else if(b.mode=="vertical"){if(b.tickerDirection=="next"){var h=parseInt(e.css("top"));var d=B+h+g.eq(0).height()}else if(b.tickerDirection=="prev"){var h=-parseInt(e.css("top"));var d=h-g.eq(0).height()}var f=d*b.tickerSpeed/B;L(D,d,f);if(typeof a=="undefined"){var a=true}if(a&&b.ticker){p.html(s).removeClass("start").addClass("stop");t=true}}};this.initShow=function(){e=a(this);f=e.clone();g=e.children();h="";i=e.children(":first");j=i.width();v=0;k=i.outerWidth();w=0;l=X();m=Y();E=false;n="";x=0;y=0;z=0;o="";p="";q="";r="";s="";t=true;u=false;A=0;B=0;C=0;D=0;F=0;G=g.length-1;g.each(function(b){if(a(this).outerHeight()>w){w=a(this).outerHeight()}if(a(this).outerWidth()>v){v=a(this).outerWidth()}});if(b.randomStart){var c=Math.floor(Math.random()*g.length);x=c;y=k*(b.moveSlideQty+c);z=w*(b.moveSlideQty+c)}else{x=b.startingSlide;y=k*(b.moveSlideQty+b.startingSlide);z=w*(b.moveSlideQty+b.startingSlide)}H();if(b.page&&!b.ticker){if(b.pageType=="full"){S("full")}else if(b.pageType=="short"){S("short")}}if(b.controls&&!b.ticker){J()}if(b.auto||b.ticker){if(b.autoControls){M()}if(b.autoStart){setTimeout(function(){d.startShow(true)},b.autoDelay)}else{d.stopShow(true)}if(b.autoHover&&!b.ticker){N()}}if(b.moveSlideQty>1){Q(Math.ceil(x/b.moveSlideQty))}else{Q(x)}V();if(b.captions){T()}b.onAfterSlide(x,g.length,g.eq(x))};this.destroyShow=function(){clearInterval(o);a(".bx-next, .bx-prev, .bx-page, .bx-auto",h).remove();e.unwrap().unwrap().removeAttr("style");e.children().removeAttr("style").not(".page").remove();g.removeClass("page")};this.reloadShow=function(){d.destroyShow();d.initShow()};this.each(function(){if(a(this).children().length>0){d.initShow()}});return this};jQuery.fx.prototype.cur=function(){if(this.elem[this.prop]!=null&&(!this.elem.style||this.elem.style[this.prop]==null)){return this.elem[this.prop]}var a=parseFloat(jQuery.css(this.elem,this.prop));return a}})(jQuery)


/*
 * jQuery Easing v1.3 - http://gsgd.co.uk/sandbox/jquery/easing/
 *
*/


// t: current time, b: begInnIng value, c: change In value, d: duration
jQuery.easing['jswing'] = jQuery.easing['swing'];

jQuery.extend( jQuery.easing,
{
	def: 'easeOutQuad',
	swing: function (x, t, b, c, d) {
		//alert(jQuery.easing.default);
		return jQuery.easing[jQuery.easing.def](x, t, b, c, d);
	},
	easeInQuad: function (x, t, b, c, d) {
		return c*(t/=d)*t + b;
	},
	easeOutQuad: function (x, t, b, c, d) {
		return -c *(t/=d)*(t-2) + b;
	},
	easeInOutQuad: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t + b;
		return -c/2 * ((--t)*(t-2) - 1) + b;
	},
	easeInCubic: function (x, t, b, c, d) {
		return c*(t/=d)*t*t + b;
	},
	easeOutCubic: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t + 1) + b;
	},
	easeInOutCubic: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	},
	easeInQuart: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t + b;
	},
	easeOutQuart: function (x, t, b, c, d) {
		return -c * ((t=t/d-1)*t*t*t - 1) + b;
	},
	easeInOutQuart: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
		return -c/2 * ((t-=2)*t*t*t - 2) + b;
	},
	easeInQuint: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t*t + b;
	},
	easeOutQuint: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t*t*t + 1) + b;
	},
	easeInOutQuint: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
		return c/2*((t-=2)*t*t*t*t + 2) + b;
	},
	easeInSine: function (x, t, b, c, d) {
		return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
	},
	easeOutSine: function (x, t, b, c, d) {
		return c * Math.sin(t/d * (Math.PI/2)) + b;
	},
	easeInOutSine: function (x, t, b, c, d) {
		return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
	},
	easeInExpo: function (x, t, b, c, d) {
		return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
	},
	easeOutExpo: function (x, t, b, c, d) {
		return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
	},
	easeInOutExpo: function (x, t, b, c, d) {
		if (t==0) return b;
		if (t==d) return b+c;
		if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
		return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
	},
	easeInCirc: function (x, t, b, c, d) {
		return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
	},
	easeOutCirc: function (x, t, b, c, d) {
		return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
	},
	easeInOutCirc: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
		return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
	},
	easeInElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
	},
	easeOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
	},
	easeInOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (!p) p=d*(.3*1.5);
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
		return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
	},
	easeInBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*(t/=d)*t*((s+1)*t - s) + b;
	},
	easeOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
	},
	easeInOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158; 
		if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
		return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
	},
	easeInBounce: function (x, t, b, c, d) {
		return c - jQuery.easing.easeOutBounce (x, d-t, 0, c, d) + b;
	},
	easeOutBounce: function (x, t, b, c, d) {
		if ((t/=d) < (1/2.75)) {
			return c*(7.5625*t*t) + b;
		} else if (t < (2/2.75)) {
			return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
		} else if (t < (2.5/2.75)) {
			return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
		} else {
			return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
		}
	},
	easeInOutBounce: function (x, t, b, c, d) {
		if (t < d/2) return jQuery.easing.easeInBounce (x, t*2, 0, c, d) * .5 + b;
		return jQuery.easing.easeOutBounce (x, t*2-d, 0, c, d) * .5 + c*.5 + b;
	}
});

/**
 * jQuery NiceIt plugin
 *
 * jNiceIt jQuery light-weight plugin which converts boring generic user controls (i.e. text input boxes, textareas, selectboxes, buttons, etc) to a fully customizable controls. 
 * You may use this plugin to emulate any operating system (for example, MacOS, Windows 7) or create your own fantastic UI. 
 * jNiceIt was created with cross-browser functionality in mind and was tested on large forms.
 * The plugin does not use absolute positioning for skinned controls and is fully compatible with fluid layouts.
 *
 * Current version of jNiceIt is a stable release but it does not support theming yet. 
 * You can customize any control by changing one general CSS file only, NO JavaScript modifications are required.
 *
 * @name jquery.nice-it.1.0.js
 * @author ajaxBlender.com - http://www.ajaxBlender.com
 * @version 1.0
 * @date January 13, 2010
 * @category jQuery plugin
 * @copyright (c) 2010 ajaxBlender.com
 * @example Visit http://www.ajaxBlender.com/ for more informations about this jQuery plugin
 */

(function($) {

	/**
	 * $ is an alias to jQuery object
	 *
	 */

	$.fn.NiceIt = function(settings) {
		settings = jQuery.extend({ // Settings
			Version: 		'1.0'
		}, settings);
		
		var jQueryMatchedObj = this; // This, in this context, refer to jQuery object
		
		/**
		 * Initializing the plugin
		 */
		Run(this, jQueryMatchedObj);
		
		/**
		 * Start the jQuery NiceIt plugin
		 *
		 * @param object objForm The object (form) which the user wants to stylish
		 * @param object jQueryMatchedObj The jQuery object with all elements matched
		 */
		 
		function Run(objForm, jQueryMatchedObj) {
			for ( var idx = 0; idx < jQueryMatchedObj.length; idx++ ) {
			
				var form = $(jQueryMatchedObj[idx]);
				
				if(!$(form).attr('id')) { $(form).attr('id', 'fm-' + idx); }
				
				$(form).setTabIndexes();
				$(form).fnReplaceCheckBoxes();
				$(form).fnReplaceRadioButtons();
				$(form).fnReplaceInputBoxes();
				$(form).fnReplaceSelectBoxes();
				$(form).fnReplaceSelectboxesM();
				$(form).fnReplaceTextareas();
				$(form).fnReplaceButtons();
				$(form).fnReplaceFiles();
			}
		}
		
		return;
	};
	
	/*
	 * Set up tab indexes to all form elements for correct tab navigation
	 */
	$.fn.setTabIndexes = function() {
		$(this).find('select, input:not(:hidden), textarea, button').each(function(i, ctrl) {
			$(ctrl).attr('tabindex', (i + 1));
		});
	}
	
	/*
	 * Run replacement Checkboxes
	 */
	$.fn.fnReplaceCheckBoxes = function() {
		var objForm = this;
		$(objForm).find('input[type="checkbox"]').each(function(i, ctrl) {
			$(ctrl).hide();
	        
			var id = 'fmCbx-' + $(objForm).attr('id') + '-' + (i + 1); 

			if($(ctrl).attr('id')) { id = 'fmChbx-' + $(ctrl).attr('id'); }
				        
	        $(ctrl).after('<b class="fmCheckbox' + ($(ctrl).attr('checked') ? ' checked' : '') + '" id="' + id + '">&nbsp;</b>');
	        
	        var nCtrl = $('#' + id);
	        
			if($(ctrl).attr('disabled')) { 
				$(nCtrl).addClass('chbx-disabled');
				return;
			}
			
			$(ctrl).bind('click', function () {
	        	if(!$(ctrl).attr('checked')) { $(nCtrl).removeClass('checked'); } else { $(nCtrl).addClass('checked'); }
	        });
	        
	        $(nCtrl).bind('click', function () {
				if($(ctrl).attr('checked')) {
					$(ctrl).attr('checked', false);
					$(nCtrl).removeClass('checked');
				} else {
					$(ctrl).attr('checked', true);
					$(nCtrl).addClass('checked');
				}
	        });
			/**/	        
	        $('label[for="' + $(ctrl).attr('id') + '"]').bind('click', function () {
				if($(ctrl).attr('checked')) {
					$(ctrl).attr('checked', false);
					$(nCtrl).removeClass('checked');
				} else {
					$(ctrl).attr('checked', true);
					$(nCtrl).addClass('checked');
				}
	        });
	    });
	}
	
	/*
	 * Run replacement of Radio Buttons
	 */
	$.fn.fnReplaceRadioButtons = function() {
		var objForm = this;
		$(objForm).find('input[type="radio"]').each(function(i, ctrl) {
			$(ctrl).hide();

			var id = 'fmRbtn-' + $(objForm).attr('id') + '-' + (i + 1);
			
			if($(ctrl).attr('id')) { id = 'fmRbtn-' + $(ctrl).attr('id'); }

	        $(ctrl).after('<a rel="' + $(ctrl).attr('name') + '" class="fmRadio' + ($(ctrl).attr('checked') ? ' checked' : '') + '" id="' + id + '">&nbsp;</a>');
	        
			var nCtrl = $('#' + id); 
			
			if($(ctrl).attr('disabled')) { 
				$(nCtrl).addClass('rbtn-disabled');
				return;
			}
			
			$(ctrl).bind('click', function () {
				$('.fmRadio[rel="' + $(ctrl).attr('name') + '"]').removeClass('checked');
				$(nCtrl).addClass('checked');
	        });
	        
	        $(nCtrl).bind('click', function () {
				$('.fmRadio[rel="' + $(nCtrl).attr('rel') + '"]').removeClass('checked');
				$('input[name="' + $(nCtrl).attr('rel') + '"]').attr('checked', false);
				
				$(this).addClass('checked');
				$(ctrl).attr('checked', true);
				
				return false;
	        });
	    });			
	}
	
	/*
	 * Run replacement of Inputs
	 */
	$.fn.fnReplaceInputBoxes = function() {
		var objForm = this;
		
		$(objForm).find('input[type="text"],input[type="password"]').each(function (i, ctrl) {
		
			var width = '50%';
	    	
	    	if($(ctrl).css('width') != 'auto' && $(ctrl).parent().css('width') != 'auto') {
		    	width = Math.ceil( 100 * parseFloat($(ctrl).css('width')) / parseFloat($(ctrl).parent().css('width')) ) + '%';
	    	}  
	    	
	    	var id = 'fmInp-' + $(objForm).attr('id') + '-' + (i + 1);
	    	if($(ctrl).attr('id')) { 
	    		id = 'fmInp-' + $(ctrl).attr('id'); 
	    	}
	    	
	    	$(ctrl).width('100%');
	    	$(ctrl).wrap('<span class="fmInput" id="' + id + '"><span></span></span>');
	    	
	    	var nCtrl = $('#' + id);
	    	
	    	$(nCtrl).width(width);
	    	
	    	if($(ctrl).attr('disabled')) { $(nCtrl).addClass('disabled'); }
	    	
	    	$(nCtrl).swapStyles($(ctrl));
	    	
	    	$(ctrl).bind('focus', function () { 
	    		if($(ctrl).val() == $(ctrl).attr('title')) { $(ctrl).val(''); } 
	    		$(nCtrl).CtrlInFocus();
	    	});
			$(ctrl).bind('blur', function () { 
				if($(ctrl).val() == '') { $(ctrl).val($(ctrl).attr('title')); }
				$(nCtrl).CtrlOutFocus();
			});
	    });		
	}
	
	/*
	 * Run replacement of Select Boxes
	 */
	$.fn.fnReplaceSelectBoxes = function() {
		var objForm = this;
		var cite = 'strong span cite';
		
	    $(objForm).find('select').each(function (i, ctrl) {
			
			if($(ctrl).attr('multiple')) { return; }
			
			var width = '50%';
	    	
	    	if($(ctrl).css('width') != 'auto' && $(ctrl).parent().css('width') != 'auto') {
		    	width = Math.ceil( 100 * parseFloat($(ctrl).css('width')) / parseFloat($(ctrl).parent().css('width')) ) + '%';
	    	}
	    	
	    	var id = 'fmCbox-' + $(objForm).attr('id') + '-' + (i + 1);
	    	
	    	if($(ctrl).attr('id')) { 
	    		id = $(ctrl).attr('id');
	    		$(ctrl).attr('id', '');
	    	}
	    	
	    	$(ctrl).width('100%');
	    	
	    	var selectedTxt = ($(ctrl).find('option:selected').text() != '' ? $(ctrl).find('option:selected').text() : '&nbsp;');
	    	
	    	$(ctrl).before('<div class="fmSelect" tabindex="' + $(ctrl).attr('tabindex') + '" id="' + id + '"><strong><span><cite>' + selectedTxt + '</cite></span></strong><ul></ul></div>');
	    	$(ctrl).attr('tabindex', '');
	    	$(ctrl).hide();
	    		
	    	var nCtrl = $('#' + id);
	    	
	    	$(nCtrl).width(width);
	    	$(nCtrl).swapStyles($(ctrl));
	    	
	    	/* Define Events  */
	    	
	    	$(nCtrl).bind('click', function () { $(this).find('ul').show(); });
	    	$(nCtrl).bind('mouseleave', function () { $(this).find('ul').hide(); });
	    	
	    	$(nCtrl).bind('focus', function () { 
	    		$('body').focus();
	    		$('.fmSelect').css('z-index', '100');
	    		$(nCtrl).css('z-index', '1500'); 
	
	    		$(this).CtrlInFocus();
	    	});
	    	
			$(nCtrl).bind('blur', function () { $(this).CtrlOutFocus(); });
	    	
	    	$(ctrl).find('option').each(function (idx, item) {
	    		nCtrl.find('ul').append('<li option="' + $(item).attr('value') + '">' + $(item).text() + '</li>');
	    	});
	    	
	    	nCtrl.find('li').each(function(num, elem) {
	    		$(elem).bind('mouseenter', function() { $(this).addClass('active'); });
	    		$(elem).bind('mouseout', function() { $(this).removeClass('active'); });
	    		
	    		$(elem).bind('click', function() { 
	    			nCtrl.find(cite).text($(this).text());
	    			$(ctrl).val($(this).attr('option'));
	    			nCtrl.find('ul').fadeOut();
	    		});
	    	});
	    	
	    	nCtrl.bind('keydown', function(event) {
	    		var selected = $(ctrl).find('option:selected');
	    		
	    		$(nCtrl).find('ul').hide();
	    		
	    		if(event.keyCode == 38) { // Up Key
	    			if(selected.val() != $(ctrl).find('option:first').val()) {
	    				selected.prev().attr('selected', true);
	    				$(nCtrl).find(cite).text(selected.prev().text());
	    			}   
	    			return false;
	    		} 
	    		
	    		if(event.keyCode == 40) { // Down Key
	    			if(selected.val() != $(ctrl).find('option:last').val()) {
	    				selected.next().attr('selected', true);
	    				$(nCtrl).find(cite).text(selected.next().text());
	    			}   
	    			return false;
	    		}	
	    		
	    		if(event.keyCode == 33) { // PageUp Key
	    			$(ctrl).find('option:first').attr('selected', true);
					$(nCtrl).find(cite).text($(ctrl).find('option:first').text());
					
	    			return false;
	    		}	
	    		
	    		if(event.keyCode == 34) { // PageDown Key
	    			$(ctrl).find('option:last').attr('selected', true);
					$(nCtrl).find(cite).text($(ctrl).find('option:last').text());
	
	    			return false;
	    		}	
	    	});
	    	
	    	$(ctrl).bind('change', function() {
	    		nCtrl.find(cite).text($(this).find('option:selected').text());	
	    	});
	    });
	}
	
	/*
	 * Run replacement of List Boxes
	 */
	$.fn.fnReplaceSelectboxesM = function() {
		var objForm = this;
		
		$(objForm).find('select[multiple]').each(function (i, ctrl) {
	    	
	    	var width = '100%';
			var height = $(ctrl).height();
	    	
	    	if($(ctrl).css('width') != 'auto' && $(ctrl).parent().css('width') != 'auto') {
		    	width = Math.ceil( 100 * parseFloat($(ctrl).css('width')) / parseFloat($(ctrl).parent().css('width')) ) + '%';
	    	} 
	    	
			var id = 'fmMsel-' + $(objForm).attr('id') + '-' + (i + 1);
	    	
	    	if($(ctrl).attr('id')) { 
	    		id = $(ctrl).attr('id');
	    		$(ctrl).attr('id', '');
	    	}
	    	
	    	$(ctrl).wrap('<span class="fmMultipleSelect" id="' + id + '"><span><span><span></span></span></span></span>');
	    	
	    	var nCtrl = $('#' + id);
	    	
	    	$(nCtrl).width(width);
	    	$(ctrl).width('96%');
	    	$(ctrl).height(height);
	    	
	    	/* Define Events  */
	    	
	    	$(ctrl).bind('focus', function () { $(nCtrl).CtrlInFocus(); });
			$(ctrl).bind('blur', function () { $(nCtrl).CtrlOutFocus(); });
	    });
	}
	
	/*
	 * Run replacement of Textareas
	 */
	$.fn.fnReplaceTextareas = function() {
		var objForm = this;
		
		$(objForm).find('textarea').each(function (i, ctrl) {
			
			var width = '50%';
			var height = $(ctrl).height();
	    	
	    	if($(ctrl).css('width') != 'auto' && $(ctrl).parent().css('width') != 'auto') {
		    	width = Math.ceil( 100 * parseFloat($(ctrl).css('width')) / parseFloat($(ctrl).parent().css('width')) ) + '%';
	    	} 
	    	
	    	var id = 'fmTarea-' + $(objForm).attr('id') + '-' + (i + 1);
	    	if($(ctrl).attr('id')) { id = 'fmTarea-' + $(ctrl).attr('id'); }
	    	
	    	$(ctrl).wrap('<span class="fmTextarea" id="' + id + '"><span><span><span></span></span></span></span>');
	    	
	    	var nCtrl = $('#' + id);
	    	$(nCtrl).width(width);
	    	$(ctrl).height(height);
	    	
	    	if($(ctrl).attr('disabled')) { $(nCtrl).addClass('tx-disabled'); }
	    	
	    	$(ctrl).bind('focus', function () { $(nCtrl).CtrlInFocus(); });
			$(ctrl).bind('blur', function () { $(nCtrl).CtrlOutFocus(); });
	    });
	}
	
	/*
	 * Run replacement of Buttons
	 */
	$.fn.fnReplaceButtons = function() {
		var objForm = this;
		
		$(objForm).find('button:not(.fmButton)').each(function(i, ctrl) {
			$(ctrl).addClass('fmButton');
			
			if($(ctrl).attr('type').toLowerCase() == 'submit') { 
				$(ctrl).wrapInner('<strong><span></span></strong>');
			} else { 
				$(ctrl).wrapInner('<span><span></span></span>');
			}
			
			$(ctrl).bind('focus', function () { $(ctrl).CtrlInFocus(); });
			$(ctrl).bind('blur', function () { $(ctrl).CtrlOutFocus(); });
		});
	}
	
	/*
	 * Run replacement of File Inputs
	 */
	$.fn.fnReplaceFiles = function() {
		var objForm = this;
		
		$(objForm).find('input[type="file"]').each(function (i, ctrl) {
			
			var width = '50%';
	    	
	    	if($(ctrl).css('width') != 'auto' && $(ctrl).parent().css('width') != 'auto') {
		    	width = Math.ceil( 100 * parseFloat($(ctrl).css('width')) / parseFloat($(ctrl).parent().css('width')) ) + '%';
	    	} 
	    	
	    	var id = 'fmFinp-' + $(objForm).attr('id') + '-' + (i + 1);
	    	if($(ctrl).attr('id')) { 
	    		id = 'fmFinp-' + $(ctrl).attr('id'); 
	    	}
	    	
	    	$(ctrl).before('<a tabindex="' + $(ctrl).attr('tabindex') + '" class="fnFileInput" id="' + id + '"><span><cite>Not Selected ...</cite><strong>' + ($(ctrl).attr('title') != '' ? $(ctrl).attr('title') : 'Browse ...') + '</strong></span></a>');
	    	$(ctrl).attr('tabindex', 0);
	   		$(ctrl).addClass('fnFileHidden');
	    	
	    	var nCtrl = $('#' + id);
	    	
	   		$(ctrl).appendTo('#' + id + ' span strong');
	    	
	    	$(nCtrl).width(width);
	    	
	    	if($(ctrl).attr('disabled')) { $(nCtrl).addClass('disabled'); }
	    	    	
	    	/* Define Events  */
	    	$(ctrl).bind('change', function () { $(nCtrl).find('cite').text($(this).attr('value')); });
	    	
	    	$(nCtrl).bind('focus', function () { $(nCtrl).CtrlInFocus(); });
			$(nCtrl).bind('blur', function () { $(nCtrl).CtrlOutFocus(); });
	    });
	}
		
	/* 
	 * Service functions 
	 *
	 */

	$.fn.swapStyles = function (objSrc) { // Apply to newely created control old styles (such as margins)
		var styles = new Array('margin-left', 'margin-right', 'margin-top', 'margin-bottom');
		
		$(styles).each(function(idx, property) {
	    	$(this).css(property, $(objSrc).css( property ));
		});
		
		$(objSrc).addClass('fmZero');
	}
	
	$.fn.CtrlInFocus = function() { $(this).addClass('fmInFocus'); }		// Apply fmInFocus class when control get focus 
	$.fn.CtrlOutFocus = function() { $(this).removeClass('fmInFocus'); }	// Apply fmInFocus class when control lose focus
	
})(jQuery); // Call and execute the function immediately passing the jQuery object