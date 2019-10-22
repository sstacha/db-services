/**
 * Created by sstacha on 12/23/13.
 */
'use strict';


var app = angular.module("console", ['ngRoute']);
window.app = app;
var base_url = window.base_url;
var ctx = window.ctx;
console.log('base_url: ' + base_url);
console.log('ctx: ' + ctx);

function unescape_quote(input) {
    if (input!==undefined && input!==null)
        input = input.replace(/\\'/g, '\'');
    return input;
};

app.factory('ConfigurationService', function($http) {
	var urlBase = ctx + "/_system/configurations";
	return {
		items: function () {
			// $http returns a promise, which has a then function, which also returns a promise
			// Return the promise to the controller
			return $http.get(urlBase, {cache: false}).then(function (response) {
				// The then function here is an opportunity to modify the response
				console.log(response);
				return response.data;
			});
		},
		item: function (id) {
			return $http.get(urlBase, id);
		},
		insert: function (data) {
			return $http({
				url: urlBase,
				method: "POST",
				dataType: "json",
				data: $.param(data),
				headers: { "Content-Type": "application/x-www-form-urlencoded"}
			});
		},
		update: function (id, data) {
			if (id && id.length > 0) {
				data.push({name: "$action", value: "update"});
				return $http({
					url: urlBase,
					method: "POST",
					dataType: "json",
					data: $.param(data),
					headers: { "Content-Type": "text/html"}
				});
			}
			else
				return $http({
					url: urlBase,
					method: "POST",
					dataType: "json",
					data: $.param(data),
					headers: { "Content-Type": "application/x-www-form-urlencoded"}
				});
		},
		remove: function (id) {
			var data = 'path=' + id + '&$action=delete';
			return $http({
				url: urlBase,
				method: "POST",
				dataType: "json",
				data: data,
				headers: { "Content-Type": "text/html"}
			});
		}
	}
});

app.factory('ConnectionService', function($http) {
	var urlBase = ctx + "/_system/connections";
	return {
		items: function () {
			// $http returns a promise, which has a then function, which also returns a promise
			// Return the promise to the controller
			return $http.get(urlBase, {cache: false}).then(function (response) {
				// The then function here is an opportunity to modify the response
				console.log(response);
				return response.data;
			});
		},
		item: function (id) {
			return $http.get(urlBase, id);
		},
		insert: function (data) {
			return $http({
				url: urlBase,
				method: "POST",
				dataType: "json",
				data: $.param(data),
				headers: { "Content-Type": "application/x-www-form-urlencoded"}
			});
			//return $http.post(urlBase, data);
		},
		update: function (id, data) {
			if (id && id.length > 0) {
				data.push({name: "$action", value: "update"});
				return $http({
					url: urlBase,
					// method: "PUT",
					method: "POST",
					dataType: "json",
					data: $.param(data),
					headers: {"Content-Type": "text/html"}
				});
			}
				//return $http.put(urlBase, data);
			else
				return $http({
					url: urlBase,
					method: "POST",
					dataType: "json",
					data: $.param(data),
					headers: { "Content-Type": "application/x-www-form-urlencoded"}
				});

				//return $http.post(urlBase, data);
		},
		remove: function (id) {
			var data = 'name=' + id + '&$action=delete';
			return $http({
				url: urlBase,
				method: "POST",
				dataType: "json",
				data: data,
				headers: { "Content-Type": "text/html"}
			});

			//return $http.post(urlBase, data);
		},
		setRootLogLevel: function (llevel) {
			var data = 'log_level=' + llevel;
			if (llevel != null && llevel.length > 3) {
				return $http({
					url: ctx + "/_system/log/level/set",
					method: "POST",
					dataType: "json",
					data: data,
					headers: { "Content-Type": "text/html"}
				});
			}
		}
	};
});

app.factory('DataService', function($http) {
	var urlBase = ctx + "/_system/data";
	return {
		tables: function () {
			var tables = ["test", "test2", "todo"];
			return tables;
			// $http returns a promise, which has a then function, which also returns a promise
			// Return the promise to the controller
// 			return $http.get(urlBase, {cache: false}).then(function (response) {
// 				// The then function here is an opportunity to modify the response
// 				console.log(response);
// 				return response.data;
// 			});
		},
		headers: function (tableName) {
			var headers = ["id", "name"];
			return headers;
		},
		item: function (id) {
			return $http.get(urlBase + '/' + id);
		},
		insert: function (data) {
			return $http.post(urlBase, data);
		},
		update: function (id, data) {
			if (id && id.length > 0)
				return $http.put(urlBase, data);
			else
				return $http.post(urlBase, data);
		},
		remove: function (id) {
			return $http.post(urlBase + '/' + id);
		}
	};
});

app.config(function($routeProvider) {
	$routeProvider.when('/configurations', {
		controller: 'ConfigurationsController',
		templateUrl: 'configuration.html'
	}).when('/connections', {
		controller: 'ConnectionsController',
		templateUrl: 'connection.html'
	}).when('/tables', {
		controller: 'DataController',
		templateUrl: 'data.html'
	}).otherwise({redirectTo: '/configurations'});
});

app.controller("ConfigurationsController", function($scope, $http, ConfigurationService, ConnectionService) {
	$scope.ctx = ctx;
	$scope.selectedConfiguration = {};
	$scope.dbConfiguration = {};
	$scope.selectedConfiguration.connection_name="";
	$scope.connections = [];
	$scope.testActions = ['query', 'insert', 'update', 'delete'];
	$scope.testAction = 'query';
	$scope.usedActions = [];
	$scope.testResultsMsg = {};
	
	// async load the configurations from database
	ConfigurationService.items().then(function (data) {
		$scope.configurations = data;
		if ($scope.configurations)
			$scope.configurations.sort(sortByPath);
		console.log("Configurations: " + JSON.stringify($scope.configurations));
	}, function(response) {
		$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
	});
	// async load the connections from database
	ConnectionService.items().then(function (data) {
		$scope.connections = data;
		if ($scope.connections)
			$scope.connections.sort(sortByName);
		console.log("Connections: " + JSON.stringify($scope.connections));
	}, function(response) {
		$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
	});

	//// function to calculate the max z index currently used by controls.
	//// NOTE: only checks elements with
	//$scope.getMaxZIndex = function(className) {
	//	var index_highest = 0;
	//	// more effective to have a class for the div you want to search and
	//	// pass that to your selector
	//	$(("." + className)).each(function() {
	//		// always use a radix when using parseInt
	//		var index_current = parseInt($(this).css("zIndex"), 10);
	//		if(index_current > index_highest) {
	//			index_highest = index_current;
	//		}
	//	});
	//};

// function to set the zindex properly on the modal events
// 	$(document).on({
// 		'show.bs.modal': function () {
// 			var zIndex = 1041 + (10 * $('.modal:visible').length);
// 			$(this).css('z-index', zIndex);
// 			setTimeout(function() {
// 				$('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');
// 			}, 0);
// 		},
// 		'hidden.bs.modal': function() {
// 			if ($('.modal:visible').length > 0) {
// 				// restore the modal-open class to the body element, so that scrolling works
// 				// properly after de-stacking a modal.
// 				setTimeout(function() {
// 					$(document.body).addClass('modal-open');
// 				}, 0);
// 			}
// 		}
// 	}, '.modal');
	$scope.pathFilter = function(config) {
		var thisFilter = "";
		if ($scope.app.viewFilter[0])
			thisFilter = $scope.app.viewFilter[0].trim();
		if (thisFilter.length == 0)
			return true;
		if (!(config && config.path))
			return true;
		if (config.path.startsWith(thisFilter))
			return true;
			
		return false;
	};
	$scope.printFilterResults = function () {
		console.log('viewFilter: ' + $scope.app.viewFilter[0]);
		// loop through each configuration and apply the keywordFilter to see if it would match
		$scope.configurations.forEach(function (value, index, arr) {
			console.log(index + ': ' + $scope.keywordFilter(value));
		});
	};
	$scope.keywordFilter = function(config) {
		// by precedence if we have a ! it goes first; ie: web product:web !crm !pie
		//		translates to web and product:web and not crm and not pie
		// strip each word block by spaces
		// NOTE: if we have an empty filter we simply want to always include everything
		var thisFilter = "";
		if ($scope.app.tagFilter[0])
			thisFilter = $scope.app.tagFilter[0].trim();
		if (thisFilter.indexOf("_system") == -1) {
			if (thisFilter.length == 0) {
				$scope.app.tagFilter[0] = "!_system";
				thisFilter = "!_system";
			}
			else {
				$scope.app.tagFilter[0] = $scope.app.tagFilter[0] + " !_system";
				thisFilter = thisFilter + " !_system";
			}
		}
		var parts = thisFilter.split(' ');
		var keywords = "";
		var lvalue = true;
		var rvalue, op, i=1;
		
		if (config && config.keywords)
			keywords = config.keywords.replace(/[\s,]+/g, ',');
		var tags = keywords.split(',');
			
		// should be [value] [optional op] [value]...
		parts.forEach(function (value, index, arr) {
			// index is only 0 once at the beginning
			if (index == 0) {
				lvalue = $scope.evaluateCondition(value, tags);
			}
			else if (i==2) {
				// optional op or value2
				if (value == '&&' || value == '||')
					op = value;
				else {
					rvalue = $scope.evaluateCondition(value, tags);
					op = '&&'
					i=3;
				}
			} 
			if (i == 3) {
				if (rvalue == 'undefined' || rvalue == null)
					rvalue = $scope.evaluateCondition(value, tags);
				if (op == '&&')
					lvalue = (lvalue && rvalue);
				else
					lvalue = (lvalue || rvalue);
			}
			if (++i > 3) {
				i = 2;	
				rvalue = null;
			}	
		});
		return lvalue;
	};
	$scope.evaluateCondition = function(value, keywords) {
		var realValue = value;
		if (value.indexOf('!') == 0)
			realValue = realValue.substr(1);
		var found = $.inArray(realValue, keywords) > -1;
		if (value.indexOf('!') == 0) 
			return !found;
		else 
			return found;
	};
	$scope.cantModifyConfiguration = function() {
		// we can only modify a configuration that has been saved; any edits and we disable
		if ($scope.selectedConfiguration && $scope.dbConfiguration && $scope.selectedConfiguration.path && $scope.selectedConfiguration.$$hashKey)
			if ($scope.isModified($scope.selectedConfiguration) == false && $scope.selectedConfiguration.path.length > 1)
				return false;
		return true;
	};
	$scope.cantSaveConfiguration = function () {
		// in order to save we have to have a value for the connection, path > 1 and at least one sql not blank
		if (isObject($scope.selectedConfiguration) && isObject($scope.dbConfiguration) && ($scope.selectedConfiguration.path && $scope.selectedConfiguration.path.length > 1)) {
			if ($scope.selectedConfiguration.connection_name && $scope.selectedConfiguration.connection_name.length > 0)
					if( ($scope.selectedConfiguration.query_statement && $scope.selectedConfiguration.query_statement.length > 0) || 
						($scope.selectedConfiguration.update_statement && $scope.selectedConfiguration.update_statement.length > 0) || 
						($scope.selectedConfiguration.insert_statement && $scope.selectedConfiguration.insert_statement.length > 0) ||
						($scope.selectedConfiguration.delete_statement && $scope.selectedConfiguration.delete_statement.length > 0))
							return false;
		}
		return true;
	};
	
	$scope.saveConfiguration = function() {
		var form = $("#configuration-form");
		var form_array = form.serializeArray();
		// var form_data = form.serialize();
		// console.log ("data: " + form_data);

		// async save the configuration to the database
		ConfigurationService.update($scope.selectedConfiguration.$$hashKey, form_array).then(function (data) {
			prettyPrintConsole(data);
			$scope.msgbox.header = $scope.selectedConfiguration.path;
			$("#dlg-msg").html(prettyPrint(toProduction(data)));
			// reread the configuration data
			ConfigurationService.items().then(function (data) {
				$scope.configurations = data;
				if ($scope.configurations)
					$scope.configurations.sort(sortByPath);
				console.log("Reloaded Configurations: " + JSON.stringify($scope.configurations));
				$('#msg-box').modal('show');
			});
		}, function(response) {
			$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
		});
	};	
	$scope.deleteConfiguration = function() {				
		if ($scope.selectedConfiguration && $scope.selectedConfiguration.path && $scope.selectedConfiguration.path.length > 0 && $scope.selectedConfiguration.$$hashKey && $scope.selectedConfiguration.$$hashKey.length > 0) {
            ConfigurationService.remove($scope.selectedConfiguration.path).then(function(data) {
				prettyPrintConsole(data);
				$scope.msgbox.header = $scope.selectedConfiguration.path;
				$("#dlg_msg").html(prettyPrint(toProduction(data)));
				// reread the configuration data
				ConfigurationService.items().then(function (data) {
					$scope.configurations = data;
					if ($scope.configurations)
						$scope.configurations.sort(sortByPath);
					console.log("Reloaded Configurations after delete... ");
					$('#msg-box').modal('show');
					$('#configuration-dialog').modal('hide');
				});
					}, function(response) {
				$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
				alert(JSON.stringify(response, null, 2));
				$('#configuration-dialog').modal('hide');
			});			
		}
	};
	$scope.editConfiguration = function(data) {
        data.query_statement = unescape_quote(data.query_statement);
        data.insert_statement = unescape_quote(data.insert_statement);
        data.update_statement = unescape_quote(data.update_statement);
        data.delete_statement = unescape_quote(data.delete_statement);

		$scope.selectedConfiguration = data;
		$scope.dbConfiguration = jQuery.extend({},data);
		$scope.msgbox.header = $scope.selectedConfiguration.path;
		$scope.usedActions = $scope.testActions.filter($scope.hasAction);
		console.log ("[edit configuration]: ");
		prettyPrintConsole(data);
		$('#configuration-form').attr('method','PUT');
		$('#configuration-dialog').modal({show: true})
	};
	$scope.addConfiguration = function() {
		$scope.selectedConfiguration = {};
		$scope.dbConfiguration = {};
		$scope.msgbox.header = "New Configuration";
		console.log ("adding new configuration");
		$('#configuration-form').attr('method','POST');
		$('#configuration-dialog').modal({show: true});
	};
	$scope.isModified = function() {
		// loop through each property and check if the original is different
		var modified = false;
		Object.keys($scope.selectedConfiguration).forEach(function (val, idx, array) {
			if ($scope.selectedConfiguration[val] !== $scope.dbConfiguration[val])
				modified=true;
		});
		return modified;
	};
	
	$scope.showTestConfiguration = function() {
		console.log ("building test parameters...");
		$scope.buildTestConfigurationParameters();
		console.log ("showing selectedConfiguration...");
		$('#test-configuration-dialog').modal({show: true});
	};
	$scope.hasResultsMsg = function() {
		var hasMsg = ($scope.testResultsMsg && JSON.stringify($scope.testResultsMsg) != '{}')
		return hasMsg;
	};
	$scope.testConfiguration = function() {
		console.log ("testing selectedConfiguration...");
		// use an ajax call to get results
		// get our form and use it to serialize the data elements
		var form = $("#test-configuration-form");
        //alert("form action: " + form.attr('action'));
        //alert("serialized params: " + form.serialize());
		var form_array = form.serializeArray();
		var form_method = form.attr('method');
		if (form_method.toUpperCase() === 'PUT') {
			form_method = "POST";
			form_array.push({name: "$action", value: "update"});
		}
		var data = $.param(form_array);
		$.ajax({
			async: false,
			type: form_method,
			url: (ctx + $scope.selectedConfiguration.path),
			data: data,
			success: function(data) {
				prettyPrintConsole(data);
				$scope.testHeaders = [];
				$scope.testResults = [];
				$scope.testResultsMsg = {};
				// callable statements need to display as json
				// NOTE: remove the 2 to go back to table; major issues when looking at many fields
				if ($scope.testAction == 'query2' && (!data.cs)) {
					if (data[0])
						$scope.testHeaders = Object.keys(data[0]);
					$scope.testResults=data;
				}
				else {
					prettyPrintConsole(data);
					$scope.testResultsMsg = prettyPrint(toProduction(data));
					$("#results_msg").html($scope.testResultsMsg);
				}
				$('#test-configuration-results-dialog').modal({
					show: true
				});
			}
		}).fail(function(jqXHR, textStatus) {
			var umsg = jqXHR.responseText.toUpperCase();
			alert(jqXHR.responseText);
		});
	};

	$scope.hasAction = function(value) {
		if (value == 'query' && $scope.selectedConfiguration.query_statement && $scope.selectedConfiguration.query_statement.length > 0)
			return true;
		else if (value == 'insert' && $scope.selectedConfiguration.insert_statement && $scope.selectedConfiguration.insert_statement.length > 0)
			return true;
		else if (value == 'update' && $scope.selectedConfiguration.update_statement && $scope.selectedConfiguration.update_statement.length > 0)
			return true;
		else if (value == 'delete' && $scope.selectedConfiguration.delete_statement && $scope.selectedConfiguration.delete_statement.length > 0)
			return true;
		else
			return false;
	};

	$scope.buildTestConfigurationParameters = function() {
		console.log('action: ' + $scope.testAction);
		if ($scope.testAction == 'query')
			$scope.testSql = $scope.selectedConfiguration.query_statement;
		else if ($scope.testAction == 'insert')
			$scope.testSql = $scope.selectedConfiguration.insert_statement;
		else if ($scope.testAction == 'update')
			$scope.testSql = $scope.selectedConfiguration.update_statement;
		else if ($scope.testAction == 'delete')
			$scope.testSql = $scope.selectedConfiguration.delete_statement;
		else
			console.log('unhandled action [' + $scope.testAction + "] received.");
		console.log('sql: ' + $scope.testSql);
		// count the number of question marks in the selected sql and set up parameters for them
		if ($scope.testSql.length > 0) {
			var qcount = $scope.testSql.count("?");
			console.log('qcount: ' + qcount);
			$scope.testParams = [];
			for (var i = 0; i < qcount; i++) {
				$scope.testParams[i] = {};
				$scope.testParams[i].name = 'param ' + i;
				$scope.testParams[i].value = '';
			}
			console.log('testParams: ' + $scope.testParams);
		}
	};	

		// check if we are not set right and set it
	if ($scope.app.accordian.index != 0) {
		//alert('setting accordian configurations...');
		$scope.app.setAccordian('#configurations');
		}

});


app.controller("ConnectionsController", function($scope, $http, ConnectionService) {
	$scope.selectedConnection = {};
	$scope.selectedConnection.name="";
	$scope.dbConfiguration = {};
	$scope.connections = [];
	$scope.testResultsMsg = {};
	$scope.msgbox.header = "";

	// async load the links from database
    ConnectionService.items().then(function (data) {
		$scope.connections = data;
		if ($scope.connections)
			$scope.connections.sort(sortByName);
		console.log("Connections: " + JSON.stringify($scope.connections));
	}, function(response) {
		$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
	});
	
	$scope.nameFilter = function(con) {
		var thisFilter = "";
		if ($scope.app.viewFilter[1])
			thisFilter = $scope.app.viewFilter[1].trim();
		if (thisFilter.length == 0)
			return true;
		if (!(con && con.name))
			return true;
		if (con.name.startsWith(thisFilter))
			return true;
			
		return false;
	};

	
	$scope.cantModifyConnection = function() {
		// we can only modify a configuration that has been saved; any edits and we disable
		if ($scope.selectedConnection && $scope.dbConnection && $scope.selectedConnection.name && $scope.selectedConnection.$$hashKey)
			if ($scope.isModified($scope.selectedConnection) == false && $scope.selectedConnection.name.length > 1)
				return false;
		return true;
	};
	$scope.cantSaveConnectition = function () {
		// in order to save we have to have a value for the connection, path > 1 and at least one sql not blank
		if (isObject($scope.selectedConfiguration) && isObject($scope.dbConfiguration) && ($scope.selectedConfiguration.name && $scope.selectedConfiguration.name.length > 1)) {
			if ($scope.selectedConfiguration.connection_name && $scope.selectedConfiguration.connection_name.length > 0)
					if( ($scope.selectedConfiguration.query_statement && $scope.selectedConfiguration.query_statement.length > 0) || 
						($scope.selectedConfiguration.update_statement && $scope.selectedConfiguration.update_statement.length > 0) || 
						($scope.selectedConfiguration.insert_statement && $scope.selectedConfiguration.insert_statement.length > 0) ||
						($scope.selectedConfiguration.delete_statement && $scope.selectedConfiguration.delete_statement.length > 0))
							return false;
		}
		return true;
	};

	$scope.saveConnection = function() {
		var form = $("#connection-form");
		var form_array = form.serializeArray();
		// var form_data = form.serialize();
		// console.log ("data: " + form_data);

		// async save the connection to the database
		ConnectionService.update($scope.selectedConnection.$$hashKey, form_array).then(function (data) {
			prettyPrintConsole(data);
			$scope.msgbox.header = $scope.selectedConnection.name;
			$("#dlg-msg").html(prettyPrint(toProduction(data)));
			// reread the configuration data
			ConnectionService.items().then(function (data) {
				$scope.connections = data;
				if ($scope.connections)
					$scope.connections.sort(sortByName);
				console.log("Reloaded Connections: " + JSON.stringify($scope.connections));
				$('#msg-box').modal('show');
			});
		}, function(response) {
			$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
		});
	};
	$scope.editConnection = function(data) {
		$scope.selectedConnection = data;
		$scope.dbConnection = jQuery.extend({},data);
		$scope.msgbox.header = $scope.selectedConnection.name;
		console.log ("[edit connection]: ");
		prettyPrintConsole(data);
		console.log ("connection_name: " + $scope.selectedConnection.name);
		$('#connection-form').attr('method','PUT');
		$('#connection-dialog').modal({show: true});
	};
	$scope.addConnection = function() {
		$scope.selectedConnection = {};
		$scope.dbConnection = {};
		console.log ("adding new connection");
		$('#connection-form').attr('method','POST');
		$('#connection-dialog').modal({show: true});
	};
	$scope.isModified = function() {
		// loop through each property and check if the original is different
		var modified = false;
		Object.keys($scope.selectedConnection).forEach(function (val, idx, array) {
			if ($scope.selectedConnection[val] !== $scope.dbConnection[val])
				modified=true;
		});
		return modified;
	};
	$scope.hasResultsMsg = function() {
		var hasMsg = ($scope.testResultsMsg && JSON.stringify($scope.testResultsMsg) != '{}')
		return hasMsg;
	};
	
	$scope.deleteConnection = function() {
		if ($scope.selectedConnection && $scope.selectedConnection.name && $scope.selectedConnection.name.length > 0 && $scope.selectedConnection.$$hashKey && $scope.selectedConnection.$$hashKey.length > 0) {
            ConnectionService.remove($scope.selectedConnection.name).then(function(data) {
				prettyPrintConsole(data);
				$scope.msgbox.header = $scope.selectedConnection.name;
				$("#dlg_msg").html(prettyPrint(toProduction(data)));
				// reread the configuration data
				ConnectionService.items().then(function (data) {
					$scope.connections = data;
					if ($scope.connections)
						$scope.connections.sort(sortByName);
					console.log("Reloaded Connections after delete... ");
					$('#msg-box').modal('show');
					$('#connection-dialog').modal('hide');
				});
					}, function(response) {
				$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
				alert(JSON.stringify(response, null, 2));
				$('#connection-dialog').modal('hide');
			});			
		}
	};

	$scope.testConnection = function() {
		console.log ("testing selectedConnection...");
		// use an ajax call to get results
		// get our form and use it to serialize the data elements
		var form = $("#connection-form");
//         alert("form action: " + form.attr('action'));
//         alert("serialized params: " + form.serialize());
        console.log('params: ' + form.serialize());
		var form_array = form.serializeArray();
		var form_method = form.attr('method');
        if (form_method.toUpperCase() === 'PUT') {
			form_method = "POST";
			form_array.push({name: "$action", value: "update"});
		}
        var data = $.param(form_array);
		$.ajax({
			async: false,
			type: form_method,
			url: (ctx + '/_system/connections/test'),
			data: data,
			success: function(data) {
				prettyPrintConsole(data);
				$scope.testHeaders = [];
				$scope.testResults = [];
				$scope.testResultsMsg = {};
				if (data.constructor === Array) {
					if (data[0])
						$scope.testHeaders = Object.keys(data[0]);
					$scope.testResults=data;
				}
				else {
					$scope.testHeaders = Object.keys(data);
					$scope.testResults.push(data);
				}
				prettyPrintConsole(data);
				$scope.testResultsMsg = prettyPrint(data);
				$("#results_msg").html($scope.testResultsMsg);
				$('#test-connection-results-dialog').modal('show');
			}
		}).fail(function(jqXHR, textStatus) {
			alert(jqXHR.responseText);
		});
	};

	function sortByName(a,b) {
		if (a.name.toLowerCase() < b.name.toLowerCase())
			return -1;
		if (a.name.toLowerCase() > b.name.toLowerCase())
			return 1;
		return 0;
	}
	
	// check if we are not set right and set it
	if ($scope.app.accordian.index != 1) {
		//alert('setting accordian connections...');
		$scope.app.setAccordian('#connections');
		}
});

app.controller("DataController", function($scope, $http, DataService) {
	$scope.tables = ["test1", "test2", "test3"];
	$scope.selectedTable = {};
	$scope.dbTable = {};
	$scope.tableHeaders = ["id", "name"];
	
	// async load the links from database
// 	DataService.tables().then(function (data) {
// 		$scope.tables = data;
// 		if ($scope.tables)
// 			$scope.tables.sort(sortByUri);
// 		console.log("tables: " + $scope.tables);
// 	}, function(response) {
// 		$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
// 	});

	$scope.editTable = function(table) {
//		alert("in edit table: " + JSON.stringify(table));
		// setup the form for edit before showing
		$scope.selectedTable = table;
	};
	$scope.saveTable = function() {
		if ($scope.table) {
			alert ("saving: " + JSON.stringify($scope.table));
			DataService.insertTable($scope.table).then(
				function(data) {
					alert("update response: " + JSON.stringify(data));
					DataService.tables().then(function (data) {
						$scope.tables = data;
						if ($scope.tables)
							$scope.tables.sort(sortByUri);
						console.log("Go URLs: " + JSON.stringify($scope.gourls));
					}, function(response) {
						$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
					});
				}, function(response) {
					$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
				});
		}
	};

	$scope.deleteTable = function(id) {
		if ($scope.link && id)
			alert("delete go url: " + JSON.stringify($scope.link));
		if (id.length > 0) {
			alert ("id passed: " + id);
			DataService.removeTable(id).then(
				function(data) {
					alert("delete response: " + JSON.stringify(data));
					// remove the element from our existing list
					var i = $scope.tables.indexOf($scope.table);
					if (i != -1)
						$scope.tables.splice(i, 1);
				}, function(response) {
					$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
				});
		}
	};

	function sortByUri(a,b) {
		if (a.uri.toLowerCase() < b.uri.toLowerCase())
			return -1;
		if (a.uri.toLowerCase() > b.uri.toLowerCase())
			return 1;
		return 0;
	}
	// check if we are not set right and set it
	if ($scope.app.accordian.index != 2) {
			//alert('setting accordian data...');
			$scope.app.setAccordian('#tables');
		}

});

app.filter('capitalize', function() {
  	return function(input, scope) {
    	if (input!==undefined && input!==null) {
    		input = input.toLowerCase();
    		return input.substring(0,1).toUpperCase()+input.substring(1);
    	}
    	return "";
  	}  	
});

app.filter('unescape_quote', function() {
    return function(input, scope) {
        if (input!==undefined && input!==null) {
            input = input.replace(/\\'/g, '\'');
            return input;
        }
        return "";
    }
});

app.directive("fileread", [function () {
    return {
        scope: {
            fileread: "="
        },
        link: function (scope, element, attributes) {
            element.bind("change", function (changeEvent) {
                scope.$apply(function () {
                    scope.fileread = changeEvent.target.files[0];
                    // or all selected files:
                    // scope.fileread = changeEvent.target.files;
                });
            });
        }
    }
}]);

app.controller("AppController", function($scope, ConnectionService) {
//	// async load the collections from database
//	LinkService.links().then(function (data) {
//		$scope.links = data;
//		console.log("Links: " + $scope.links);
//	}, function(response) {
//		$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
//	});

	// ----- basic properties -----
	// all alerts are displayed on each page at the top if not empty
	$scope.alerts = [];

	//$scope.user = window.userId;
	$scope.appversion = "1.0";
	//console.log ("[sso]: " + $scope.user);
	// the app msgbox header variable to be manipulated by child controllers
	$scope.msgbox = {};
	$scope.msgbox.header = "";
	$scope.app = {};
	$scope.app.accordian = {};
	$scope.app.setAccordian = function(link) {
		$("#accordion").find(".panel-collapse.collapse.in").removeClass('in');
		$(link).addClass('in');
		if (link == '#configurations')
			$scope.app.accordian.index=0;
		if (link == '#connections')
			$scope.app.accordian.index=1;
		if (link == '#data')
			$scope.app.accordian.index=2;
	};
	$scope.app.accordian.index = 0;
	$scope.app.viewFilter = ["", "", ""];
	$scope.app.tagFilter = ["!_system", "", ""];

    $scope.downloadConfigurations = function() {
    	console.log('downloading configurations...');
        window.location.href = ctx + '/_system/configurations/download?pathFilter=' + $scope.app.viewFilter[0] + '&tagFilter=' + $scope.app.tagFilter[0];
    };
    $scope.uploadConfigurations = function() {
    	console.log('uploading configurations...');
    	$scope.app.upload_type = "configurations";
        document.getElementById('file-upload-form').action = ctx + '/_system/configurations/upload';
        $('#file-upload-dialog').modal('show');
    };
    $scope.downloadConnections = function() {
    	console.log('downloading connections...');
        window.location.href = ctx + '/_system/connections/download?nameFilter=' + $scope.app.viewFilter[1];
    };
    $scope.uploadConnections = function() {
    	console.log('uploading connections...');
    	$scope.app.upload_type = "connections";
        document.getElementById('file-upload-form').action = ctx + '/_system/connections/upload';
        $('#file-upload-dialog').modal('show');
    };
	$scope.setRootLogLevel = function(requestLevel) {
	 	console.log("setting log level to [" + requestLevel + "]...");
		ConnectionService.setRootLogLevel(requestLevel).then(
			function(data) {
				alert("set log level response: " + JSON.stringify(data));
			}, function(response) {
				$scope.alerts.push({type: 'danger', msg: 'AJAX Response (' + response.status + ') - ' + response.data});
			});
		window.location.reload();
	};
// 	$scope.$on('$viewContentLoaded', function (event) {
// 		console.log("$viewContentLoaded --- event");
// 		console.log(event);
// 		console.log(arguments);
// 	});

    // try and change our file upload results to go to the iframe
    document.getElementById('file-upload-form').onsubmit=function() {
        document.getElementById('file-upload-form').target = 'upload_target'; //'upload_target' is the name of the iframe
    }

});

function prettyPrintConsole(data) {
		var str = data;
		if (typeof str != 'string') 
			str = JSON.stringify(data, undefined, 2); // indentation level = 2
		var 
        arr = [],
        _string = 'color:green',
        _number = 'color:darkorange',
        _boolean = 'color:blue',
        _null = 'color:magenta',
        _key = 'color:red';

    	str = str.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var style = _number;
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                style = _key;
            } else {
                style = _string;
            }
        } else if (/true|false/.test(match)) {
            style = _boolean;
        } else if (/null/.test(match)) {
            style = _null;
        }
        arr.push(style);
        arr.push('');
        return '%c' + match + '%c';
    	});

    	arr.unshift(str);

    	console.log.apply(console, arr);
	}

// production attempts to strip out all the details from the server response that are unnecessary
function toProduction(data) {
	var newData = {};
	if (data.data && data.status && data.statusText) {
		newData.data = data.data;
		newData.status = data.status;
		newData.statusText = data.statusText;
		return newData;
	}
	else 
		return data;
}
	
function prettyPrint(data) {
		var str = data;
		if (typeof str != 'string') 
			str = JSON.stringify(data, undefined, 4);
    	str = str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    	return str.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });	
}

	function sortByPath(a,b) {
		if (a.path.toLowerCase() < b.path.toLowerCase())
			return -1;
		if (a.path.toLowerCase() > b.path.toLowerCase())
			return 1;
		return 0;
	}
	function sortByName(a,b) {
		if (a.name.toLowerCase() < b.name.toLowerCase())
			return -1;
		if (a.name.toLowerCase() > b.name.toLowerCase())
			return 1;
		return 0;
	}

function isObject(val) {
	if (typeof val === 'object' && val !== null)
		return true;
	return false;
};