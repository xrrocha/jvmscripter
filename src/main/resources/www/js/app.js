'use strict';

angular.module('jvmscripter', ['ngRoute', 'ui.ace', 'jvmscripterFilters', 'jvmscripterServices']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
      when('/', {templateUrl: 'partials/create-script.html', controller: CreateScriptCtrl}).
      when('/edit', {templateUrl: 'partials/edit-script.html', controller: EditScriptCtrl}).
      otherwise({redirectTo: '/'});
  }]);
