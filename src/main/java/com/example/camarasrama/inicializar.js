<html><body>
<div id="mapdiv"></div>
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script>
    map = new OpenLayers.Map("mapdiv",
    {zoomDuration: 1,controls: [],restrictedExtent : [-6530420.0516811,-4118322.9349418,-6513551.952168,-4100050.6036164]});
    map.addControl(new OpenLayers.Control.ArgParser());
    map.addControl(new OpenLayers.Control.Navigation());
    var i, l, c = map.getControlsBy( "zoomWheelEnabled", true );
    for ( i = 0, l = c.length; i < l; i++ ) {
    c[i].disableZoomWheel();
}

    map.addLayer(new OpenLayers.Layer.OSM());
    var lonLat = new OpenLayers.LonLat( -58.5908 ,-34.5901 )
    .transform(
    new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
    map.getProjectionObject() // to Spherical Mercator Projection
    );
    var zoom=16;
    var markers = new OpenLayers.Layer.Markers( "Markers" );
    map.addLayer(markers);
    map.setCenter(lonLat, zoom);
</script>
</body></html>