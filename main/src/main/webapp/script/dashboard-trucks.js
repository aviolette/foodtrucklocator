function setupTruckListToggles() {
  function withStops() {
    $(".inactive-stops").addClass("hidden");
    $(".inactive-truck").addClass("hidden");
    $(".active-stop").removeClass("hidden");
  }

  function withoutStops() {
    $(".inactive-stops").removeClass("hidden");
    $(".inactive-truck").addClass("hidden");
    $(".active-stop").addClass("hidden");
  }

  function inactiveTrucks() {
    $(".inactive-stops").addClass("hidden");
    $(".inactive-truck").removeClass("hidden");
    $(".active-stop").addClass("hidden");
  }

  $("#with-stops").click(withStops);
  $("#without-stops").click(withoutStops);
  $("#inactive-stops").click(inactiveTrucks);

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