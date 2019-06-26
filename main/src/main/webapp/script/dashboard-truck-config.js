(function() {
  $("#submitButton").click(function(e) {
    $("#theForm").submit();
  });

  $(document).ready(function () {
    setupImageUpload(function(imgUrl) {
      $("#previewIcon").attr("value", imgUrl);
    });
  });
})();