package com.unit11apps.drawing;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.Expose;
import com.unit11apps.circusletters.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

public class TokenData {
	@Expose protected ArrayList<Token> tokens;
	int currentTokenIndex = 0;
	DrawingActivity da;
	
	public ArrayList<Token> getTokens()
	{
		return tokens;
	}
	
	public TokenData(JSONObject rawTokenData, DrawingActivity DA)
	{
		da = DA;
		
		tokens = new ArrayList<Token>();
		
		//loop over and create the data
		try 
		{
			JSONArray freeWritingTokens = rawTokenData.getJSONArray("freeSlots");
			JSONArray strictWritingTokens = rawTokenData.getJSONArray("strictSlots");
			JSONArray images = rawTokenData.getJSONArray("images");
			
			int arrLen = freeWritingTokens.length();

			for (int i = 0; i < arrLen; i++) 
			{
				int free = freeWritingTokens.getInt(i);
				int strict = strictWritingTokens.getInt(i);
				
				int freeImage = -1;
				int strictImage = -1;
				
				if(free > 0)
				{
					currentTokenIndex++;
				}
				
				try 
				{
					freeImage = images.getJSONObject(i).getInt("freeModeImage");
					strictImage = images.getJSONObject(i).getInt("strictModeImage");
				}
				catch (JSONException e) {
					//No Problem
				}
				
				Token token = new Token(free, strict, freeImage, strictImage);
				
				tokens.add(token);
			}
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateDisplay()
	{
		for(int i=0; i<tokens.size(); i++)
		{
			Token thisToken = tokens.get(i);
			
			thisToken.updateDisplay();
		}
	}
	
	public void awardToken()
	{
		if(currentTokenIndex < 6)
		{
			Token thisToken = tokens.get(currentTokenIndex);
			
			thisToken.setFreeWritingTokenAwarded(true);
			
			fadeNext(currentTokenIndex);

			currentTokenIndex++;
		}
	}
	
	public void awardToken(int index)
	{
		Token thisToken = tokens.get(index);
			
		thisToken.setFreeWritingTokenAwarded(true);
		
		fadeNext(index);
	}
	
	public void fadeNext(int index)
	{
		Token thisToken = tokens.get(index);
			
		thisToken.swapDrawable();
	}
	
	public void recycleBitmaps()
	{
		for(int i=0; i<tokens.size(); i++)
		{
			Token thisToken = tokens.get(i);
			
			thisToken.recycleBitmaps();
		}
	}
	
	public boolean full()
	{
		if(currentTokenIndex == tokens.size())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public class Token
	{
		@Expose boolean freeWritingTokenAwarded;
		@Expose boolean strictWritingTokenAwarded;
		ImageView imageView;
		private TransitionDrawable trans;
		int currentDrawableId;
		int freeImageIndex;
		int strictImageIndex;
		String freeImageIndexString;
		String strictImageIndexString;
		Bitmap tokenBitmap;
		
		public Token(int free, int strict, int freeImageI, int strictImageI)
		{
			freeWritingTokenAwarded = (free == 1);
			strictWritingTokenAwarded = (strict == 1);	
			
			freeImageIndex = freeImageI;
			strictImageIndex = strictImageI;
			
			if(freeImageIndex >= 0)
			{
				freeImageIndexString = String.valueOf(freeImageI);
			}
			else
			{
				freeImageIndexString = "star";
			}
			
			if(strictImageIndex >= 0)
			{
				strictImageIndexString = String.valueOf(strictImageI);
			}
			else
			{
				strictImageIndexString = "star";
			}
		}
		
		public void setFreeWritingTokenAwarded(boolean b) {
			freeWritingTokenAwarded = b;
		}

		public void setImageView(ImageView iv)
		{
			imageView = iv;
		}
		
		public void updateDisplay()
		{
			int id;
			
			if(!freeWritingTokenAwarded)
			{
				id = da.getApplicationContext().getResources().getIdentifier("token_q", "drawable", da.getApplicationContext().getPackageName());
				currentDrawableId = id;
				
				imageView.setImageResource(id);
			}
			else if(freeImageIndexString == "star")
			{
				id = da.getApplicationContext().getResources().getIdentifier("token_star", "drawable", da.getApplicationContext().getPackageName());
				currentDrawableId = id;
				
				imageView.setImageResource(id);
			}
			else
			{
				id = da.getApplicationContext().getResources().getIdentifier("token_"+freeImageIndexString, "drawable", da.getApplicationContext().getPackageName());
				currentDrawableId = id;
				
				BitmapFactory.Options o = new BitmapFactory.Options();
		        o.inPurgeable = true;
		        
		        tokenBitmap = BitmapFactory.decodeResource(da.getResources(), id, o);
		        
		        imageView.setImageBitmap(tokenBitmap);
			}
		}
		
		public void swapDrawable()
		{
			int id = da.getApplicationContext().getResources().getIdentifier("token_"+freeImageIndexString, "drawable", da.getApplicationContext().getPackageName());
			
			BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inPurgeable = true;
	        
	        tokenBitmap = BitmapFactory.decodeResource(da.getResources(), id, o);
	        
	        //create drawable
	        BitmapDrawable bitmapDrawable = new BitmapDrawable(da.getResources(),tokenBitmap);

			BitmapDrawable[] drawables = {(BitmapDrawable)da.getApplicationContext().getResources().getDrawable(currentDrawableId), bitmapDrawable};
			
			trans = new TransitionDrawable(drawables);
			
			imageView.setImageDrawable(trans);
			trans.reverseTransition(1000);
			
			currentDrawableId = id;
		}
		
		public void recycleBitmaps()
		{
			if(tokenBitmap != null)
			{
				tokenBitmap.recycle();
				tokenBitmap = null;
			}
		}
	}
}
