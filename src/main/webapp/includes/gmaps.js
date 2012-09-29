function TdarGMap(mapElement, allowRegionSelection) {
	this.map = new GMap2(mapElement);
	this.map.addMapType(G_PHYSICAL_MAP);
	this.map.addControl(new GLargeMapControl());
	this.map.addControl(new GOverviewMapControl());
	this.map.addControl(new GMapTypeControl());
	this.map.addControl(new GScaleControl());
    
    if(allowRegionSelection) { 
	    gzControl = new GZoomControl(
	        { 
	        	nOpacity:.2, 
	        	sBorder:"2px solid red" 
	        },
	        {
				sButtonHTML:"<img src='" + getBaseURI() + "images/select-region.png' />",
				sButtonZoomingHTML:"<img src='"+ getBaseURI() +"images/select-region.png' />",
				oButtonStartingStyle:{width:'24px',height:'24px'}
			},
			{ 
				buttonClick:function(){}, 
				dragStart:function(){}, 
				dragging:function(x1,y1,x2,y2){}, 
				dragEnd:function(nw,ne,se,sw,nwpx,nepx,sepx,swpx) { 
					$("#minx").val(sw.lng());
					$("#miny").val(sw.lat());
					$("#maxx").val(ne.lng());
					$("#maxy").val(ne.lat());
					boundBox = true;
				}
			}
		);
	   this.map.addControl(gzControl, new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(10,120)));
   }
   this.centerToUs();
   this.map.enableDoubleClickZoom();
}

TdarGMap.prototype.centerToUs = function() {
	this.map.setCenter(new GLatLng(40, -97.00), 4, G_NORMAL_MAP);
};

TdarGMap.prototype.drawBoundingBox = function(){
	
	var G = GZoomControl.G;
    if (G.oZoomArea != null) G.oMap.removeOverlay(G.oZoomArea);
	
    var x1 = parseFloat($.trim($("#minx").val()));
    var y1 = parseFloat($.trim($("#miny").val()));
    var x2 = parseFloat($.trim($("#maxx").val()));
    var y2 = parseFloat($.trim($("#maxy").val()));

    if(isNaN(x1)||isNaN(y1)||isNaN(x2)||isNaN(y2)){
        this.centerToUs();
        return;
    }
    
    var colour = "#00OOFF";
    var width  = 2;
    var pts = [];
    pts[0] = new GLatLng(y1, x1);
    pts[1] = new GLatLng(y1, x2);
    pts[2] = new GLatLng(y2, x2);
    pts[3] = new GLatLng(y2, x1);
    pts[4] = new GLatLng(y1, x1);		

    var style = G.style;		
    G.oZoomArea = new GPolyline(pts, G.style.sOutlineColor, G.style.nOutlineWidth+1, .4);

    var bounds = new GLatLngBounds();
    bounds.extend(pts[0]);
    bounds.extend(pts[1]);
    bounds.extend(pts[2]);
    bounds.extend(pts[3]); 
    this.map.setZoom(this.map.getBoundsZoomLevel(bounds));

    this.map.panTo(new GLatLng((y1+y2)/2, (x1+x2)/2));
    this.map.addOverlay(G.oZoomArea);
};
