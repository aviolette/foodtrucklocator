function flash(message, type) {
  var $flash = $("#flash");
  $flash.empty();
  $flash.append("<p>" + message + "</p>");
  $flash.removeClass("alert-error");
  $flash.addClass(type);
  $flash.css("display", "block");
}

function dissolveFlash() {
  setTimeout(function () {
    $("#flash").css("display", "none");
  }, 5000);
}

function generateTimes() {
  return ["7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
    "11:30 AM", "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM",
    "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM",
    "8:30 PM", "9:00 PM", "9:30 PM", "10:00 PM", "10:30 PM", "11:00 PM"]
}

function bindAjaxCallToButton(button, url) {
  var link = $("#" + button);
  link.click(function(evt) {
    evt.preventDefault();
    link.addClass("disabled");
    $.ajax({
      context: document.body,
      url: url,
      complete : function() {
        link.removeClass("disabled");
      },
      success: function() {
        window.location.reload();
      }
    });
  });
}
