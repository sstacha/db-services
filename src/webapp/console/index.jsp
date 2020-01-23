<!DOCTYPE html>
<html lang="en" data-ng-app="console" data-ng-controller="AppController">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
<%--    <link rel="shortcut icon" href="../../assets/ico/favicon.ico">--%>

    <title>Database Services Console</title>

    <!-- GOOGLE FONTS -->
    <link href='//fonts.googleapis.com/css?family=PT+Sans+Narrow:700' rel='stylesheet' type='text/css'>
    <!-- UI CSS -->
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
    <!-- OUR CSS -->
<%--   	<link rel="stylesheet" href="console.css" type="text/css">--%>
    <!-- link rel="stylesheet" href="forms.css" type="text/css" -->

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="//oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

    <!-- global server side variables for our javascript to use -->
    <script type="text/javascript">
        window.base_url = "http://localhost:3000";
        window.ctx = "<%=request.getContextPath()%>";
    </script>
    <style>
        body {
            position: relative; /* For scrollyspy */
            padding-top: 50px; /* Account for fixed navbar */
            font-size: 1.2em;
            background-color: #f5f5f5;
        }
        .nav-bordered-rt {
            border-right: 1px dotted grey;
        }
        .main-bordered-top {
            border-top: 1px dotted grey;
        }
        .icon-large {
            font-size: 1.2em;
        }
        .nav-padded-top, .toolbar {
            padding-top: 10px;
        }
        .toolbar { padding-bottom: 10px; }
        .transparent {background-color: transparent;}
        .block-container {
            display: -moz-inline-stack;
            /*text-align: center;*/
            vertical-align: top;
            /*font-family: Verdana, Helvetica, Arial, sans-serif;*/
            /*font-size: 12px;*/
            /*line-height: 18px;*/
        }
        .brand {
            /*background: url(/images/logo.png) no-repeat left center;*/
            height: 38px;
            width: 80px;
            margin-top: 6px;
        }
        .alert {
            margin-top: 6px;
        }
        .full-page {margin: 0; width: 100%}
        .full-width {width: 100%;}
        .panel-link {padding:0;}
        .nav, .pagination, .carousel, .panel-title a, .clickable { cursor: pointer; }
        .id {font-weight: bold;}
        .field-label {padding-left: 20px;}
        pre {outline: 1px solid #ccc; padding: 5px; margin: 5px; }
		.string { color: green; }
		.number { color: darkorange; }
		.boolean { color: blue; }
		.null { color: magenta; }
		.key { color: red; }
    </style>
</head>

<body>

<section id="global-nav">
    <nav class="navbar navbar-inverse navbar-fixed-top">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">Database Services Console</a>
            </div>
            <div id="navbar" class="navbar-collapse collapse">
                <ul class="nav navbar-nav navbar-right">
                    <li class="dropdown">
                        <a href="#" id="settings-menu-item" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-cog
                        glyphicon-white icon-large"></i>&nbsp;<b class="caret-down"></b></a>
                        <ul class="dropdown-menu" role="menu">
                            <li role="presentation" class="dropdown-header">LOGGING</li>
                            <li role="presentation"><a href="#" data-ng-click="setRootLogLevel('debug')">Set to Debug</a></li>
                            <li role="presentation"><a href="#" data-ng-click="setRootLogLevel('info')">Set to Info</a></li>
                            <li role="presentation"><a href="#" data-ng-click="setRootLogLevel('warn')">Set to Warning</a></li>
                            <li role="presentation" class="dropdown-header" style="display:none;">PROFILE</li>
                            <li role="presentation" style="display:none;"><a href="#">Login</a></li>
                        </ul>
                    </li>
                </ul>
                <form class="navbar-form navbar-right">
                    <input type="text" class="form-control" placeholder="Filter..." data-ng-model="app.viewFilter[app.accordian.index]" />
                </form>
            </div>
        </div>
    </nav>
</section>



<section id="body">
    <div class="container full-page">
        <div class="row">
            <div class="col-md-3 col-sm-3 col-lg-3 nav-bordered-rt nav-padded-top" >
                <!-- BEGIN NEW ACCORDIAN CODE -->
                <div id="accordion" class="panel-group" >
                    <div class="panel panel-default" >
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a href="#configurations">Configurations</a>
                            </h4>
                        </div>
                        <div id="configurations" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <div class = nav_fixed>
                                    <span data-ng-show="app.viewFilter[0]">Path filtered to: {{app.viewFilter[0]}}</span><br>
                                    <span data-ng-show="app.tagFilter[0]">Tags filtered to: {{app.tagFilter[0]}}</span><br>
                                    <br>
                                    <!--<a class="configurationDownload" href="/ds/_system/boom">Download Boom</a><br>-->
                                    <button class="btn btn-default" data-ng-click="downloadConfigurations()">Package</button><br>
                                    <button class="btn btn-default" data-ng-click="uploadConfigurations()">Deploy</button><br>
                                    <br>
                                    <a href="#" data-toggle="collapse" data-target="#configuration_info" onclick="return false;">info...</a><br>
                                    <div id="configuration_info" class="panel-collapse collapse">
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
                                    <!--TODO: add ability to cache result and indicator in the configuration list<br>-->
                                    <!--TODO: add indicator for list items that are in error and move them to the top<br>-->
                                    <!--TODO: add indicator for list items that are beyond a given variance and move them to the top (sort)<br>-->
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a href="#connections">Connections</a>
                            </h4>
                        </div>
                        <div id="connections" class="panel-collapse collapse">
                            <div class="panel-body">
                                    <span data-ng-show="app.viewFilter[1]">Name filtered to: {{app.viewFilter[1]}}</span><br>
                                <p>
                                    <button class="btn btn-default" data-ng-click="downloadConnections()">Package</button><br>
                                    <button class="btn btn-default" data-ng-click="uploadConnections()">Deploy</button><br>
                                    <br>
                                    <a href="#" data-toggle="collapse" data-target="#connection_info" onclick="return false;">info...</a><br>
                                    <div id="connection_info" class="panel-collapse collapse">
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
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default" style="display:none;">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a href="#tables">Data</a>
                            </h4>
                        </div>
                        <div id="tables" class="panel-collapse collapse">
                            <div class="panel-body">
                                <p>
                                    <a href="#" data-toggle="collapse" data-target="#data_info" onclick="return false;">info...</a><br>
                                    <div id="data_info" class="panel-collapse collapse">
                                        <p class="nav_block">
                                            This area is the future location for simple data manipulation.  I hope to support additional
                                            package management; change management like the previous.  However, it is much more complex since
                                            database tables may already exist with data so you need migration scripts and such instead of
                                            the simple replace functionality of configurations and connections.
                                        </p>
                                        <p class="nav_block">
                                            Additionally, it is recommended to use your own database management tool.
                                        </p>
                                    </div>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-9 col-sm-9 col-lg-9 main-bordered-top">
                <!-- display alerts if we have any (dismissable) -->
                <div id="errors">
                    <div data-ng-repeat="alert in alerts">
                        <div class="alert alert-{{alert.type}} alert-dismissable">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                            {{alert.msg}}
                            <span ng-bind-html="alert.msg"></span>
                        </div>
                    </div>
                </div>
                <!-- placeholder for view insertion -->
                <section id="dynamic-fragment" data-ng-view></section>
            </div>
        </div>
    </div>
</section>
<section id="footer"></section>
<!-- reusable message box to use for all views -->
<div id="msg-box" class="modal fade" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">MessageBox | {{msgbox.header}}</h4>
            </div>
            <div class="modal-body">
                <p>
                    <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
                    <pre id="dlg-msg"></pre>
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<!-- moving the keyword or tag filter dialog and variable to the parent so it can be re-uesed on other views -->
<div id="tag-filter-dlg" class="modal fade" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="tag-configuration-dialog-label">Keyword Filter</h4>
            </div>
            <div class="modal-body">
                     <div class="form-group">
                        <label for="tag-filter-control">Tag Filter</label><span class="help-text">
                        (Note: Can be a boolean expression like tag1 tag2 !tag3.  
                        		Defaults to && instead of ||)
                        		</span>
                        <input class="form-control" type="text" id="tag-filter-control" name="tagfilter" data-ng-model="app.tagFilter[app.accordian.index]" autofocus/>
                    </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal">Ok</button>
            </div>
        </div>
    </div>
</div>

<!-- file upload dialog -->
<div id="file-upload-dialog" class="modal fade" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
    	<form id="file-upload-form" method="post" enctype="multipart/form-data" action="<%=request.getContextPath()%>/_system/configurations/upload">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" >{{app.upload_type | capitalize}} | Package Deployment</h4>
            </div>
            <div class="modal-body">
                     <div class="form-group">
                        <label for="file">Package File</label>
                        <input class="form-control" type="file" id="file" name="file" fileread="app.upload_file" autofocus/>
                    </div>
                    <div class="form-group">
                    	<!-- pre id="upload-status-msg"></pre -->
                    	<iframe id="upload_target" name="upload_target" src="" style="width:280px;height:50px;border:1px solid #ccc;"></iframe>
                    </div>
            </div>
            <div class="modal-footer">
            	<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="submit" class="btn btn-primary" name="action" value="Upload">Upload</button>
            </div>
        </div>
        </form>
    </div>
</div>


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="//code.jquery.com/jquery-2.1.3.min.js" type="text/javascript"></script>
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.3.13/angular.min.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.3.13/angular-route.min.js" type="text/javascript"></script>
<!-- script src="//cdnjs.cloudflare.com/ajax/libs/angular-ui-router/0.2.10/angular-ui-router.min.js" type="text/javascript"></script -->
<script src="spe-generic.js" type="text/javascript"></script>
<script src="spe-jquery-extensions.js" type="text/javascript"></script>
<script src="console.js"></script>
</body>
</html>
