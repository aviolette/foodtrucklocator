</div>
<%-- brighttag script --%>
<c:if test="${!empty(signalId)}">
    <script type="text/javascript">
        (function () {
            var tagjs = document.createElement("script");
            var s = document.getElementsByTagName("script")[0];
            tagjs.async = true;
            tagjs.src = "//s.btstatic.com/tag.js#site=${signalId}";
            s.parentNode.insertBefore(tagjs, s);
        }());
    </script>
    <noscript>
        <iframe src="//s.thebrighttag.com/iframe?c=${signalId}" width="1" height="1" frameborder="0" scrolling="no"
                marginheight="0" marginwidth="0"></iframe>
    </noscript>

</c:if>
</body>
</html>
