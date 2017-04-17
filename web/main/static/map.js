var HOST_URL = 'http://paradin-me.appspot.com'
var TIMEOUT_DURATION = 30000;
var map;
var routePath;
var snappedCoordinates;

function initMap() {
  $.get(HOST_URL + '/route/api/v1.0/anchor',
    {
      'limit': 100
    }, function(data) {
      var initCenter = {lat: 23.318063, lng: 120.267220};
      if (data.length > 0) {
        initCenter['lat'] = data[0]['latitude'];
        initCenter['lng'] = data[0]['longitude'];
      }
      map = new google.maps.Map(document.getElementById('map'), {
        center: initCenter,
        zoom: 16
      });
      routePath = new google.maps.Polyline({
        strokeColor: 'pink',
        strokeOpacity: 0.6,
        strokeWeight: 6
      });
      routePath.setMap(map);
      if (data.length > 0) {
        snapAndDraw(data);
      } else {
        setTimeout(redrawRoute, TIMEOUT_DURATION);
      }
  });
}

// Snap a user-created polyline to roads and draw the snapped path
function snapAndDraw(path) {
  var pathValues = [];
  for (var i = 0; i < path.length; ++i) {
    pathValues.push(path[i]['latitude'] + ',' + path[i]['longitude']);
  }

  $.get('https://roads.googleapis.com/v1/snapToRoads', {
    interpolate: true,
    key: 'AIzaSyApwYBz8k3oOqw70nvoFgiBmTfqNvFbE2U',
    path: pathValues.join('|')
  }, function(data) {
    processSnapToRoadResponse(data);
    drawSnappedPolyline();
    setTimeout(redrawRoute, TIMEOUT_DURATION);
  });
}

// Store snapped polyline returned by the snap-to-road service.
function processSnapToRoadResponse(data) {
  snappedCoordinates = [];
  for (var i = 0; i < data.snappedPoints.length; i++) {
    var latlng = new google.maps.LatLng(
      data.snappedPoints[i].location.latitude,
      data.snappedPoints[i].location.longitude);
    snappedCoordinates.push(latlng);
  }
}

// Draws the snapped polyline (after processing snap-to-road response).
function drawSnappedPolyline() {
  routePath.setPath(snappedCoordinates);
}

function redrawRoute() {
  $.get(HOST_URL + '/route/api/v1.0/anchor',
    {
      'limit': 100
    }, function(data) {
      if (data.length > 0) {
        snapAndDraw(data);
      } else {
        setTimeout(redrawRoute, TIMEOUT_DURATION)
      }
  });
}

