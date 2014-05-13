package com.unit11apps.drawing;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.unit11apps.circusletters.R;
import com.unit11apps.drawing.LetterPointData.LetterPoint;
import com.unit11apps.drawing.LetterPointData.Segment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
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
	DrawingView dv ;   

	private JSONObject letterData;
	private JSONObject letterDataConfig;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		//Extract the letter data from the JSON
		try {
			args = new JSONObject(newString);
			
			letterData = args.getJSONObject("letterData");
			letterDataConfig = args.getJSONObject("letterDataConfig");
			pointRadius = args.getDouble("pointRadius");
			scaleFactor = args.getDouble("scaleFactor");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    setContentView(R.layout.activity_drawing);
	    
	    dv = (DrawingView) findViewById(R.id.drawingCanvasView);

		clearButton = (Button) findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				pointerImageView.setVisibility(View.GONE);
				dv.reset();
			}
 
		});
		
		submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				dv.submit();
			}
 
		});
		
		backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				finish();
			}
 
		});
		
		demoButton = (Button) findViewById(R.id.demoButton);
		demoButton.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				startDemo();
			}
 
		});
		
		String currentLetter = "";
		
		//get the letter data
        try {
			JSONObject letterData = getLetterData();
			
			currentLetter = letterData.getString("character");
			
			Log.d("DrawingActivity",currentLetter);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        int id = getResources().getIdentifier(currentLetter, "drawable", "com.unit11apps.circusletters");
        
        image = (ImageView) findViewById(R.id.imageView1);
        image.setImageResource(id);
		
        dv.initialize(this);

    	mysteriousPointerTop = (60 * scaleFactor);
    	mysteriousPointerLeft = (60 * scaleFactor); //(general letter image offset + the 10% space added above in layout)
    	
    	letterOffsetLeft = dv.getLetterOffsetLeft();
    	letterOffsetTop = dv.getLetterOffsetTop();
    	
		pointerImageView = (ImageView) findViewById(R.id.pointer);
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
	
    
    private class MyAnimationListener implements AnimationListener{
    	
    	View mView;
    	
    	public MyAnimationListener(View view)
    	{
    		mView = view;
    	}

        @Override
        public void onAnimationEnd(Animation animation) {
        	//show the feedback
        	TranslateAnimation newAnimation = new TranslateAnimation(0, 0, -500, 0);
        	newAnimation.setDuration(1000);
        	newAnimation.setFillAfter(false);
        	newAnimation.setStartOffset(1000);

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
    
    protected void hideFeedbackImages()
    {
    	ImageView feedbackImageViewTick = (ImageView) findViewById(R.id.tick);
    	feedbackImageViewTick.setVisibility(View.GONE);
    	
    	ImageView feedbackImageViewCross = (ImageView) findViewById(R.id.cross);
    	feedbackImageViewCross.setVisibility(View.GONE);
    }
    
    protected void feedback(boolean result)
    {
    	ImageView feedbackImageView;
    	int soundResourceId;
    	
    	if(result)
    	{
    		feedbackImageView = (ImageView) findViewById(R.id.tick);
    		
    		soundResourceId = R.raw.success1;
    	}
    	else
    	{
    		feedbackImageView = (ImageView) findViewById(R.id.cross);
    		
    		soundResourceId = R.raw.fail;
    	}
    	
    	//show the feedback
    	feedbackImageView.setVisibility(View.VISIBLE);
    	
    	//show the feedback
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -500);
		animation.setDuration(1000);
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
    
	@Override
	public void onBackPressed() {
		Intent a = new Intent(Intent.ACTION_MAIN);
		a.addCategory(Intent.CATEGORY_HOME);
		a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(a);
		
	   return;
	}
	
	public void startDemo()
	{
		dv.reset();
		
		//hide the dv
		dv.setVisibility(View.GONE);
		
		oldPointerX = 0;
		oldPointerY = 0;
		
		preparePointer();
		
		movePointer();
	}
	
	protected void preparePointer()
	{
		//show it 
		pointerImageView.setVisibility(View.VISIBLE);
		
		int pointerImageWidth = 800;//;

		LayoutParams params = new LayoutParams(
				pointerImageWidth,      
				pointerImageWidth
		);
		
		int topMargin = (int)(dv.getDrawingAreaTop());
		int leftMargin = -pointerImageWidth + (int)(dv.getDrawingAreaLeft());
		
		params.setMargins(leftMargin, topMargin, 0, 0);
		pointerImageView.setLayoutParams(params);
		pointerImageView.requestLayout();
	}
	
	private class PointerAnimationListener implements AnimationListener{
    	
        @Override
        public void onAnimationEnd(Animation animation) {
        	
        	movePointer();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
	
	//move the wand
    protected void movePointer()
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
        	LetterPoint thisPoint = points.get(selectedSegmentPoints);
        	
            //check it is a wand point and within the points length
            if((selectedSegmentPoints < points.size()) && (thisPoint.wandPoint == true))
            {
                //calculate starting point and convert to pixels
                x = (thisPoint.x * drawingAreaWidth) / 100;
                y = (thisPoint.y * drawingAreaHeight) / 100;
                
                x += mysteriousPointerLeft - letterOffsetLeft;
                y += mysteriousPointerTop - letterOffsetTop;
                
                //increment point counter
                selectedSegmentPoints++;
                
                TranslateAnimation animation = new TranslateAnimation(oldPointerX, x, oldPointerY, y);
        		animation.setDuration(200);
        		animation.setFillAfter(true);
        		animation.setInterpolator(new LinearInterpolator());
        		animation.setAnimationListener(new PointerAnimationListener());

        		pointerImageView.startAnimation(animation);
        		
                oldPointerX = x;
                oldPointerY = y;
            }
            else
            {
                //increment the
                selectedSegmentIndex++;
                selectedSegmentPoints = 0;
                
                //call this again
                movePointer();
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
		}
	};
}
