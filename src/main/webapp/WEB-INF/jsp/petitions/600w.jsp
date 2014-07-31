<%@ include file="../header.jsp" %>

<div class="row">

  <div class="col-md-8">
    <div class="alert alert-danger hidden">
      <span id="flash"></span>
    </div>

    <h3>Alderman Walter Burnett Jr., Save Chicago Food Trucks by reinstating and expanding the 600 W Chicago Food Truck Stand.</h3>

    <p class="lead">Chicago Food trucks have been selling delicious street food successfully in
      front of the 600 W. Building (829 N. Larabee) for breakfast, lunch, and
      dinner over the past 5 years. During that period many have built a solid
      base of regular customers who also work/live in the area and love
      frequenting the food trucks on a daily basis.  We ask that you keep
      Chicago's food truck small business community growing by reinstating and
      expanding the 600 W. Chicago (829 N. Larrabee) food truck stand. </p>

    <p class="lead">Thank you!</p>
  </div>
  <div class="col-md-4">

    <div class="panel panel-default">
      <div class="panel-body">
        ${numSignatures} <c:choose><c:when test="${numSignatures == 1}">person has</c:when><c:otherwise>people have</c:otherwise></c:choose> signed this petition
      </div>
    </div>
    <div>
      <h3>Sign here!</h3>
      <form method="POST"  role="form">
        <div class="form-group">
          <label for="firstName">First Name</label>
          <input type="text" placeholder="" id="firstName" name="firstName" class="form-control"/>
          <p class="help-block hidden" id="firstNameHelp">First Name is required</p>
        </div>
        <div class="form-group">
          <label for="lastName">Last Name</label>
          <input type="text" placeholder="" id="lastName" name="lastName" class="form-control"/>
          <p class="help-block hidden" id="lastNameHelp">Last Name is required</p>
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" placeholder="you@foobar.com" name="email" id="email" class="form-control"/>
          <p class="help-block hidden" id="emailHelp">Email is required</p>

        </div>
        <div class="form-group">
          <label for="zipcode">Zipcode</label>
          <input type="zipcode" placeholder="XXXXX" name="zipcode" id="zipcode" class="form-control"/>
          <p class="help-block hidden" id="zipcodeHelp">zip code is required</p>

        </div>
        <div class="checkbox">
          <label>
            <input type="checkbox" name="inWard">
            Check this if you are a resident of Ward 27
          </label>
        </div>
        <button title="Your finger is on the button. Push the button" type="submit" id="submitButton" class="btn btn-primary">Submit</button>
      </form>
    </div>

    <div>
      <h3>A statement about your privacy</h3>
      <p>The information you are providing here will <strong>only be used for sigining this petition</strong>.  I will never provide your information to a third-party or use them for any other use than described here.</p>
    </div>

  </div>
</div>

<%@include file="../include/core_js.jsp"%>

<script type="text/javascript">
  (function() {
    function setCookie(name, value, days) {
      var expires;
      if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
      }
      else expires = "";
      document.cookie = name + "=" + value + expires + "; path=/";
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

    var flash = getCookie("flash");
    if (flash) {
      $(".alert").removeClass("hidden");
      $("#flash").html(flash);
      setCookie("flash", null, -1);
    }

    var errors = 0;

    function assertCondition(condition, controlName) {
      if (!condition) {
        $("#" + controlName + "Help").removeClass("hidden");
        $("#" + controlName).parent().addClass("has-error")
        errors = errors + 1;
      } else {
        $("#" + controlName + "Help").addClass("hidden");
        $("#" + controlName).parent().removeClass("has-error");
      }
    }

    $("#submitButton").click(function(e) {
      e.preventDefault();
      var email = $("#email").val(),
          zipcode = $("#zipcode").val(),
          firstName = $("#firstName").val(),
          lastName = $("#lastName").val();
      errors = 0;
      var at = email.indexOf('@');
      assertCondition(email.length > 3 && at > 1 && at < (email.length - 1), "email");
      assertCondition(firstName.length > 0, "firstName");
      assertCondition(lastName.length > 0, "lastName");
      assertCondition(/^\d{5}(-\d{4})?$/.exec(zipcode) != null, "zipcode");
      if (errors == 0) {
        $("form").submit();
      }
    });

    })();
</script>

<%@ include file="../footer.jsp" %>
