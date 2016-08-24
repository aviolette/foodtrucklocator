function locationMatching(locations, field) {
  // Type-ahead related stuff
  var substringMatcher = function (strs) {
    return function (q, cb) {
      var matches, substrRegex;
      matches = [];
      substrRegex = new RegExp(q, 'i');
      $.each(strs, function (i, str) {
        if (substrRegex.test(str)) {
          matches.push({value: str});
        }
      });
      cb(matches);
    };
  };

  $("#" + field).typeahead({
    hint: true,
    highlight: true,
    minLength: 1
  }, {name: 'locations', displayKey: 'value', source: substringMatcher(locations)});
}
