
<!DOCTYPE html>
<html lang="en" data-ng-app="console" data-ng-controller="AppController">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="shortcut icon" href="../../assets/ico/favicon.ico">

    <title>Data Services Console</title>

    <!-- GOOGLE FONTS -->
    <link href='//fonts.googleapis.com/css?family=PT+Sans+Narrow:700' rel='stylesheet' type='text/css'>
    <!-- UI CSS -->
    <link rel=stylesheet href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.1/themes/smoothness/jquery-ui.css" type=text/css>
    <!-- OUR CSS -->
<%--   	<link rel=stylesheet href=console.css type=text/css>--%>

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="//oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script type="text/javascript">
        window.base_url = "<%=request.getContextPath()%>";
    </script>
    <style>
    </style>
</head>

<body>

<div id="bodycontainer">

    <div id="headersection">
        <div class="innertube">
            <h1>Data Services Console  (BETA)</h1>
        </div>
    </div>

    <div id="contentsection">
        <div id="contentcolumn">
            <div class="innertube">
                <!-- display alerts if we have any (dismissable) -->
                <div id="errors">
                    <div data-ng-repeat="alert in alerts">
                        <div class="alert alert-{{alert.type}} alert-dismissable">
                            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                            {{alert.msg}}
                        </div>
                    </div>
                </div>
                <!-- placeholder for view insertion -->
                <section id="dynamic-fragment" data-ng-view></section>
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


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="//code.jquery.com/jquery-1.11.0.min.js" type="text/javascript"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.min.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.12/angular.min.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.12/angular-route.min.js" type="text/javascript"></script>
<script src="console.js"></script>
</body>
</html>
