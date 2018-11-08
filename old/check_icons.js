var http = require('http'), util = require('util'), url = require('url');

var appKey = '';

function printBadIconUrls(trucks) {
  trucks.forEach(function(truck) {
    var serverUrl = url.parse(truck["iconUrl"]);
    var req = http.request({ hostname : serverUrl.hostname, path : serverUrl.path}, function(res) {
      if (res.statusCode != 200) {
        console.log("http://www.chicagofoodtruckfinder.com/admin/trucks/"+truck["id"]+"/configuration");
      }
    });
    req.on("error", function(e) {

    });
    req.end();
  });
}


var req = http.request({hostname: "www.chicagofoodtruckfinder.com", path:"/services/trucks?appKey="+appKey}, function(res) {
  var responseData = '';
  res.on('data', function(chunk) {
    responseData += chunk;
  });
  res.on('end', function() {
    printBadIconUrls(JSON.parse(responseData));
  });
});
req.on("error", function(e) {
  console.log("Problem contacting food truck finder: " + e.message);
});
req.end();
