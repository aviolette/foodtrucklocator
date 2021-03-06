function flash(message, type) {
  type = type || "alert-danger";
  var $flash = $("#flash");
  if (!message) {
    $flash.addClass("hidden");
    return;
  }
  $flash.empty();
  $flash.append("<p>" + message + "</p>");
  $flash.removeClass("hidden");
  $flash.addClass(type);
  $flash.css("display", "block");
}

function dissolveFlash() {
  setTimeout(function () {
    $("#flash").css("display", "none");
  }, 5000);
}

(function () {
  function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
  }

  function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }

  var flashMsg = getCookie("flash");
  deleteCookie("flash");
  flash(flashMsg);
})();
