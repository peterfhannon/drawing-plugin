/*-
 * Drawing Plugin for Android
 */

var exec = require("cordova/exec");

var Drawing = {
    drawing: function() {
    	alert("Drawing::drawing");
    	
        exec(function(){alert("SUCCESS");}, function(error){alert("error");}, "Drawing", "drawing", []);
    }
};

module.exports = Drawing;

function dumpObject(obj)
{
	for(var i in obj)
	{
		alert(obj[i]);
	}
}