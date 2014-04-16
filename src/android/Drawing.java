/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 */

package com.unit11apps.drawing;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import com.unit11apps.drawing.DrawingActivity;

import android.content.Context;
import android.content.Intent;

public class Drawing extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        if (action.equals("drawing")) {
        	cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Context context = cordova.getActivity()
                            .getApplicationContext();
                    Intent intent = new Intent(context, DrawingActivity.class);
                    cordova.getActivity().startActivity(intent);
                }
            });

            return true;
		}
		else {
		    status = PluginResult.Status.INVALID_ACTION;
		}
        
		callbackContext.sendPluginResult(new PluginResult(status, result));
        return true;
    }
}
