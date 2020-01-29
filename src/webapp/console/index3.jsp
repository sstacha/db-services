<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8" />
	<title>Data Services Console</title>
<%--	<link rel=stylesheet href=console.css type=text/css>--%>

    <!-- link to get the jqueryui look and feel css stuff -->
    <link rel=stylesheet href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/themes/smoothness/jquery-ui.css" type=text/css>
    <!-- link to google font to look "special" on all browsers -->
    <link href='//fonts.googleapis.com/css?family=PT+Sans+Narrow:700' rel='stylesheet' type='text/css'>
    <!-- normal jquery include links -->
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/jquery-ui.min.js"></script>
    <%--<script src="/includes/jquery.fileDownload.js" type="text/javascript"></script>--%>
    <%-- uncomment to support older (ie7 and early ie8 browsers for JSON parsing and stringify)
    <%--<script type="text/javascript" src="//github.com/douglascrockford/JSON-js/raw/master/json2.js"></script>--%>
	<script type="text/javascript">
        var keywordArray = [];
//        var defaultConnection;
        var connectionNames = [];

        // takes an array of configuration elements and creates HTML visual representation to display on the page
        function showConfigurations(data) {
            var resultsContainer = document.getElementById("configuration_list");
            $('#configuration_list').empty();
            // test that we have a container element
            if (resultsContainer && typeof(resultsContainer.nodeName) !== 'undefined') {
                // make sure we have results to display
                if (typeof(data) !== 'undefined' && data instanceof Array && data.length > 0) {
                    var resultsReplacement = resultsContainer.cloneNode();

                    // add each element to the table body fragment to be replaced later
                    var fragment = document.createDocumentFragment(), div, i, il, record;
                    var resultHtml;
                    for (i=0, il=data.length;i<il;i++) {
                        record = data[i];
                        resultHtml = "";
                        // build our inner html that we don't need extra stuff for
                        resultHtml +=  (record.path + "<br>");
                        if (record.connection_name && record.connection_name != 'null' && record.connection_name != 'default')
                            resultHtml += ("<b>CONNECTION:</b> " + record.connection_name + "<br>");
                        if (record.query_statement && record.query_statement != 'null')
                            resultHtml += ("<b>QUERY:</b> " + record.query_statement + "<br>");
                        if (record.insert_statement && record.insert_statement != 'null')
                            resultHtml += ("<b>INSERT:</b> " + record.insert_statement + "<br>");
                        if (record.update_statement && record.update_statement != 'null')
                            resultHtml += ("<b>UPDATE:</b> " + record.update_statement + "<br>");
                        if (record.delete_statement && record.delete_statement != 'null')
                            resultHtml += ("<b>DELETE:</b> " + record.delete_statement + "<br>");
                        if (record.keywords && record.keywords != 'null')
                            resultHtml += ("<b>KEYWORDS:</b> " + record.keywords + "<br>");

                        div = document.createElement('div');
                        div.className = "ui-list-content-block configuration";
                        div.innerHTML = resultHtml;
                        div.record = record;
                        div.categories = convertToArray(record.keywords);
                        div.onclick = function() {
							var pConnectionName = (this.record.connection_name && this.record.connection_name != 'null') ? this.record.connection_name : 'default';
                            var editConfigDialog = document.getElementById("edit-configuration-dialog");

                            // placing on dialog div so we can test the last saved values
                            editConfigDialog.connection_name = pConnectionName;
                            editConfigDialog.path = this.record.path.trim();
                            editConfigDialog.action = this.record.path.trim();
                            editConfigDialog.querysql = this.record.query_statement;
                            editConfigDialog.insertsql = this.record.insert_statement;
                            editConfigDialog.updatesql = this.record.update_statement;
                            editConfigDialog.deletesql = this.record.delete_statement;
							$('select#edit-configuration-connection').val(pConnectionName);
							$("input#edit-configuration-path").val(this.record.path.trim());
                            $("input#edit-configuration-id").val(this.record.path.trim());
                            $("textarea#edit-configuration-querysql").val(this.record.query_statement);
							$("textarea#edit-configuration-insertsql").val(this.record.insert_statement);
							$("textarea#edit-configuration-updatesql").val(this.record.update_statement);
							$("textarea#edit-configuration-deletesql").val(this.record.delete_statement);
                            $("input#edit-configuration-keywords").val(this.record.keywords);
                            $('#edit-configuration-dialog').dialog('open');
                        };
                        fragment.appendChild(div);
                    }
                    resultsReplacement.appendChild(fragment);
                }
                resultsContainer.parentNode.replaceChild(resultsReplacement, resultsContainer);
            }
            filterConfigurations();
        }

        // takes an array of connection elements and creates HTML visual representation to display on the page
        function showConnections(data) {
//            reset = ((reset != null) ? reset : false);
            var completeList = data;
//            if (defaultConnection != null && defaultConnection != 'undefined') {
//                completeList = new Array(data.length + 1);
//                completeList[0]=defaultConnection;
//                for (i=0;i<data.length;i++)
//                    completeList[i+1]=data[i];
//            }
//            else
//                completeList = data;
            $('#connection_list').empty();
            var resultsContainer = document.getElementById("connection_list");

            // test that we have a container element
            if (resultsContainer && typeof(resultsContainer.nodeName) !== 'undefined') {
                // make sure we have results to display
                if (typeof(completeList) !== 'undefined' && completeList instanceof Array && completeList.length > 0) {
                    var resultsReplacement = resultsContainer.cloneNode();

                    // add each element to the table body fragment to be replaced later
                    var fragment = document.createDocumentFragment(), div, i, il, record;
                    var resultHtml;
                    for (i=0, il=completeList.length;i<il;i++) {
                        record = completeList[i];
                        resultHtml = "";
                        // build our inner html that we don't need extra stuff for
                        if (record.name && record.name != 'null')
                            resultHtml += (record.name + "<br>");
                        if (record.type && record.type != 'null' && record.type == 'jndi') {
                            if (record.jndi_name && record.jndi_name != 'null')
                                resultHtml += ("<b>JNDI NAME:</b> " + record.jndi_name + "<br>");
                            if (record.jndi_context && record.jndi_context != 'null')
                                resultHtml += ("<b>JNDI CONTEXT:</b> " + record.jndi_context + "<br>");
                        }
                        else {     // everything but a jndi value will default to jdbc fields
                            if (record.jdbc_driver && record.jdbc_driver != 'null')
                                resultHtml += ("<b>JDBC DRIVER:</b> " + record.jdbc_driver + "<br>");
                            if (record.jdbc_url && record.jdbc_url != 'null')
                                resultHtml += ("<b>JDBC URL:</b> " + record.jdbc_url + "<br>");
                            if (record.user_name && record.user_name != 'null')
                                resultHtml += ("<b>JDBC LOGIN:</b> " + record.jdbc_username + "<br>");
                            if (record.password && record.password != 'null')
                                resultHtml += ("<b>JDBC PASSWORD:</b> " + record.jdbc_password + "<br>");
                        }
                        if (record.description && record.description != 'null')
                            resultHtml += ("<b>DESCRIPTION:</b> " + record.description + "<br>");

                        div = document.createElement('div');
                        div.className = "ui-list-content-block configuration";
                        div.innerHTML = resultHtml;
                        div.record = record;
                        div.onclick = function() {
							var pType = (this.record.type && this.record.type != 'null') ? this.record.type : 'jdbc';
                            $("input#edit-connection-name").val(this.record.name);
                            $("input#edit-connection-id").val(this.record.name);
							$('select#edit-connection-type').val(pType);
							$("input#edit-connection-jndi-name").val(this.record.jndi_name);
                            $("input#edit-connection-jndi-context").val(this.record.jndi_context);
                            $("input#edit-connection-driver").val(this.record.jdbc_driver);
							$("input#edit-connection-url").val(this.record.jdbc_url);
							$("input#edit-connection-login").val(this.record.jdbc_username);
							$("input#edit-connection-password").val(this.record.jdbc_password);
                            $("input#edit-connection-description").val(this.record.description);
                            showConnectionTypeFields($("#edit-connection-type").val(), "#edit-connection-");
							$('#edit-connection-dialog').dialog('open');
                        };
                        fragment.appendChild(div);
                    }
                    resultsReplacement.appendChild(fragment);
                }
                resultsContainer.parentNode.replaceChild(resultsReplacement, resultsContainer);
            }
        }

        // takes an array of configuration elements, loads distinct keywords to global array and then fills the category filter drop down
        function showCategories(data) {
            // loop through the configuration json data and pull keywords; insert only unique values into the keywordArray
            //      the result should be a distinct list of keywords from all configurations
            var idx=0;
            keywordArray = [];
            if (typeof(data) !== 'undefined' && data instanceof Array && data.length > 0) {
                var thisKeywords;
                var thisKeywordArray;
                keywordArray[idx++] = '!system';
                keywordArray[idx++] = 'all';
                for (var i=0; i<data.length; i++) {
                    thisKeywords = data[i].keywords;
                    thisKeywordArray = convertToArray(thisKeywords);
                    if ($.inArray('system', thisKeywordArray) == -1) {
                    for (var j=0; j<thisKeywordArray.length; j++) {
                        if ($.inArray(thisKeywordArray[j], keywordArray) == -1)
                            keywordArray[idx++] = thisKeywordArray[j];
                    }
                    }
                }
                idx=0;
                $("#fltr-categories").empty();
                var categorySelection = document.getElementById("fltr-categories");
                for (i=0; i<keywordArray.length; i++)
                    categorySelection[idx++]=new Option(keywordArray[i], keywordArray[i], false, false);
            }
        }

        function showConfigurationConnections(data) {
            // get our complete list
            var completeList = data;
//            if (defaultConnection != null && defaultConnection != 'undefined') {
//                completeList = new Array(data.length + 1);
//                completeList[0]=defaultConnection;
//                for (i=0;i<data.length;i++)
//                    completeList[i+1]=data[i];
//            }
//            else
//                completeList = data;

            $("#add-configuration-connection").empty();
            $("#edit-configuration-connection").empty();
            $("#at-con").empty();
            var acfConnection = document.getElementById("add-configuration-connection");
            var ecfConnection = document.getElementById("edit-configuration-connection");
            var atConnection = document.getElementById("at-con");
            for (i=0; i<completeList.length; i++) {
                acfConnection[i]=new Option(completeList[i].name, completeList[i].name, false, false);
                ecfConnection[i]=new Option(completeList[i].name, completeList[i].name, false, false);
                atConnection[i]=new Option(completeList[i].name, completeList[i].name, false, false);
            }
        }

        function filterConfigurations() {
            var categorySelection = $("#fltr-categories").val();
            var filter = categorySelection;
            $("div.configuration").each(function() {
                // if we have a category selected to filter on then only show results for the category selected else show all
                if (categorySelection && categorySelection !== 'undefined') {
                    if (categorySelection == '*' || categorySelection == 'all')
                        $(this).show();
                    else {
                        if (this.categories == null || this.categories == 'undefined')
                            $(this).hide();
                        else {
                        	if (categorySelection.substring(0,1) == '!') {
                        		filter = categorySelection.substring(1);
                            	if ($.inArray(filter, this.categories) > -1)
                                	$(this).hide();
                            	else
                                	$(this).show();
                        	}
                        	else {
                            	if ($.inArray(categorySelection, this.categories) > -1)
                                	$(this).show();
                            	else
                                	$(this).hide();
                            }
                        }
                    }
                }
                else
                    $(this).show();
            });

        }

        function getUserId() {
            var userId = "";
            // first look for the _uid cookie value
            if (navigator.cookieEnabled && nominatorId.length == 0)
                nominatorId = document.cookie.getValueByKey("_uid");
            // todo: look for header value as new security product will use this method by default
            // last look for userId as a parameter
            // NOTE: it is assumed a filter or security product will ensure parameter manipulation is not allowed
            if (location.search != "")
                nominatorId = location.search.getValueByKey("_uid");
            if ((!nominatorId) || (nominatorId.length == 0))
            {
                alert("user id was not passed. this should not happen.");
                return false;
            }
            return userId;
        }

        // convert to array converts a comma separated string to an array of string values
        function convertToArray(str) {
            var nospaces = $.trim(str);
            arr = nospaces.split(',');
            for (val in arr)
                arr[val] = arr[val].trim();
            return arr;
        }

        // sort function (applies to nested stuff too)
        function sortArray (prop, arr) {
            prop = prop.split('.');
            var len = prop.length;

            arr.sort(function (a, b) {
                var i = 0;
                while( i < len ) {
                    a = a[prop[i]];
                    b = b[prop[i]];
                    i++;
                }
                if (a < b) {
                    return -1;
                } else if (a > b) {
                    return 1;
                } else {
                    return 0;
                }
            });
            return arr;
        }

		// add trim functionality to string class if browser does not support (IE doesn't)
		if(typeof String.prototype.trim !== 'function') {
			String.prototype.trim = function() {
			return this.replace(/^\s+|\s+$/g, ''); 
			}
		}
        // add functionality to string class to pull values from the URI (gets cookies, headers, parameters etc)
        String.prototype.getValueByKey = function(k){
            var p = new RegExp('\\b'+k+'\\b','gi');
            return this.search(p) != -1 ? decodeURIComponent(this.substr(this.search(p)+k.length+1).substr(0,this.substr(this.search(p)+k.length+1).search(/(&|;|$)/))) : "";
        };
        // counts the number of occurrences in the string of the 's1' string supplied
        String.prototype.count = function(s1) {
            var m = this.match(new RegExp(s1.toString().replace(/(?=[.\\+*?[^\]$(){}\|])/g, "\\"), "g"));
            return m ? m.length:0;
        }

        function getConfigurations()
        {
            // load the initial list of configurations from local web service and display them once returned
            $.getJSON("<%=request.getContextPath()%>/_system/configurations",
                    function(data){
                        <%--alert("setting up configurations");--%>
                        <%--alert(data);--%>
                        showCategories(data);
                        showConfigurations(data);
                    });
        }

        function getConnections()
        {
            // load the current system default connection
            $.getJSON("<%=request.getContextPath()%>/_system/connections",
                    function(data){
                        // changing where all connections are returned as data in one shot
                        showConnections(data);
                        showConfigurationConnections(data);
                    });
        }

        // jquery onload function
        jQuery(document).ready(function($) {
            // lets show any ajax errors
//            $(document).ajaxError(function(e, xhr, settings, exception) {
//                alert('error in: ' + settings.url + ' \n'+'error:\n' + xhr.responseText );
//            });

            $("#accordion").accordion({ heightStyle: "content" });
            $(".ui_button").button();
            $(".add_icon").button({
                    icons: {
                    secondary: "ui-icon-plusthick"
                    },
                    text: true
                });
            $("#add-table-field_button").button({
                    icons: {
                    secondary: "ui-icon-plusthick"
                    },
                    text: true
                });
            showPanel("configurations");

            // initially display our current configurations
            getConfigurations();

            $( "#del-configuration-confirm" ).dialog({
                autoOpen: false,
                resizable: false,
                height:140,
                modal: true,
                buttons: {
                    "Delete Configuration": function() {
                        var pId = $("input#edit-configuration-id").val();
                        var pData = "pId=" + pId;
//                        alert (pData);
                        $.ajax({
                            async: false,
                            type: 'DELETE',
                            url: '<%=request.getContextPath()%>/_system/configurations',
                            data: pData,
                            success: function(data) {
                                alert("deleted configuration: " + data);
                                // operations now clear cache so we can now just directly re-read our configurations
                                getConfigurations();
                                $( "#del-configuration-confirm" ).dialog( "close" );
                                $( "#edit-configuration-dialog" ).dialog( "close" );
                            }
                        }).fail(function(jqXHR, textStatus) {
                            alert (jqXHR.responseText);
                        });
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                }
            });

            $( "#add-table-confirm" ).dialog({
                autoOpen: false,
                resizable: false,
                height:140,
                modal: true,
                buttons: {
                    "Build Table": function() {
                        // figure out the starting params; set sql; show add-table-dialog
                        $("#add-table-dialog").dialog('open');
                        $( this ).dialog( "close" );
                        // todo : add processing for if they check the do not show again
                    },
                    Close: function() {
                        // todo : add processing for if they check the do not show again
                        $( this ).dialog( "close" );
                    }
                }
            });

            $( "#add-table-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
						"Add Table": function() {
                            // get our form and use it to serialize the data elements
                            var form = $("#add-table-form");
//                            alert("form action: " + testForm.attr('action'));
//                            alert("serialized params: " + testForm.serialize());
                            $.ajax({
                            async: false,
                            type: form.attr('method'),
                            url: form.attr('action'),
                            data: form.serialize(),
                            success: function(data) {
                                alert("Table Created");
                                $( this ).dialog( "close" );
                            }
                            }).fail(function(jqXHR, textStatus) {
                                    alert(jqXHR.responseText);
                                });
						},
                        Cancel: function() {$( this ).dialog( "close" );}
                        }
                    });

            $( "#add-configuration-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
                            "Add Configuration": function() {
                                var form = $("#add-configuration-form");
								$.ajax({
                                    async: false,
                                    type: form.attr('method'),
                                    url: form.attr('action'),
                                    data: form.serialize(),
                                    success: function(data) {
                                        alert("saved configuration: " + data);
//                                    alert("saved configuration: " + JSON.stringify(data));
                                    // we can now directly just re-read our configs since cache is now cleared after operation server side
                                        getConfigurations();
                                        $( "#add-configuration-dialog" ).dialog( "close" );
                                    }
                                }).fail(function(jqXHR, textStatus) {
                                    alert(jqXHR.responseText);
                                });
                            },
                            Cancel: function() {
                                $( this ).dialog( "close" );
                            }
                        },
                        close: function() {
//                            allFields.val( "" ).removeClass( "ui-state-error" );
                        }
                    });

            $( "#edit-configuration-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
                            Test: function() {
                                // set the action of the test form to our path
                                var editConfigDialog = document.getElementById("edit-configuration-dialog");
                                // lets start with putting each query in the edit configuration form attached to the div
                                var testConfigDialog = document.getElementById("test-configuration-dialog");
                                var testConfigForm = document.getElementById("test-configuration-form");
                                testConfigDialog.querysql = editConfigDialog.querysql;
                                testConfigDialog.insertsql = editConfigDialog.insertsql;
                                testConfigDialog.updatesql = editConfigDialog.updatesql;
                                testConfigDialog.deletesql = editConfigDialog.deletesql;
                                testConfigDialog.connection_name = editConfigDialog.connection_name;
                                if (editConfigDialog.action.charAt(0) != "/")
                                    editConfigDialog.action = "/" + editConfigDialog.action;
                                testConfigForm.action = "<%=request.getContextPath()%>" + editConfigDialog.action;

                                // test this works; delete after done
    //                            alert(testConfigDialog.querysql);
    //                            alert(testConfigDialog.insertsql);
    //                            alert(testConfigDialog.updatesql);
    //                            alert(testConfigDialog.deletesql);
    //                            alert(testConfigDialog.connection_name);
    //                            alert("form action: " + testConfigForm.action);

                                // tcf-action maps to action parameter on post to tell what to actually do (should support all of them)
                                // next lets build our options based on values for each field; assign the query to the option (id = tcf-opt-query...)
                                var tcfActionSelect = document.getElementById("test-configuration-action");
                                $("#test-configuration-action").empty();
                                var i=0;
                                if (testConfigDialog.querysql != null && testConfigDialog.querysql.length > 0)
                                    tcfActionSelect[i++]= new Option("query", "query", false, false);
                                if (testConfigDialog.insertsql != null && testConfigDialog.insertsql.length > 0)
                                    tcfActionSelect[i++]= new Option("insert", "insert", false, false);
                                if (testConfigDialog.updatesql != null && testConfigDialog.updatesql.length > 0)
                                    tcfActionSelect[i++]= new Option("update", "update", false, false);
                                if (testConfigDialog.deletesql != null && testConfigDialog.deletesql.length > 0)
                                    tcfActionSelect[i++]= new Option("delete", "delete", false, false);

                                // finally build out the paramter input fields and show the form
                                buildTestConfigurationParameters();
                                $('#test-configuration-dialog').dialog('open');
                            },
                            Delete: function() {
                                $('#del-configuration-confirm').dialog('open');
                            },
                            Update: function() {
                                var form = $("#edit-configuration-form");
								$.ajax({
                                    async: false,
                                    type: form.attr('method'),
                                    url: form.attr('action'),
                                    data: form.serialize(),
									success: function(data) {
                                        alert("saved configuration: " + data);
                                        getConfigurations();
                                        $( "#edit-configuration-dialog" ).dialog( "close" );
                                    }
//                                  dataType: "json"
                                }).fail(function(jqXHR, textStatus) {
                                    alert( "Save Request failed: " + jqXHR.responseText );
                                });
                            },
                            Cancel: function() {
                                $( this ).dialog( "close" );
                            }
                        },
                        close: function() {
//                            allFields.val( "" ).removeClass( "ui-state-error" );
                        }
                    });

            $( "#test-configuration-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
						Test: function() {
                            // get our form and use it to serialize the data elements
                            var form = $("#test-configuration-form");
//                            alert("form action: " + testForm.attr('action'));
//                            alert("serialized params: " + testForm.serialize());
                            $.ajax({
                            async: false,
                            type: form.attr('method'),
                            url: form.attr('action'),
                            data: form.serialize(),
                            success: function(data) {
//                                alert(JSON.stringify(data));
                                $("#test-configuration-results").html(data);
                                $( "#test-configuration-results-dialog" ).dialog('open');
                            }
                            }).fail(function(jqXHR, textStatus) {
                                var umsg = jqXHR.responseText.toUpperCase();
                                var pos = umsg.indexOf("TABLE ");
                                var pos2 = umsg.indexOf(" NOT FOUND");
                                if (pos != -1 && pos2 != -1 && pos < pos2) {
                                    // attempt to create the table
                                    pos = umsg.indexOf("UPDATE: ", pos2);
                                    if (pos != -1)
                                        buildTableFields(umsg.substring(pos + 8));
                                    $("#at-con").val(document.getElementById("test-configuration-dialog").connection_name);
                                    $("#add-table-confirm").dialog('open');
                                }
                                else
                                    alert(jqXHR.responseText);
                                });
						},
                        Close: function() {$( this ).dialog( "close" );}
                        }
                    });

            $( "#test-configuration-results-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
                        Close: function() {$( this ).dialog( "close" );}
                        }
                    });

            $( "#file-upload-dialog" ).dialog({
                        autoOpen: false,
                        <%--height: 80,--%>
                        width: 350,
                        modal: true,
                        buttons: {
                        Close: function() {$( this ).dialog( "close" );}
                        }
                    });

            // initially display our current connections
            getConnections();

            $( "#del-connection-confirm" ).dialog({
                autoOpen: false,
                resizable: false,
                height:140,
                modal: true,
                buttons: {
                    "Delete Connection": function() {
                        var pId = $("input#edit-connection-id").val();
                        var pData = "pId=" + pId;
//                        alert (pData);
                        $.ajax({
                        async: false,
                        type: 'DELETE',
                        url: '<%=request.getContextPath()%>/_system/connections',
                        data: pData,
                        success: function(data) {
                            alert("deleted connection: " + data);
                            getConnections();
                            $( "#del-connection-confirm" ).dialog( "close" );
                            $( "#edit-connection-dialog" ).dialog( "close" );
                        }
                        }).fail(function(jqXHR, textStatus) {
                            alert(jqXHR.responseText);
                        });
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                }
            });

            $( "#add-connection-dialog" ).dialog({
                autoOpen: false,
                <%--height: 80,--%>
                width: 350,
                modal: true,
                buttons: {
                    "Test": function() {
                        var form = $("#add-connection-form");
                        $.ajax({
                            async: false,
                            type: 'GET',
                            url: '<%=request.getContextPath()%>/_system/connections/test',
                            data: form.serialize(),
                            success: function(data) {
                                if (data == 'true')
                                    alert("Test Connection Succeeded");
                                else
                                    alert( "Test Connection Failed");
                            }
                        }).fail(function(jqXHR, textStatus) {
                            alert(jqXHR.responseText);
                        });

                    },
                    "Add Connection": function() {
                        var form = $("#add-connection-form");
                        alert ("serialized data: " + form.serialize());
                        $.ajax({
                            async: false,
                            type: form.attr('method'),
                            url: form.attr('action'),
                            data: form.serialize(),
                            success: function(data) {
                                alert("saved connection: " + data);
//                                      alert("saved connection: " + JSON.stringify(data));
                                getConnections();
                                $( "#add-connection-dialog" ).dialog( "close" );
                            }
                        }).fail(function(jqXHR, textStatus) {
                            alert(jqXHR.responseText);
                        });
                    },
                    Cancel: function() {
                        $( this ).dialog( "close" );
                    }
                },
                close: function() {
//                            allFields.val( "" ).removeClass( "ui-state-error" );
                }
            });

            $( "#edit-connection-dialog" ).dialog({
                autoOpen: false,
                <%--height: 80,--%>
                width: 350,
                modal: true,
                buttons: {
                "Test": function() {
                    var form = $("#edit-connection-form");
                    $.ajax({
                        async: false,
                        type: 'GET',
                        url: '<%=request.getContextPath()%>/_system/connections/test',
                        data: form.serialize(),
                        success: function(data) {
                            if (data == 'true')
                                alert("Test Connection Succeeded");
                            else
                                alert( "Test Connection Failed");
                        }
//                                  dataType: "json"
                    }).fail(function(jqXHR, textStatus) {
                        alert(jqXHR.responseText);
                    });
                },
                "Delete": function() {
                    $('#del-connection-confirm').dialog('open');
                },
                "Update": function() {
                    var form = $("#edit-connection-form");
                    $.ajax({
                        async: false,
                        type: form.attr('method'),
                        url: form.attr('action'),
                        data: form.serialize(),
                        success: function(data) {
                            alert("saved connection: " + data);
                            getConnections();
                            $( "#edit-connection-dialog" ).dialog( "close" );
                        }
//                                  dataType: "json"
                    }).fail(function(jqXHR, textStatus) {
                        alert( "Save Request failed: " + jqXHR.responseText );
                    });
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
                },
                close: function() {}
            });

            // try and change our file upload results to go to the iframe
            document.getElementById('file-upload-form').onsubmit=function() {
            		document.getElementById('file-upload-form').target = 'upload_target'; //'upload_target' is the name of the iframe
            	}

            $(".info_content").hide();
            // include the header and footer from the includes folder
//            $.get("/inc/custom/typical_header_tall_no_jquery", function(data) {
//                $("#header").html(data);
//            });
        });

        function showConnectionTypeFields(typeSelection, prefix) {

            if (typeSelection == "jndi") {
                $(prefix + "jndi-fields").show();
                $(prefix + "jdbc-fields").hide();
            }
            else {
                $(prefix + "jndi-fields").hide();
                $(prefix + "jdbc-fields").show();
            }
        }

        function buildTestConfigurationParameters() {
            // get our selected test action and set up info and parameters based on selection
            var testConfigDialog = document.getElementById("test-configuration-dialog");
            var selectedAction = $("#test-configuration-action").val();
            var testConfigInfo = document.getElementById("test-configuration-info");
            var sql = "";
            var infoText = "";
            if (testConfigDialog.connection_name && testConfigDialog.connection_name != 'null' && testConfigDialog.connection_name != 'default')
                infoText += ("<b>CONNECTION:</b> " + testConfigDialog.connection_name + "<br>");
//            alert (selectedAction);
            if (selectedAction == "query") {
                sql = testConfigDialog.querysql;
                infoText += ("<b>QUERY:</b> " + sql + "<br>");
            }
            if (selectedAction == "insert") {
                sql = testConfigDialog.insertsql;
                infoText += ("<b>INSERT:</b> " + sql + "<br>");
            }
            if (selectedAction == "update") {
                sql = testConfigDialog.updatesql;
                infoText += ("<b>UPDATE:</b> " + sql + "<br>");
            }
            if (selectedAction == "delete") {
                sql = testConfigDialog.deletesql;
                infoText += ("<b>DELETE:</b> " + sql + "<br>");
            }
            testConfigInfo.innerHTML = infoText;
            // figure out how many question marks and make an input paramter for each
            var paramCount = 0;
            var i;
            if (sql && sql.length > 0)
                paramCount = sql.count("?");
            // for each parameter load up a field to hold the value
            $("div#test-configuration-parameters").empty();
            for (i=0; i<paramCount; i++) {
                $('<label for="param' + i + '">Param' + i + '</label><input type="text" class="field" name="param' + i + '" id="param' + i + '" />').appendTo('div#test-configuration-parameters');
            }
        }

        function buildCreateTableSql() {
            // loop through each field to build out the sql and replace the existing sql text
            var c = document.getElementById("add-table-fields").children.length;
            var i;
            var sql = "";
            var fieldList = "";
            var fname = "";
            var ftype = "";
            var fsize = "";
            var tableName = $("#at-table-name").val();
            for (i=0;i<c;i++) {
                fname = $("#at-field-name" + (i + 1)).val();
                ftype = $("#at-field-type" + (i + 1)).val();
                if (fname && ftype) {
                    if (fieldList.length > 0)
                        fieldList += ", ";
                    fieldList += fname + " " + ftype;
                }
            }
            if (fieldList.length > 0)
                sql += "create table " + tableName + " (\n" +
                        fieldList + ")";
            if (sql.length > 0)
                $("#at-sql").val(sql);
        }
        function buildTableFields(updateSql) {
            $("#add-table-fields").empty();
            // start i at the children count + 1
            var i = document.getElementById("add-table-fields").children.length;
            // first look for an update statement with the right tags to parse
            var pos1 = -1;
            var pos2 = -1;
            var posEq = -1;
            var posWhere = -1;
            var posSet = -1;
            var tableName = "";
            var fieldName = "";
            var sql = "";
            if (updateSql && updateSql.length > 0) {
                sql = updateSql.toUpperCase();
                pos1 = sql.indexOf("UPDATE ");
                if (pos1 != -1) {
                    posSet = sql.indexOf(" SET ");
                    // if we have a update and set then the table name is what is between the 2
                    if (posSet != -1) {
                        tableName = sql.substring(pos1 + 7, posSet).trim();
                        $("#at-table-name").val(tableName);
                        // pull the first parameter from the where first since it is most likely the unique key if there
                        posWhere = sql.indexOf(" WHERE ", posSet);
                        if (posWhere != -1) {
                            pos1 = posWhere + 6;
                            posEq = sql.indexOf("=", pos1);
                            if (posEq != -1) {
                                fieldName = sql.substring(pos1, posEq).trim();
                                addTableField(fieldName, 'varchar(50)');
                            }
                        }
                        // if we have an = the first parameter is everything between the set and =
                        pos1 = posSet + 4;
                        posEq = sql.indexOf("=", posSet)
                        while (posEq != -1 && posWhere > posEq) {
                            fieldName = sql.substring(pos1, posEq).trim();
                            addTableField(fieldName, 'varchar(50)');
                            pos1 = sql.indexOf(",", posEq);
                            if (pos1 != -1)
                            	pos1 += 1;
                            posEq = sql.indexOf("=", posEq + 1);
                        }
                    }
                }
            }
            buildCreateTableSql();
        }
        function addTableField(name, type) {
             // start i at the children count + 1
            var i = document.getElementById("add-table-fields").children.length + 1;
            $('<tr><td><input type="text" class="text ui-widget-content ui-corner-all" name="at-field-name' + i + '" id="at-field-name' + i + '" value="' + name + '" style="width: 135px;" /></td><td><input type="text" class="text ui-widget-content ui-corner-all" name="at-field-type' + i + '" id="at-field-type' + i + '" value="' + type + '" style="width: 135px;" onBlur="buildCreateTableSql();" /></td></tr>').appendTo("#add-table-fields");
            return false;
        }
        function downloadConfigurations() {
            window.location.href = '<%=request.getContextPath()%>/_system/configurations/download?filter=' + $("#fltr-categories").val();
        }
        function uploadConfigurations() {
            document.getElementById('file-upload-form').action='<%=request.getContextPath()%>/_system/configurations/upload';
            $('#file-upload-dialog').dialog('open');
        }
        function downloadConnections() {
            window.location.href = '<%=request.getContextPath()%>/_system/connections/download';
        }
        function uploadConnections() {
            document.getElementById('file-upload-form').action='<%=request.getContextPath()%>/_system/connections/upload';
            $('#file-upload-dialog').dialog('open');
        }

        // problem with jquery ui newest version; panel does not work anymore; making myself
        function showPanel(panelName) {
            $(".panel").hide();
            $('#' + panelName).show();
        }
    </script>

</head>
<body>
<div id="bodycontainer">

    <div id="headersection">
        <div class="innertube">
            <h1>Data Services Console  (BETA)</h1>
        </div>
    </div>

    <!-- file upload dialog -->
    <div id="file-upload-dialog" title="File Upload">
        <form id="file-upload-form" method="post" enctype="multipart/form-data" action="<%=request.getContextPath()%>/_system/configurations/upload">
            <input name="file" id="file" size="32" type="file" /><br />
            <input type="submit" name="action" value="Upload" /><br />
            <iframe id="upload_target" name="upload_target" src="" style="width:280px;height:50px;border:1px solid #ccc;"></iframe>
        </form>
    </div>

    <!-- add connection dialog -->
    <div id="add-connection-dialog" title="Add Connection">
        <form id="add-connection-form" method="POST" action="<%=request.getContextPath()%>/_system/connections">
        <fieldset>
            <label for="add-connection-name">Name</label>
            <input type="text" id="add-connection-name" name="name" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            <label for="add-connection-type">Connection Type</label>
            <select id=add-connection-type name=type onchange="showConnectionTypeFields($(this).val(), '#add-connection-');">
                <option value="jdbc">JDBC Connection</option>
                <option value="jndi">JNDI Connection (container managed)</option>
            </select>
            <div id="add-connection-jdbc-fields">
                <label for="add-connection-driver">Driver</label>
                <input type="text" id="add-connection-driver" name="driver" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="add-connection-url">URL</label>
                <input type="text" id="add-connection-url" name="url" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="add-connection-login">Login</label>
                <input type="text" id="add-connection-login" name="login" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="add-connection-password">Password</label>
                <input type="text" id="add-connection-password" name="password" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            </div>
            <div id="add-connection-jndi-fields">
                <label for="add-connection-jndi-name">Reference Name</label>
                <input type="text" id="add-connection-jndi-name" name="jndi-name" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="add-connection-jndi-context">Context</label>
                <input type="text" id="add-connection-jndi-context" name="jndi-context" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            </div>
            <label for="add-connection-description">Description</label>
            <input type="text" id="add-connection-description" name="description" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
        </fieldset>
        </form>
    </div>

    <!-- edit connection dialog -->
    <div id="edit-connection-dialog" title="Edit Connection">
        <form id="edit-connection-form" method="PUT" action="<%=request.getContextPath()%>/_system/connections">
        <fieldset>
            <label for="edit-connection-name">Name</label>
            <input type="text" id="edit-connection-name" name="name" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            <label for="edit-connection-type">Connection Type</label>
            <select id=edit-connection-type name=type onchange="showConnectionTypeFields($(this).val(), '#edit-connection-');">
                <option value="jdbc">JDBC Connection</option>
                <option value="jndi">JNDI Connection (container managed)</option>
            </select>
            <div id="edit-connection-jdbc-fields" name="edit-connection-jdbc-fields">
                <label for="edit-connection-driver">Driver</label>
                <input type="text" id="edit-connection-driver" name="driver" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="edit-connection-url">URL</label>
                <input type="text" id="edit-connection-url" name="url" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="edit-connection-login">Login</label>
                <input type="text" id="edit-connection-login" name="login" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="edit-connection-password">Password</label>
                <input type="text" id="edit-connection-password" name="password" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            </div>
            <div id="edit-connection-jndi-fields">
                <label for="edit-connection-jndi-name">Reference Name</label>
                <input type="text" id="edit-connection-jndi-name" name="jndi-name" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
                <label for="edit-connection-jndi-context">Context</label>
                <input type="text" id="edit-connection-jndi-context" name="jndi-context" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            </div>
            <label for="edit-connection-description">Description</label>
            <input type="text" id="edit-connection-description" name="description" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            <input type="hidden" id="edit-connection-id" name="id"/>
        </fieldset>
        </form>
    </div>

    <div id="add-table-confirm" title="Add Table?">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><span id="add-table-msg">Detected table does not exist. Would you like to create it?</span></p>
    </div>

    <div id="add-table-dialog" title="Build Table">
        <button id="add-table-field_button" onclick="addTableField('','','');">Add Field</button>
        <label for="at-table-name">Table Name</label> <input id="at-table-name" style="width: 135px;" class="text ui-widget-content ui-corner-all" />
		<table style="margin-left: 7px;">
			<thead><tr><th>Name</th><th>Type [(size)]</th></tr></thead>
			<tbody id="add-table-fields">
			<%--<tr><td><input style="width: 115px;"/></td><td><input style="width: 115px;"/></td><td><input style="width: 60px;"/></td></tr>--%>
			</tbody>
		</table>
        <form id="add-table-form" method="POST" action="<%=request.getContextPath()%>/_system/sql/execute">
        <fieldset>
            <label for="at-con">Connection</label>
            <select id=at-con name="$con">
                <option value="default">default</option>
            </select>
            <label for="at-sql">SQL</label>
            <textarea rows="3" cols="49" id="at-sql" name="$sql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
        </fieldset>
        </form>
    </div>

    <div id="del-connection-confirm" title="Delete Connection?">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>This connection will be permanently deleted and cannot be recovered. Are you sure?</p>
    </div>

    <div id="add-configuration-dialog" title="Add Configuration">
        <form id="add-configuration-form" method="POST" action="<%=request.getContextPath()%>/_system/configurations">
        <fieldset>
            <label for="add-configuration-connection">Connection</label>
            <select id=add-configuration-connection name="connection">
                <option value="default">default</option>
            </select>

            <label for="add-configuration-path">Path<span class="help-text">(deploys to <%=request.getContextPath()%>/[your path])</span></label>
            <input type="text" id="add-configuration-path" name="path" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            <label for="add-configuration-querysql">Query SQL</label>
            <textarea rows="3" cols="49" id="add-configuration-querysql" name="querysql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
            <label for="add-configuration-insertsql">Insert SQL</label>
            <textarea rows="3" cols="49" id="add-configuration-insertsql" name="insertsql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
            <label for="add-configuration-updatesql">Update SQL</label>
            <textarea rows="3" cols="49" id="add-configuration-updatesql" name="updatesql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
            <label for="add-configuration-deletesql">Delete SQL</label>
            <textarea rows="3" cols="49" id="add-configuration-deletesql" name="deletesql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
            <label for="add-configuration-keywords">Keywords<span class="help-text">(comma separated list)</span></label>
            <input type="text" id="add-configuration-keywords" name="keywords" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
            <%--<label for="acf-cached">Cached</label>--%>
            <%--<input type="checkbox" name="acf-cached" id="acf-cached" value="true" />--%>
        </fieldset>
        </form>
    </div>

    <div id="edit-configuration-dialog" title="Edit Configuration">
    <form id="edit-configuration-form" method="PUT" action="<%=request.getContextPath()%>/_system/configurations">
    <fieldset>
        <label for="edit-configuration-connection">Connection</label>
        <select id=edit-configuration-connection name="connection">
            <option value="default">default</option>
        </select>

        <label for="edit-configuration-path">Path<span class="help-text">(deploys to <%=request.getContextPath()%>/[your path])</span></label>
        <input type="text" id="edit-configuration-path" name="path" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
        <label for="edit-configuration-querysql">Query SQL</label>
        <textarea rows="3" cols="49" id="edit-configuration-querysql" name="querysql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
        <label for="edit-configuration-insertsql">Insert SQL</label>
        <textarea rows="3" cols="49" id="edit-configuration-insertsql" name="insertsql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
        <label for="edit-configuration-updatesql">Update SQL</label>
        <textarea rows="3" cols="49" id="edit-configuration-updatesql" name="updatesql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
        <label for="edit-configuration-deletesql">Delete SQL</label>
        <textarea rows="3" cols="49" id="edit-configuration-deletesql" name="deletesql" class="text ui-widget-content ui-corner-all" style="width: 98%;"></textarea>
        <label for="edit-configuration-keywords">Keywords<span class="help-text">(comma separated list)</span></label>
        <input type="text" id="edit-configuration-keywords" name="keywords" class="text ui-widget-content ui-corner-all" style="width: 98%;"/>
        <%--<label for="ecf-cached">Cached</label>--%>
        <%--<input type="checkbox" name="ecf-cached" id="ecf-cached" value="true" />--%>
        <input type="hidden" id="edit-configuration-id" name="id"/>
    </fieldset>
    </form>
    </div>

    <!-- planning to place the sql as properties on the div -->
    <!-- will interrogate the div for question marks to determine properties and build inputs based on selection -->
    <div id="test-configuration-dialog" title="Test Configuration">
        <form id="test-configuration-form" method="POST" >
        <fieldset>
            <!-- insert dynamic form fields here -->
            <div id="test-configuration-parameters">

            </div>
            <label for="test-configuration-action">Action</label>
            <select id="test-configuration-action" name="$action" onchange="buildTestConfigurationParameters();">
            </select>
            <!-- test info should be replaced by javascript record properties -->
            <p id="test-configuration-info"></p>
        </fieldset>
        </form>
    </div>

    <div id="test-configuration-results-dialog" title="Test Configuration Results">
        <pre id="test-configuration-results">
        </pre>
    </div>

    <div id="del-configuration-confirm" title="Delete Configuration?">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>This configuration will be permanently deleted and cannot be recovered. Are you sure?</p>
    </div>

    <div id="contentsection">
        <div id="contentcolumn">
            <div class="innertube">
                <div id="configurations" class="panel">
                    <div class="toolbar">
                        <button class="add_icon" onclick="$('#add-configuration-dialog').dialog('open');">Add Configuration</button>
                    </div>
                    <div id="configuration_list">
                    </div>
                </div>
                <div id="connections" class="panel">
                    <div class="toolbar">
                        <button class="add_icon" onclick="showConnectionTypeFields($('#add-connection-type').val(), '#add-connection-');$('#add-connection-dialog').dialog('open');">Add Connection</button>
                    </div>
                    <div id="connection_list">
                        <div class="ui-list-content-block">
                            Default H2 Connection
                        </div>
                    </div>
                </div>
                <%--<div id="management" class="panel">--%>
                    <%--<div class="ui-content-block">--%>
                        <%--TODO: build dashboard on metrics collected from configuration runs<br>--%>
                        <%--NOTE: may want to open another real-time performance page that uses websockets to get real time--%>
                        <%--usage data (pause) during high peaks etc.--%>
                        <%--TODO: build in package management--%>
                    <%--</div>--%>
                <%--</div>--%>
            </div>
        </div>
    </div>

    <div id="leftcolumn">
        <div class="innertube">
            <div id="accordion">
                <h3 onclick="showPanel('configurations');"><a href="#">Configurations</a></h3>
                <div>
                    Filter to:<br>
                    <select id=fltr-categories onchange="filterConfigurations();">
                        <option value="*">all</option>
                    </select><br>
                    <br>
                    <%--<a class="configurationDownload" href="/ds/_system/boom">Download Boom</a><br>--%>
                    <button onclick="downloadConfigurations();">Package</button><br>
                    <button onclick="uploadConfigurations();">Deploy</button><br>
                    <br>
                    <a href="#" onclick="$('.info_content').toggle();">info...</a><br>
                    <div class="info_content">
                        <p class="nav_block">
                        Configurations essentially map SQL Statements to a URL; allowing data queries and manipulation.
                        Currently, data is returned as JSON only and parameters are passed in order so be sure to place
                        fields in the correct position on the form before submitting. Each operation by default is mapped
                        to an HTTP method, however, this can be over-ridden for GET and POST by passing the desired
                        $action = [query, insert, update, delete].  Keywords
                        are intended for filtering and packaging and are comma separated values. Note that while you can
                        use the configuration to query, add, edit and delete data you must create your tables manually.
                        </p>
                        <p class="nav_block">
                        While complex, This console is a good example of how an HTML page can use AJAX to call data
                        services until I can add some simple tutorials.
                        </p>
                    </div>
                    <!-- groups will be generated here when initialized via ajax -->
                    <%--TODO: add ability to cache result and indicator in the configuration list<br>--%>
                    <%--TODO: add indicator for list items that are in error and move them to the top<br>--%>
                    <%--TODO: add indicator for list items that are beyond a given variance and move them to the top (sort)<br>--%>
                </div>
                <h3 onclick="showPanel('connections');"><a href="#">Connections</a></h3>
                <div>
                    <button onclick="downloadConnections();">Package</button><br>
                    <button onclick="uploadConnections();">Deploy</button><br>
                    <br>
                    <a href="#" onclick="$('.info_content').toggle();">info...</a><br>
                    <div class="info_content">
                        <p class="nav_block">
                        The default H2 connection is a file based database on the local machine.  You can edit
                        the default connection to point to an external location for the default configuration
                        and connection storage.  This can be helpful when you are dealing with clustered or load
                        balanced application servers / databases.  It is advised to export a package of all the current
                        connections and configurations before making changes to the default connection.
                        </p>
                        <p class="nav_block">
                        Additional JDBC or JNDI configurations can be created for your data connections as needed.
                        </p>
                    </div>
                </div>
                <%--<h3 onclick="showPanel('management');"><a href="#">Enterprise Dashboard</a></h3>--%>
                <%--<div>--%>
                    <%--<div class="nav_content">--%>
                        <%--<p class="nav_block">--%>
                        <%--The idea is to display a dashboard to better see any performance issues with the configured--%>
                        <%--data services.--%>
                        <%--</p>--%>
                        <%--<ul>--%>
                            <%--<li>Max Execution Time</li>--%>
                            <%--<li>Max Variance</li>--%>
                            <%--<li>Top 3 statements (used and execution time)</li>--%>
                            <%--<li>SQL Exceptions during configuration and execution (error log)</li>--%>
                        <%--</ul>--%>
                        <%--TODO: consider button to export all data in one bundle (archive)<br>--%>
                        <%--TODO: implement patches / releases (export and import bundles for specific items)<br>--%>
                        <%--TODO: consider integration to version control--%>
                    <%--</div>--%>
                <%--</div>--%>
            </div>

        </div>
    </div>

    <div id="footersection"></div>

</div>
</body>
</html>
