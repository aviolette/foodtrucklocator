</div>
</div>
<script type="text/javascript" src="/script/flash.js"></script>

<script>
  function headerFlash(msg) {
    var $flash = $("#flash");
    $flash.empty();
    $flash.append(msg);
    $flash.removeClass("hidden");
  }
  (function () {
    function deleteCookie(name) {
      document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
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
    deleteCookie("flash");
    if (flash) {
      headerFlash(msg);
    }
  })();
</script>
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-24765719-1', 'auto');
  ga('send', 'pageview');

</script>
</body>
</html>
