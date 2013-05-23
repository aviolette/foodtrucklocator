function flash(message, type) {
  var $flash = $("#flash");
  $flash.empty();
  $flash.append("<p>" + message + "</p>");
  $flash.addClass(type);
  $flash.css("display", "block");
}

function dissolveFlash() {
  setTimeout(function () {
    $("#flash").css("display", "none");
  }, 5000);
}

function generateTimes() {
  var times = [];
  for (var a = 0; a < 2; a++) {
    for (var i = 1; i < 13; i++) {
      for (var j = 0; j < 60; j = j + 15) {
        var t = j;
        if (t == 0) {
          t = "00";
        }
        var ampm = (a == 0) ? "AM" : "PM";
        times.push(i + ":" + t + " " + ampm);
      }
    }
  }
  return times;
}
