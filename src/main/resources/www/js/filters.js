'use strict';

angular.module('jvmscripterFilters', []).filter('languageDescription', function() {
  return function(language) {
	  var description = language.name;
	  if (language.name != language.syntax) {
		  description += " (" + language.syntax + ")";
	  }
	  return description;
  };
});
