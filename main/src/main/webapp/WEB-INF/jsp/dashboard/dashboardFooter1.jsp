</div>
</div>

<!-- Include jQuery (required) and the JS -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
<script src="/theme-dashboard/toolkit.min.js"></script>
<script src="/theme-dashboard/assets/js/tablesorter.min.js"></script>
<script src="/script/dashboard.js"></script>

<c:forEach var="scriptItem" items="${extraScripts}">
  <script src="${scriptItem}"></script>
</c:forEach>

</body>
</html>
