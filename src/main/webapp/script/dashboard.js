function flash(message, type) {
  var $flash = $("#flash");
  $flash.empty();
  $flash.append("<p>" + message + "</p>");
  $flash.addClass(type);
  $flash.css("display", "block");
}

function dissolveFlash() {
  setTimeout(function() {
    $("#flash").css("display", "none");
  }, 5000);
}