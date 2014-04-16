/*-
 * Echo Plugin for Android
 */

var exec = require("cordova/exec");

var Echo = {
    play: function() {
    	alert("ECHO::play");
    	
        exec(function(){alert("SUCCESS");}, function(error){alert("error");}, "Echo", "echo", []);
    }
};

module.exports = Echo;

function dumpObject(obj)
{
	for(var i in obj)
	{
		alert(obj[i]);
	}
}