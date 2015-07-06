/*
	jquery extensions for
		- adding passed constructor event functions to dialog (wrappers modal)
		- setting options from JSON data NOTE: assumes select element
 */
(function ( $ ) {
	$.fn.dlg = function (options) {
		// if options = 'show' or 'hide' then pass through the call
		if (options == 'show' || options == 'hide')
			this.modal(options);
		else {
			// default options for creating the dialog.
			var settings = $.extend({
				// These are the defaults.
				backdrop: "static",
				keyboard: true,
				show: false,
				buttons: {
				}
			}, options );

			// bind our function if one is passed; if we have any buttons passed search for the button to append the click function to
			this.find("button").each(function( index ) {
				var buttonText = $(this).html();
//					        alert( index + ": " + buttonText );
				if (settings.buttons[buttonText]) {
//						        alert("found buttonText: " + buttonText);
					$(this).click(settings.buttons[buttonText]);
				}
			});
			// pass through to modal creating the buttons click() fns if passed
			return this.modal(settings);
		}
	};
    $.fn.dlgData = function (obj) {
   		// if a valid object is passed search for any custom data-map attribute input elements and if they match a property set the value
   		if (obj) {
   //			alert ("setting up data for project id: " + JSON.stringify(obj, null, 4));
   //			var msg = "";
   			// using custom attribute data-map to get each property to fill
   			$(this).find(':input[data-map]').each(function(index) {
   //				msg += index + ': ' + $(this).attr('data-map') + "<br>";
   				// try and set the value to the property in the object that was passed
   				$(this).val(obj[$(this).attr('data-map')]);
   			});
   //			alert (msg);
   		}
   		else {
   			$(this).find(':input').each(function() {
   				switch(this.type) {
   					case 'password':
   					case 'select-multiple':
   					case 'select-one':
   					case 'text':
   					case 'textarea':
   						$(this).val('');
   						break;
   					case 'checkbox':
   					case 'radio':
   						this.checked = false;
   				}
   			});
   		}
   	};
	$.fn.setOptions = function (data, valueProperty, displayProperty) {
		if (this && $(this).is("select")) {
			$(this).empty();
			// we expect data to be a set of objects with passed property names above
			if (data && typeof(data) !== 'undefined' && data instanceof Array && data.length > 0) {
				var length = data.length, record = null;
				for (var i = 0; i < length; i++) {
					record = data[i];
					// skip if both properties do not exist on the object in question
					if (record[valueProperty] && record[displayProperty]) {
						this[0].options.add (new Option(record[displayProperty], record[valueProperty], false, false));
					}
				}
			}
		}
		return this;
	};

}( jQuery ));
