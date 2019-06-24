(function() {
  $("#submitButton").click(function(e) {
    $("#theForm").submit();
  });

  var truckId = JSON.parse($("#truck").text())["id"];

  $("#remove-image-button").click(function () {
    var $imageUrl = $("#imagePreview");
    $imageUrl.attr("src", "");
    $imageUrl.addClass("d-none");
    $("#remove-image-button-section").addClass("d-none");
    $("#upload").removeClass("d-none");
  });

  $(document).ready(function () {
    Dropzone.options.upload = {
      headers: {"X-Dropzone-Key": truckId, "X-Dropzone-Type": "truck" },
      maxFiles: 1,
      dictDefaultMessage: "Click or drop images here to upload",
      acceptedFiles: "image/*",
      complete: function () {
        this.removeAllFiles();
      },
      success: function (foo, response) {
        var $imageUrl = $("#imagePreview");
        $imageUrl.attr("src", response);
        $imageUrl.removeClass("d-none");
        $("#previewIcon").attr("value", response);
        $("#remove-image-button-section").removeClass("d-none");
        $("#upload").addClass("d-none");
      }
    };
  });
})();