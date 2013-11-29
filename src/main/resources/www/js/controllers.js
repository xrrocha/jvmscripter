function CreateScriptCtrl($scope, $location, $http, options) {
	$scope.languages = [];
	$scope.scripters = [];
	$scope.language = {};
	$scope.options = options;
    
    $http.get("api/languages", { params: { } })
    	.success(
	        function(languages) {
	            $scope.languages = languages;
	        	$scope.language = languages[0];
        });
    
    $http.get("api/scripters", { params: { } }).success(
        function(scripters) {
            $scope.scripters = scripters;
        });
	
    $scope.editScript = function(scripter) {
    	options.scripter = scripter;
    	for (var i = 0; i < $scope.languages.length; i++)
    		if ($scope.languages[i].name == scripter.languageName) {
    			options.language = $scope.languages[i];
    			break;
    		}
    	$location.path("/edit");
    }
	
    $scope.createScript = function() {
        var scriptUrl = "api/scripters"; 
        var data = $scope.language.name;
        var config = { headers: { "Content-Type": "text/plain" } };
    	$http.put(scriptUrl, data, config)
    		.success(function(scripter) {
    			$scope.editScript(scripter);
    		})
	    	.error(function(response) {
	            console.log("Error PUT /" + scriptUrl + ": " + response);
	        });
    }
}

function EditScriptCtrl($scope, $location, $http, options) {
	$scope.scripter = options.scripter;
	$scope.language = options.language;
	$scope.scriptText = "// Enter some " + $scope.language.name + " code\n";
	$scope.scriptResult = { result: "", output: "", error: { lineNumber: 0, columnNumber: 0, message: "" }};

	$scope.executeScript = function() {
        var scriptUrl = "api/scripters/" + $scope.scripter.id; 
        var data = $scope.scriptText;
        var config = { headers: { "Content-Type": "text/plain" } };
    	$http.post(scriptUrl, data, config)
    		.success(function(scriptResult) {
    			$scope.scriptResult = scriptResult;
    			if (scriptResult.error) {
    				$scope.editor.gotoLine(scriptResult.error.line, scriptResult.error.column, true)
    				$scope.editor.focus();
    			}
    		});
    }

	$scope.deleteScript = function() {
        var scriptUrl = "api/scripters/" + $scope.scripter.id;
    	$http.delete(scriptUrl)
    		.success(function(scripter) {
    	    	$location.path("/");
    		}).error(function(response) {
	            console.log("Error DELETE /" + scriptUrl + ": " + response);
	        });
    }
	
	$scope.aceLoaded = function(editor) {
		editor.navigateFileEnd();
		$scope.editor = editor;
	}
}