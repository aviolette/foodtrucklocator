<%@include file="dashboardHeaderBS3.jsp" %>

<div class="row">
  <div class="col-md-12">

    <div class="form-group">
      <label for="intent">Intent</label>
      <select id="intent" class="form-control">
      </select>
    </div>

    <div id="slots">
    </div>

    <button id="sendButton" class="btn btn-primary">Send</button>
  </div>
</div>

<div class="row" style="margin-top:30px">
  <div class="col-md-12">
    <table class="table">
      <head>
        <th>Response</th>
        <th>Reprompt</th>
        <th style="min-width:250px">Card</th>
      </head>
      <tbody id="response">

      </tbody>
    </table>
  </div>
</div>

<script type="text/javascript">
  var intents = ${intents}, $intent = $('#intent'), $slots = $('#slots'), selected = false, selectedIntent, $reprompt = $("#reprompt");

  function buildSlots(name) {
    selectedIntent = name;
    $slots.empty();
    $.each(intents[name], function (i, val) {
      $slots.append("<div class='form-group'><label for='" + val + "'>" + val + "</label><input class='form-control slot' name='" + val + "' type='text' id='" + val + "'/></div>");
    });
  }

  $.each(intents, function (intentName, intent) {
    $intent.append("<option>" + intentName + "</option>")
    if (!selected) {
      buildSlots(intentName);
      selected = true;
    }
  });

  $intent.change(function (e) {
    buildSlots($intent.val());
  });

  function cleanup(s) {
    s = s.replace(new RegExp('<', 'g'), '&lt;');
    s = s.replace(new RegExp('>', 'g'), '&gt;</strong>');
    return s.replace(new RegExp('\&lt\;', 'g'), '<strong>&lt;');
  }

  function extractOutputSpeech(outputSpeech) {
    if (outputSpeech["type"] == "SSML") {
      return cleanup(outputSpeech["ssml"]);
    } else {
      return outputSpeech["text"];
    }
  }

  $('#sendButton').click(function (item) {
    var slots = [];
    $(".slot").each(function (i, item) {
      slots.push({name: $(item).attr("name"), value: $(item).val()});
    });
    var payload = {intent: selectedIntent, slots: slots}
    $.ajax({
      url: "/admin/alexa_test",
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload),
      complete: function () {
      },
      success: function (data) {
        var $table = $("#response");
        $table.empty();

        var response = data["response"];
        var outputSpeech = response["outputSpeech"], outputSpeechVal, repromptVal = "none", cardVal;
        if (outputSpeech) {
          outputSpeechVal = extractOutputSpeech(outputSpeech);
        }
        if (response["reprompt"]) {
          repromptVal = extractOutputSpeech(response["reprompt"]["outputSpeech"]);
        }
        $table.append("<tr><td>" + outputSpeechVal + "</td><td>" + repromptVal + "</td><td id='card'></td></tr>");

        if (response["card"]) {
          var cardContent = "";
          if (response["card"]["type"] == "Simple") {
            cardContent = response["card"]["content"].replace(/\n/g, "<br/>");
          } else {
            var imagePart = response["card"]["image"]["smallImageUrl"];
            if (!imagePart) {
              imagePart = response["card"]["image"]["largeImageUrl"];
            }
            if (imagePart) {
              imagePart = "<img src='" + imagePart + "'/>";
            }
            cardContent = imagePart + "<div>" + response["card"]["text"].replace(/\n/g, "<br/>") + "</div>";
          }
          $("#card").append('<div class="panel panel-default"><div class="panel-heading">' + response["card"]["title"] + '</div><div class="panel-body">' + cardContent + '</div></div>')
        }
      }
    });
  });

</script>

<%@include file="dashboardFooterBS3.jsp" %>