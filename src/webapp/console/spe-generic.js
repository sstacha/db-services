/*
	basic javascript operations like:
		- canceling events
		- sorting arrays
		- string prototypes for trimming and getting a value from the header
 */
function cancelEvent(event)
{
	if (event.stopPropagation){
		event.stopPropagation();
	}
	else if(window.event){
		window.event.cancelBubble=true;
	}
}

// sort function (applies to nested stuff too
function sortArray (arr, prop) {
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

String.prototype.tidyTrim = function() {
	var s = this.replace(/(^\s*)|(\s*$)/gi,"");
	s = s.replace(/[ ]{2,}/gi," ");
	s = s.replace(/\n /,"\n");
	return s;
};

String.prototype.getValueByKey = function(k) {
//            var result = new RegExp(k + "=([^&]*)", "i").exec(this);
//            return result && decodeURIComponent(result[1]) || "";
	var p = new RegExp('\\b'+k+'\\b','gi');
	return this.search(p) != -1 ? decodeURIComponent(this.substr(this.search(p)+k.length+1).substr(0,this.substr(this.search(p)+k.length+1).search(/(&|;|$)/))) : "";
};
// add trim functionality to string class if browser does not support (IE doesn't)
if(typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, '');
	}
}
// counts the number of occurrences in the string of the 's1' string supplied
String.prototype.count = function(s1) {
	var m = this.match(new RegExp(s1.toString().replace(/(?=[.\\+*?[^\]$(){}\|])/g, "\\"), "g"));
	return m ? m.length:0;
}

if ( typeof String.prototype.startsWith != 'function' ) {
  String.prototype.startsWith = function( str ) {
    return str.length > 0 && this.substring( 0, str.length ) === str;
  }
};

if ( typeof String.prototype.endsWith != 'function' ) {
  String.prototype.endsWith = function( str ) {
    return str.length > 0 && this.substring( this.length - str.length, this.length ) === str;
  }
};