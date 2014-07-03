/*-
 * Drawing Plugin for Android
 */

var exec = require("cordova/exec");

var Drawing = {
    drawing: function(drawingArgs) {
    	var serializedArgs = JSON.stringify(drawingArgs);
    	
        exec(function(){}, function(error){}, "Drawing", "drawing", [serializedArgs]);
    },
    
    returnFromPlugin : function(jsonData)
    {
    	var index = 0;
    	var thisPlayerToken;

		console.log("Returning From plugin with data:");
 		console.log(jsonData);  
 		
 		var returnData = JSON.parse(jsonData); 
 		
 		var tokenData = returnData.tokenData; 
 		
 		if(tokenData)
 		{
	 		console.log("updateTokensFromPlugin!");	
	    	
	    	for(var p=0; p<player.tokensList.length; p++)
	        {
	        	thisPlayerToken = player.tokensList[p];
	        	
	        	if(thisPlayerToken.letter == player.currentLetter)
	        	{
	        		break;
	        	}
	    	}
	 		
	 		tokensArr = tokenData.tokens;
	 		
	 		for(var i=0; i<tokensArr.length; i++)
	 		{
	 			var thisToken = tokensArr[i];
	 			
	 			if(thisToken.freeWritingTokenAwarded)
	 			{
	 				thisPlayerToken.freeSlots[i] = 1;
	 			}
	 			else
	 			{
	 				thisPlayerToken.freeSlots[i] = 0;
	 			}
	 			
	 			if(thisToken.strictWritingTokenAwarded)
	 			{
	 				thisPlayerToken.strictSlots[i] = 1;
	 			}
	 			else
	 			{
	 				thisPlayerToken.strictSlots[i] = 0;
	 			}
	 		}
	 	}
	 	
	 	//Success
	 	if(returnData.success)
	 	{
	 		writingManager.lastWritingSuccess = true;
    	}
    	else
    	{
    		writingManager.lastWritingSuccess = false;
    	}
    	
    	console.log("returnData.stars : " + returnData.stars);

		//TODO : Stars
		if(returnData.stars)
	 	{
	 		writingManager.noOfStars = returnData.stars;
    	}
    	else
    	{
    		writingManager.noOfStars = 0;
    	}
    	
    	console.log("writingManager.noOfStars : " + writingManager.noOfStars);
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
