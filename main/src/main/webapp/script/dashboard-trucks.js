function setupTruckListToggles() {
  function withStops() {
    $(".inactive-stops").addClass("d-none");
    $(".inactive-truck").addClass("d-none");
    $(".active-stop").removeClass("d-none");
  }

  function withoutStops() {
    $(".inactive-stops").removeClass("d-none");
    $(".inactive-truck").addClass("d-none");
    $(".active-stop").addClass("d-none");
  }

  function inactiveTrucks() {
    $(".inactive-stops").addClass("d-none");
    $(".inactive-truck").removeClass("d-none");
    $(".active-stop").addClass("d-none");
  }

  $("#with-stops").parent().click(withStops);
  $("#without-stops").parent().click(withoutStops);
  $("#inactive-stops").parent().click(inactiveTrucks);

  withStops();
}

function newTruckDialog() {
  var truckId = prompt("Enter truck ID:");
  if (!truckId) {
    return;
  }
  $.ajax({
    url : "/services/trucks",
    type: "POST",
    contentType: "application/json",
    data : JSON.stringify({id : truckId, name : "UNNAMED-" + truckId, twitterHandle: truckId}),
    success : function() {
      location.href = "/admin/trucks/" + truckId.toLowerCase()
    }
  });
}

$("#new-truck").click(function(e) {
  e.preventDefault();
  newTruckDialog();
});


setupTruckListToggles();