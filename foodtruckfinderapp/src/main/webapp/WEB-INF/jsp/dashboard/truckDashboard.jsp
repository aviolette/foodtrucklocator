<%@ include file="../common.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Bootstrap, from Twitter</title>
  <meta name="description" content="">
  <meta name="author" content="">

  <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
  <!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
  <![endif]-->

  <!-- Le styles -->
  <link href="/bootstrap/bootstrap.css" rel="stylesheet">
  <style type="text/css">
      /* Override some defaults */
    html, body {
      background-color: #eee;
    }

    body {
      padding-top: 40px; /* 40px to make the container go all the way to the bottom of the topbar */
    }

    .container > footer p {
      text-align: center; /* center align it with the container */
    }

    .container {
      width: 820px; /* downsize our container to make the content feel a bit tighter and more cohesive. NOTE: this removes two full columns from the grid, meaning you only go to 14 columns and not 16. */
    }

      /* The white background content wrapper */
    .container > .content {
      background-color: #fff;
      padding: 20px;
      margin: 0 -20px; /* negative indent the amount of the padding to maintain the grid system */
      -webkit-border-radius: 0 0 6px 6px;
      -moz-border-radius: 0 0 6px 6px;
      border-radius: 0 0 6px 6px;
      -webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
      -moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
      box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
    }

      /* Page header tweaks */
    .page-header {
      background-color: #f5f5f5;
      padding: 20px 20px 10px;
      margin: -20px -20px 20px;
    }

      /* Give a quick and non-cross-browser friendly divider */
    .content .span4 {
      margin-left: 0;
      padding-left: 19px;
      border-left: 1px solid #eee;
    }

    .topbar .btn {
      border: 0;
    }

  </style>

  <!-- Le fav and touch icons -->
  <link rel="shortcut icon" href="/favicon.ico">
</head>

<body>

<div class="topbar">
  <div class="fill">
    <div class="container">
      <a class="brand" href="#">Chicago Food Truck Finder</a>
      <ul class="nav">
        <li class="active"><a href="#">Home</a></li>
        <li><a href="#about">About</a></li>
        <li><a href="#contact">Contact</a></li>
      </ul>
    </div>
  </div>
</div>

<div class="container">

  <div class="content">
    <div class="page-header">
      <h1>Dashboard
        <small>Supporting text or tagline</small>
      </h1>
    </div>
    <div class="row">
      <div class="span14">
        <h2>Tweets</h2>
        <table>
          <thead>
            <tr>
              <td>&nbsp;</td>
              <td>Time</td>
              <td>Text</td>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="tweet" items="${tweets}">
              <tr>
                <td><input type="button" class="ignoreButton btn primary" id="${tweet.id}" value="<c:choose><c:when test="${tweet.ignoreInTwittalyzer}">Unignore</c:when><c:otherwise>Ignore</c:otherwise></c:choose>"/>
                  </td>
                <td>${tweet.time}</td>
                <td>${tweet.text}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <footer>
    <p></p>
  </footer>

  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.js"></script>
  <script type="text/javascript">
    $(".ignoreButton").click(function(evt) {
      var id = $(this).attr("id");
      var value = $(this).attr("value");
      var ignore = value == "Ignore";
      var button = $(this);
      $.ajax({
        context: document.body,
        data: JSON.stringify({id: id, ignore: ignore}),
        contentType: 'application/json',
        dataType: 'json',
        type: 'POST',
        success: function() {
          button.attr("value", ignore ? "Unignore" : "Ignore")
        }
      });
    });
  </script>

</div>
<!-- /container -->

</body>
</html>
