function setupImageUpload(callback) {

  var uploadData = JSON.parse($("#upload_data").text());
  const iconDiv = "imagePreview";

  var $img = $("img");
  $img.attr("width", "100%");
  var $div = $('<div class="mb-2 mt-2" id="remove-image-button-section">' +
      '<button id="remove-image-button" class="btn-outline-danger btn">Remove</button> ' +
      '</div>');
  $img.attr("id", iconDiv);
  const $form = $("<form action=\"/admin/images\" class=\"dropzone\" id=\"upload\"></form>");
  if (uploadData["initialImage"]) {
    $img.attr("src", uploadData["initialImage"]);
    $form.addClass("d-none");
  } else {
    $img.addClass("d-none");
    $div.addClass("d-none");
  }
  const $uploadSection = $("#upload_section");
  $uploadSection.append($img);
  $uploadSection.append($div);
  $uploadSection.append($form);

  $img.height($uploadSection.width());

  $("#remove-image-button").click(function () {
    var $imageUrl = $("#" + iconDiv);
    $imageUrl.attr("src", "");
    $imageUrl.addClass("d-none");
    $("#remove-image-button-section").addClass("d-none");
    $("#upload").removeClass("d-none");
  });

  Dropzone.options.upload = {
    headers: {"X-Dropzone-Key": uploadData["key"], "X-Dropzone-Type": uploadData["artifactType"] },
    maxFiles: 1,
    dictDefaultMessage: "Click or drop images here to upload",
    acceptedFiles: "image/*",
    complete: function () {
      this.removeAllFiles();
    },
    success: function (foo, response) {
      var $imageUrl = $("#" + iconDiv);
      $imageUrl.attr("src", response);
      $imageUrl.removeClass("d-none");
      callback(response);
      $("#remove-image-button-section").removeClass("d-none");
      $("#upload").addClass("d-none");
    }
  };
}