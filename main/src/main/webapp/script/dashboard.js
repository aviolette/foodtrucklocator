function bindAjaxCallToButton(button, url) {
  var link = $("#" + button);
  link.click(function (evt) {
    evt.preventDefault();
    link.addClass("disabled");
    $.ajax({
      context: document.body,
      url: url,
      complete: function () {
        link.removeClass("disabled");
      },
      success: function () {
        window.location.reload();
      }
    });
  });
}
