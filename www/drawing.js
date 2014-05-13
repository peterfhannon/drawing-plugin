/*-
 * Drawing Plugin for Android
 */

var exec = require("cordova/exec");

var Drawing = {
    drawing: function(drawingArgs) {
    	var serializedArgs = JSON.stringify(drawingArgs);
    	
    	console.log(serializedArgs);
    	
        exec(function(){alert("SUCCESS");}, function(error){alert("error");}, "Drawing", "drawing", [serializedArgs]);
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