package com.unit11apps.drawing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.unit11apps.circusletters.R;
import com.unit11apps.drawing.LetterPointData.LetterPoint;
import com.unit11apps.drawing.LetterPointData.Segment;
import com.unit11apps.drawing.TokenData.Token;
import com.unit11apps.drawing.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class DrawingActivity extends Activity {
	DrawingView dv;   

	private JSONObject letterData;
	private JSONObject letterDataConfig;
	private JSONObject tokenList;
	private boolean showStars;
	private double pointRadius;
	private double scaleFactor;
	private Button backButton;
	private Button clearButton;
	private Button submitButton;
	private Button demoButton;
	private ImageView image;
	private ImageView pointerImageView;
	
	private int selectedSegmentIndex = 0;
	private int selectedSegmentPoints = 0;
	
	private float oldPointerX = 0;
	private float oldPointerY = 0;
	private double mysteriousPointerLeft;
	private double mysteriousPointerTop;
	private float letterOffsetLeft;
	private float letterOffsetTop;
	
	private boolean enabled;
	
	private String currentLetter = "";
	
	private Bitmap letterBitmap;
	private boolean startWithDemo = false;
	private boolean audioDemo = false;
	private String demoAudioFileName = "";
	private String strokeColor = "#8836C7";
	private boolean exiting = false;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private TokenData tokenData;
	
	public JSONObject getLetterData()
	{
		return letterData;
	}
	
	public JSONObject getLetterDataConfig()
	{
		return letterDataConfig;
	}
	
	public double getPointRadius()
	{
		return pointRadius;
	}
	
	public double getScaleFactor()
	{
		return scaleFactor;
	}
	
	protected ImageView tokenA;
	protected ImageView tokenB;
	protected ImageView tokenC;
	protected ImageView tokenD;
	protected ImageView tokenE;
	protected ImageView tokenF;

	protected int failHelpThreshold;
	protected boolean submitOnSuccess;
	protected int successesRequiredForSubmit;
	protected int stars;
	private boolean submitOnFullTokens;
	private boolean playLetterSoundOnEnter;
	private boolean playLetterSoundOnCorrect;
	private String characterImageFileName = "";
	private String characterAudioFileName = "";
	
	private int feedbackTopOffset = 340;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		logHeap();

		//Get the letter data JSON
		String newString;
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras == null) {
		        newString= null;
		    } else {
		        newString= extras.getString("letterData");
		    }
		} else {
		    newString= (String) savedInstanceState.getSerializable("letterData");
		}
		
		JSONObject args;
		
		//Extract the data from the JSON
		try {
			args = new JSONObject(newString);
			
			letterData = args.getJSONObject("letterData");
			letterDataConfig = args.getJSONObject("letterDataConfig");
			pointRadius = args.getDouble("pointRadius");
			
			try {
				tokenList = args.getJSONObject("tokenData");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				startWithDemo = args.getBoolean("startWithDemo");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				audioDemo = args.getBoolean("audioDemo");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				demoAudioFileName = args.getString("demoAudioFileName");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			try {
				characterImageFileName = args.getString("characterImageFileName");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
				
			try {
				characterAudioFileName = args.getString("characterAudioFileName");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 	}
			
			try {
				strokeColor = args.getString("strokeColor");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//showStars
			try {
				showStars = args.getBoolean("showStars");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//submit on full tokens
			try {
				submitOnFullTokens = args.getBoolean("submitOnFullTokens");
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				failHelpThreshold = args.getInt("failHelpThreshold");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				submitOnSuccess = args.getBoolean("submitOnSuccess");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				successesRequiredForSubmit = args.getInt("successesRequiredForSubmit");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				stars = args.getInt("stars");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				playLetterSoundOnEnter = args.getBoolean("playLetterSoundOnEnter");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				playLetterSoundOnCorrect = args.getBoolean("playLetterSoundOnCorrect");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(tokenList != null)
		{
			//Generate the token data
			tokenData = new TokenData(tokenList, this);
		}

	    setContentView(R.layout.activity_drawing);
	    
	    dv = (DrawingView) findViewById(R.id.drawingCanvasView);

		clearButton = (Button) findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				if(enabled)
				{
					pointerImageView.setVisibility(View.GONE);
					dv.reset();
				}
			}
 
		});
		
		submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				if(enabled)
				{
					dv.submit();
				}
			}
 
		});
		
		backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				returnWithResult(false);
			}
 
		});
		
		demoButton = (Button) findViewById(R.id.demoButton);
		demoButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				if(enabled)
				{
					startDemo();
				}
			}
 
		});

		//get the letter data
        try {
			JSONObject letterData = getLetterData();
			
			currentLetter = letterData.getString("character");
			
			Log.d("DrawingActivity",currentLetter);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String characterImageFileNameToUse = currentLetter;
        
        if(characterImageFileName != null && characterImageFileName.length() > 0)
        {
        	characterImageFileNameToUse = characterImageFileName;
        }
        
        int id = getResources().getIdentifier(characterImageFileNameToUse, "drawable", getApplicationContext().getPackageName());
        
        image = (ImageView) findViewById(R.id.imageView1);
        
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inPurgeable = true;
        
        letterBitmap = BitmapFactory.decodeResource(getResources(), id, o);
        
        image.setImageBitmap(letterBitmap);
		
        dv.initialize(this, strokeColor);
        
        float scaleFactor = dv.getScaleFactor();

    	mysteriousPointerTop = dv.getMysteriousTop() -5;
    	mysteriousPointerLeft = dv.getMysteriousLeft() + 10;
    	
    	letterOffsetLeft = dv.getLetterOffsetLeft();
    	letterOffsetTop = dv.getLetterOffsetTop();
    	
		pointerImageView = (ImageView) findViewById(R.id.pointer);
		
		logHeap();
				
		if(tokenData != null)
		{
			//TOKENS
			tokenA = (ImageView)findViewById(R.id.tokenA);
			tokenB = (ImageView)findViewById(R.id.tokenB);
			tokenC = (ImageView)findViewById(R.id.tokenC);
			tokenD = (ImageView)findViewById(R.id.tokenD);
			tokenE = (ImageView)findViewById(R.id.tokenE);
			tokenF = (ImageView)findViewById(R.id.tokenF);
			
			ArrayList<Token> tokens = tokenData.getTokens();
			
			tokens.get(0).setImageView(tokenA);
			tokens.get(1).setImageView(tokenB);
			tokens.get(2).setImageView(tokenC);
			tokens.get(3).setImageView(tokenD);
			tokens.get(4).setImageView(tokenE);
			tokens.get(5).setImageView(tokenF);
			
			updateTokens();
		}
		
		if(showStars)
		{
	    	updateStars();
		}
		
		if(startWithDemo)
		{
			startDemoOnEnter();
		}
		else
		{
			enabled = true;
		}
		
		if(!startWithDemo && playLetterSoundOnEnter)
		{
			playLetterSoundOnEnterTimer();
		}
	}
	
	public static boolean isIntegerParseInt(String str) {
	try {
		Integer.parseInt(str);
		return true;
	} 
	catch (NumberFormatException nfe) {}
		return false;
	}
	
	private class ResultData
	{
		@Expose public TokenData tokenData;
		@Expose public int stars;
		@Expose public boolean success;
	}

	public void returnWithResult(boolean result) 
	{
		if(exiting)
		{
			return;
		}
		
		exiting = true;
		
		ResultData resultData = new ResultData();
		
		resultData.tokenData = tokenData;
		resultData.stars = stars;
		resultData.success = result;
		
		Intent returnIntent = new Intent();
		
		//Serialize the token data
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String json = gson.toJson(resultData);
		
		returnIntent.putExtra("JSONResult",json);
		setResult(RESULT_OK,returnIntent);
		
		finish();

		recycleBitmaps();

        System.gc();
		
		overridePendingTransition(R.xml.jump_in,R.xml.jump_out);
	}
	
	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public int getFailHelpThreshold() {
		return failHelpThreshold;
	}

	public void setFailHelpThreshold(int failHelpThreshold) {
		this.failHelpThreshold = failHelpThreshold;
	}

	public int getSuccessesRequiredForSubmit() {
		return successesRequiredForSubmit;
	}

	public void setSuccessesRequiredForSubmit(int successesRequiredForSubmit) {
		this.successesRequiredForSubmit = successesRequiredForSubmit;
	}

	public boolean isSubmitOnSuccess() {
		return submitOnSuccess;
	}

	public void setSubmitOnSuccess(boolean submitOnSuccess) {
		this.submitOnSuccess = submitOnSuccess;
	}

	void setMainBackground(Drawable d)
	{
		((FrameLayout)findViewById(R.id.drawing_content)).setBackgroundDrawable(d);
	}
	
	/**
	 * Touch listener to use for in-layout UI
	 */
	View.OnTouchListener mTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			final int X = (int) motionEvent.getRawX();
		    final int Y = (int) motionEvent.getRawY();
		    
		    Log.d("com.unit11apps.SVT", "Move Coords : x : " + X + " y : " + Y);
		    
			return true;
		}
	};
	
	 private class FeebackCompleteListener implements AnimationListener{

	        @Override
	        public void onAnimationEnd(Animation animation) {
	        	//enabled = true;
	        }

	        @Override
	        public void onAnimationRepeat(Animation animation) {
	        }

	        @Override
	        public void onAnimationStart(Animation animation) {
	        }

	    }
	
    
    private class MyAnimationListener implements AnimationListener{
    	
    	View mView;
    	
    	public MyAnimationListener(View view)
    	{
    		mView = view;
    	}

        @Override
        public void onAnimationEnd(Animation animation) {
        	
        	Resources r = getResources();
    		
    		float tpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, feedbackTopOffset, r.getDisplayMetrics());
        	
        	//show the feedback
        	TranslateAnimation newAnimation = new TranslateAnimation(0, 0, -tpx, 0);
        	newAnimation.setDuration(500);
        	newAnimation.setFillAfter(false);
        	newAnimation.setStartOffset(1000);
        	
        	newAnimation.setAnimationListener(new FeebackCompleteListener());
        	
        	mView.clearAnimation();

    		ImageView feedbackImage = (ImageView) mView;
    		feedbackImage.startAnimation(newAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
    
    public void feedbackFinished(boolean success)
    {
    	boolean earnedEnoughStars = true;
    	
    	//check if written required number of times
    	if((showStars)&&(stars < 3))
    	{
    		earnedEnoughStars = false;
    	}
    	
    	boolean doReturn = false;
    	
    	if(success)
    	{
    		if(submitOnFullTokens && tokenData.full())
        	{
        		doReturn = true;
        	}
    		else if(!submitOnFullTokens && (submitOnSuccess)&&(earnedEnoughStars))
    		{
    			doReturn = true;
    		}
    	}
    	
    	if(doReturn)
    	{
    		returnWithResult(true);
    	}
    }
    
    protected void updateStars()
    {
    	ImageView star;
    	
    	star = (ImageView) findViewById(R.id.star1);
    	
    	if(stars >= 1)
    	{
    		//set the gold star
        	star.setImageResource(R.drawable.star_gold);
    	}
    	else
    	{
    		star.setImageResource(R.drawable.star_grey);
    	}
    	
		star = (ImageView) findViewById(R.id.star2);
		
    	if(stars >= 2)
    	{
    		//set the gold star
        	star.setImageResource(R.drawable.star_gold);
    	}
    	else
    	{
    		star.setImageResource(R.drawable.star_grey);
    	}
    	
    	star = (ImageView) findViewById(R.id.star3);
    	
    	if(stars >= 3)
    	{
    		//set the gold star
        	star.setImageResource(R.drawable.star_gold);
    	}
    	else
    	{
    		star.setImageResource(R.drawable.star_grey);
    	}
    }
    
    protected void hideFeedbackImages()
    {
    	ImageView feedbackImageViewTick = (ImageView) findViewById(R.id.tick);
    	feedbackImageViewTick.clearAnimation();
    	feedbackImageViewTick.setVisibility(View.GONE);
    	
    	ImageView feedbackImageViewCross = (ImageView) findViewById(R.id.cross);
    	feedbackImageViewCross.clearAnimation();
    	feedbackImageViewCross.setVisibility(View.GONE);
    }
    
    protected void feedback(boolean result)
    {
    	enabled = false;
    	
    	ImageView feedbackImageView;
    	int soundResourceId;
    	
    	if(result)
    	{
    		feedbackImageView = (ImageView) findViewById(R.id.tick);
    		
    		soundResourceId = R.raw.success1;
    		
    		if(showStars)
    		{
    			//increment the counter
    	    	stars++;
    			
    			updateStars();
    		}
    		
    		if(tokenData != null)
    		{
    			tokenData.awardToken();
    		}
    		
            /*
             * if(playLetterSoundOnCorrect)
    		{
    			playLetterSound();
    		}
             */
    	}
    	else
    	{
    		feedbackImageView = (ImageView) findViewById(R.id.cross);
    		
    		soundResourceId = R.raw.fail;
    	}
    	
    	//show the feedback
    	feedbackImageView.setVisibility(View.VISIBLE);
    	
    	Resources r = getResources();
    	
    	float tpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, feedbackTopOffset, r.getDisplayMetrics());
    	
    	//show the feedback
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -tpx);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setAnimationListener(new MyAnimationListener(feedbackImageView));

		feedbackImageView.startAnimation(animation);
		
		//play the sound
		MediaPlayer mp = MediaPlayer.create(this, soundResourceId);
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }

        });   
        mp.start();
    }
    
    public void playLetterSound()
    {
    	String letterSoundAudioFileNameToUse = currentLetter+"_name";
    	
    	if(characterAudioFileName.length() > 0)
    	{
    		letterSoundAudioFileNameToUse = characterAudioFileName;
    	}
    	
    	int soundResourceId = getResources().getIdentifier(letterSoundAudioFileNameToUse, "raw", getApplicationContext().getPackageName());
    	
    	//play the sound
		MediaPlayer mp = MediaPlayer.create(this, soundResourceId);
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }

        });   
        mp.start();
    }
    
    public void playLetterSoundOnEnterTimer()
   	{
       	Timer myTimer = new Timer();
       	
   		myTimer.schedule(new TimerTask() {			
   			@Override
   			public void run() {
   				playLetterSound();
   			}
   			
   		}, 1000);
       }
    
    public void startDemoOnEnter()
	{
    	Timer myTimer = new Timer();
    	
		myTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				DoDemo();
			}
			
		}, 500);
    }
    
    private void DoDemo()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		runOnUiThread(ReadyForDemo);
	}
    
    
    private Runnable ReadyForDemo = new Runnable() {
		public void run() {
		
			startDemo();
		}
	};
	
	public void startDemo()
	{
		enabled = false;
		
		hideFeedbackImages();
		
		dv.reset();
		
		//hide the dv
		dv.setVisibility(View.GONE);
		
		oldPointerX = 0;
		oldPointerY = 0;
		
		dv.clearCanvas();
		
		preparePointer();
		
		movePointer(0);
	}
	
	protected void preparePointer()
	{
		//show it 
		pointerImageView.setVisibility(View.VISIBLE);
		
		Resources r = getResources();
		
		float wpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, r.getDisplayMetrics());
		float hpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 800, r.getDisplayMetrics());
		
		int pointerImageWidth = (int)(wpx);//;
		int pointerImageHeight = (int)(hpx);//;
		
		LayoutParams params = new LayoutParams(
				pointerImageWidth,      
				pointerImageHeight
		);
		
		int topMargin = (int)(dv.getDrawingAreaTop());
		int leftMargin = -pointerImageWidth + (int)(dv.getDrawingAreaLeft());
		
		params.setMargins(leftMargin, topMargin, 0, 0);
		pointerImageView.setLayoutParams(params);
		pointerImageView.requestLayout();
		
		//audio
		if(audioDemo)
		{
			int soundResourceId;
			
			String demoAudioFileNameToUse = currentLetter;
			
			if(demoAudioFileName.length() > 0)
			{
				demoAudioFileNameToUse = demoAudioFileName;
			}
			
			soundResourceId = getResources().getIdentifier(demoAudioFileNameToUse, "raw", getApplicationContext().getPackageName());
			
			//play the sound
			MediaPlayer mp = MediaPlayer.create(this, soundResourceId);
	        mp.setOnCompletionListener(new OnCompletionListener() {

	            @Override
	            public void onCompletion(MediaPlayer mp) {
	                // TODO Auto-generated method stub
	                mp.release();
	            }

	        });   
	        mp.start();
		}
	}
	
	private class PointerAnimationListener implements AnimationListener{
    	
        @Override
        public void onAnimationEnd(Animation animation) {
        	
        	movePointer(0);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
	
	//move the wand
    protected void movePointer(int startOffset)
    {
        float x=0,y=0;
        
        float drawingAreaHeight = dv.getDrawingAreaHeight();
        float drawingAreaWidth = dv.getDrawingAreaWidth(); //4:3 area
        
        LetterPointData letterData = dv.getLetterPointData();
        
        ArrayList<Segment> segments = letterData.getSegments();
        
        //check valid segments exist
        if(selectedSegmentIndex < segments.size())
        {
        	ArrayList<LetterPoint> points = segments.get(selectedSegmentIndex).getPoints();
        	
        	boolean moving = false;
        	
        	if(selectedSegmentPoints < points.size())
        	{
	        	LetterPoint thisPoint = points.get(selectedSegmentPoints);
	        	
	            //check it is a wand point and within the points length
	            if(thisPoint.wandPoint == true)
	            {
	                //calculate starting point and convert to pixels
	                x = (thisPoint.x * drawingAreaWidth) / 100;
	                y = (thisPoint.y * drawingAreaHeight) / 100;
	                
	                x += mysteriousPointerLeft + letterOffsetLeft;
	                y += mysteriousPointerTop + letterOffsetTop;
	                
	                //increment point counter
	                selectedSegmentPoints++;
	                
	                TranslateAnimation animation = new TranslateAnimation(oldPointerX, x, oldPointerY, y);
	        		animation.setDuration(200);
	        		animation.setFillAfter(true);
	        		animation.setInterpolator(new LinearInterpolator());
	        		animation.setAnimationListener(new PointerAnimationListener());
	        		animation.setStartOffset(startOffset);
	
	        		pointerImageView.startAnimation(animation);
	        		
	                oldPointerX = x;
	                oldPointerY = y;
	                
	                moving = true;
	            }
            }
           
        	if(!moving)
            {
                //increment the
                selectedSegmentIndex++;
                selectedSegmentPoints = 0;
                
                //call this again
                movePointer(500);
            }
        }
        else
        {
            //call finish letter to reset segments
            demoFinished();
        }
    }
    
    protected void reset()
    {
    	dv.reset();
		
		//hide the dv
		dv.setVisibility(View.VISIBLE);
		
		oldPointerX = 0;
		oldPointerY = 0;
		
		pointerImageView.clearAnimation();
		
		//hide it 
		pointerImageView.setVisibility(View.GONE);
		
		selectedSegmentIndex = 0;
        selectedSegmentPoints = 0;
        
        if(tokenData != null)
        {
        	tokenData.updateDisplay();
        }
    }
    
    protected void demoFinished()
    {
    	Timer myTimer = new Timer();
    	
		myTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				TimerMethod();
			}
			
		}, 1000);
    }
    
    private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		runOnUiThread(Timer_Tick);
	}
    
    
    private Runnable Timer_Tick = new Runnable() {
		public void run() {
		
			reset();
			enabled = true;
		}
	};
	
	public int getStatusBarHeight() {
	  int result = 0;
	  int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	  if (resourceId > 0) {
	      result = getResources().getDimensionPixelSize(resourceId);
	  }
	  return result;
	}
	
	public void onResume()
	{
		super.onResume();
		Log.d("DrawingActivity","RESUMING");
	}
	
	@Override
	public void onBackPressed(){
		String packageName = getApplicationContext().getPackageName();
		Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
		
		launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		launchIntent.putExtra("EXIT", true);
		startActivity(launchIntent);
		
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		
		finish();
		
		recycleBitmaps();

        System.gc();
		
		overridePendingTransition(R.xml.jump_in,R.xml.jump_out);
	}
	
	public static void logHeap() {
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize())/new Double((1048576));
        Double available = new Double(Debug.getNativeHeapSize())/1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize())/1048576.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        Log.d("tag", "debug. =================================");
        Log.d("tag", "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free)");
        Log.d("tag", "debug.memory: allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory()/1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory()/1048576))+ "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory()/1048576)) +"MB free)");
    }
	
	protected void updateTokens()
	{
		int id;
		
		tokenData.updateDisplay();
	}
	
	@Override
	protected void onDestroy()
	{
        super.onDestroy();
	}
	
	protected void recycleBitmaps()
	{
        recycleBitmapFromImageView(R.id.imageView1);
        
        //Drawable drawable = ((FrameLayout)(findViewById(R.id.drawing_content))).getBackground();
        
        //recycleBitmap(drawable);
        
        //recycleBitmapFromImageView(R.id.tick);
        
        //recycleBitmapFromImageView(R.id.cross);
        
        if(letterBitmap != null)
        {
        	letterBitmap.recycle();
        	letterBitmap = null;
        }
        
        dv.recycleBitmaps();

	    System.gc();
	}
	
	protected void recycleBitmapFromImageView(int id)
	{
		ImageView imageView = (ImageView)findViewById(id);
		
        Drawable drawable = imageView.getDrawable();
        
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            
            if(bitmap != null)
            {
            	bitmap.recycle();
            	bitmap = null;
            }
        }
	}
	
	protected void recycleBitmap(int id)
	{
		BitmapDrawable drawable = (BitmapDrawable)getApplicationContext().getResources().getDrawable(id);

        Bitmap bitmap = drawable.getBitmap();
        bitmap.recycle();
	}
	
	protected void recycleBitmap(Drawable drawable)
	{
        Bitmap bitmap = ((BitmapDrawable)(drawable)).getBitmap();
        bitmap.recycle();
	}
}
