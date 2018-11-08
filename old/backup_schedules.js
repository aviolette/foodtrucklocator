var http = require('http'), util = require('util'), url = require('url'), fs = require('fs'), sleep=require('sleep');

var appKey = 'KbCD7Qb9',
    from=1312174800000, 
//    from=1397970000000,
    DAY_IN_MILLIS=86400000, 
    now=1398315600001;

getDate(from);

function getDate(date) {
  if (date >= now) {
    return;
  }
  var req = http.request({hostname: "www.chicagofoodtruckfinder.com", path:"/services/daily_schedule?appKey="+appKey+"&from="+date}, function(res) {
    var responseData = '';
    res.on('data', function(chunk) {
      responseData += chunk;
    });
    res.on('end', function() {
      try {
        var json = JSON.parse(responseData), fileName = "schedules/" + json['date'] + ".json";
        fs.writeFile(fileName, responseData, function(err) {
          if(err) {
            console.log(err);
          } else {
            console.log(fileName + " complete");
          }
        }); 
      } catch (e) {
        console.log(e);
      }
      setTimeout(function() {
         getDate(date + DAY_IN_MILLIS);
      }, 200);
    });
  });
  req.on("error", function(e) {
    console.log("Problem contacting food truck finder: " + e.message);
  });
  req.end();
}
