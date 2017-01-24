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
<%-- brighttag script --%>
<script src="//s.btstatic.com/tag.js">{
  site: "zIOrUTR"
}</script>
<noscript>
  <iframe src="//s.thebrighttag.com/iframe?c=zIOrUTR" width="1" height="1" frameborder="0"
          scrolling="no" marginheight="0" marginwidth="0"></iframe>
</noscript>
</body>
</html>
