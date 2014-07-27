<%@ include file="../header.jsp" %>


<h2>Save the Food Trucks at 600 West Chicago Avenue!</h2>

<div class="row">

  <div class="col-md-9">
    <div class="alert alert-danger hidden">
      <span id="flash"></span>
    </div>

    <p>Tell 27th ward Alderman Walter Burnett Jr. not to kill small business by removing Chicago's most profitable food truck stand!

    <p>Chicago Food trucks have been selling delicious street food successfully in front of the 600 w. Building
    (829 n. Larabee) for breakfast, lunch, and dinner over the past 5 years. During that period many have built
    a solid base of regular customers who also work/live in the area and love frequenting the food trucks on a daily basis.
    At its peak more than 6 food trucks would regularly park in front of the building during a weekday lunch,
    all with long lines of happy customers.</p>

    <p>The successful balance of consumers demanding delicious food, and food trucks willing to supply it, was quashed
      this July 2014 thanks to 27th ward Alderman Walter Burnett Jr. This was because two brick and mortar restaurants
      inside the 600 w. Chicago building and their respective ownership began complaining to the Alderman about food trucks
      taking away their lunch business. His solution was to permanently remove the stand, without discussion or warning
      to the dozens of food trucks that rely on that stand to make a living.

    <p>So why not park there without a designated food truck stand you might ask? That’s because Chicago’s food trucks are subject
      to a 200 foot rule where no truck can operate within 200 feet of any brick and mortar business that
      could be construed as competition or else they will be issued a $2,000 ticket. Because of this rule and
      the crippling fine, food trucks by and large are very limited in where they can operate legally within
      downtown Chicago. Without the designated stand granting permission the whole area surrounding the
      600 w. Chicago building will become off limits to food trucks. And while the stand in front of the 600
      west Chicago building was arguably the most popular and profitable stand in all of Chicago, it will no
      longer exist. With new food trucks coming into the market, and space at the existing stands already
      limited, in order for Chicago’s food truck community to continue to grow we need to add more food
      trucks stands in profitable locations, not remove one of the few ones that we have. If not it is almost
      certain that several current food trucks within the community will begin to fail and go under, and more
      trucks will soon follow. Keep Chicago’s food truck community growing by signing this petition asking for
      the reinstatement of the 600 w. Chicago (829 n. Larabee) food truck stand.</p>

    <p>Thank you!</p>
  </div>
  <div class="col-md-3">

    <div class="panel panel-default">
      <div class="panel-body">
        ${numSignatures} <c:choose><c:when test="${numSignatures == 1}">person has</c:when><c:otherwise>people have</c:otherwise></c:choose> signed this petition
      </div>
    </div>
    <div>
      <h3>Sign here!</h3>
      <form method="POST"  role="form">
        <div class="form-group">
          <label for="name">Name</label>
          <input type="text" placeholder="First-name Last-name" id="name" name="name" class="form-control"/>
          <p class="help-block hidden" id="nameHelp">Name is required</p>

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
          name = $("#name").val();
      errors = 0;
      var at = email.indexOf('@');
      assertCondition(email.length > 3 && at > 1 && at < (email.length - 1), "email");
      assertCondition(name.length > 0, "name");
      assertCondition(/^\d{5}(-\d{4})?$/.exec(zipcode) != null, "zipcode");
      if (errors == 0) {
        $("form").submit();
      }
    });

    })();
</script>

<%@ include file="../footer.jsp" %>
