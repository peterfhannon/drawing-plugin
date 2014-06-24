package com.unit11apps.drawing;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.unit11apps.MagicLetters.R;
import com.unit11apps.drawing.LetterPointData.LetterPoint;
import com.unit11apps.drawing.LetterPointData.Segment;

@SuppressLint("NewApi")
public class DrawingView extends FrameLayout {

    public int width;
    public  int height;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint circlePaint2;
    private Path circlePath2;
    private String currentLetter = "";
    private JSONObject letterData;
	private JSONObject letterDataConfig;
	private double pointRadius;
	private double scaleFactor;
	private float scaledPointRadius;
	private LetterPointData letterPointData;
	private float drawingAreaLeft;
	private float drawingAreaTop;
	private float drawingAreaWidth;
	private float drawingAreaHeight;
	private float letterOffsetTop;
	private float letterOffsetLeft;
	private Paint mPaint;
	private boolean disableDrawing = false;
	private float mysteriousPointScale;
	private float standardLineThickness = 48;
	private float lineThickness = 0;
	private boolean lastResultSuccess = false;
	
	public LetterPointData getLetterPointData() {
		return letterPointData;
	}

	public void setLetterPointData(LetterPointData letterPointData) {
		this.letterPointData = letterPointData;
	}


	public float getLetterOffsetTop() {
		return letterOffsetTop;
	}

	public void setLetterOffsetTop(float letterOffsetTop) {
		this.letterOffsetTop = letterOffsetTop;
	}

	public float getLetterOffsetLeft() {
		return letterOffsetLeft;
	}

	public void setLetterOffsetLeft(float letterOffsetLeft) {
		this.letterOffsetLeft = letterOffsetLeft;
	}


	//Assessment
	private float captureRadius = 45;
	private float oldX = -1;
	private float oldY = -1;
	private int currentPoint = -1;
	private int currentSegment = 0;
    private boolean outsideLetterBoundry = false;
    private boolean previouslyOutsideLetterBoundry = false;
	private boolean correctFirstPoint = false;
	private boolean correctInitialFirstPoint = false;
	
	private DrawingActivity da;
	
	float density = 1;
	private double mysteriousLeft;
	private double mysteriousTop;
	
	private boolean debugMode = false;
	
    private float mX = -1;
    private float mY = -1;
	private int pointTouchMoveThreshold = 400;
	private boolean ignoreStartingPoint = false;
	private String mode = "test-writing";
    private static final float TOUCH_TOLERANCE = 4;

    @SuppressLint("NewApi")
	public DrawingView(Context c) {
        super(c);
        context=c;
        
        setWillNotDraw(false);
    }
    
    public DrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        
        setWillNotDraw(false);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setWillNotDraw(false);
    }
    
    protected void initialize(DrawingActivity drawingActivity)
    {
    	da = drawingActivity;
    	
        Display display = da.getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        
        float displayHeight = 0;
        float displayWidth = 0;
        
        if(android.os.Build.VERSION.SDK_INT >= 13)
        {
	        display.getSize(displaySize);
        	displayHeight = displaySize.y;
        	displayWidth = displaySize.x;
        }
        else
        {
        	displayHeight = display.getHeight();
        	displayWidth = display.getWidth();
        }
        
    	mysteriousPointScale = 1.4f;

        //get the window width and height;
        //drawingAreaHeight = displayHeight;
        drawingAreaHeight = (73 * displayHeight / 100) * mysteriousPointScale;// * density;
    	drawingAreaWidth = (drawingAreaHeight/3) * 4; //4:3 area
    	drawingAreaTop = (displayHeight/10);
    	drawingAreaLeft = (displayWidth - drawingAreaWidth)/2;
    	
    	//this is how to work out the scalefactor
    	scaleFactor = displayHeight / 768;
    	
    	lineThickness = (float) (standardLineThickness * scaleFactor);
    	
        letterData = da.getLetterData();
        letterDataConfig = da.getLetterDataConfig();
        pointRadius = da.getPointRadius() + (6);
        //scaleFactor = da.getScaleFactor();
        
        scaledPointRadius = (float)(pointRadius * scaleFactor);
        
        //get the letter data
        try {
			JSONObject letterData = da.getLetterData();
			
			currentLetter = letterData.getString("character");
			
			Log.d("DrawingActivity",currentLetter);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
	    mPaint.setDither(true);
	    mPaint.setColor(Color.parseColor("#8836C7"));
	    mPaint.setStyle(Paint.Style.STROKE);
	    mPaint.setStrokeJoin(Paint.Join.ROUND);
	    mPaint.setStrokeCap(Paint.Cap.ROUND);
	    mPaint.setStrokeWidth(lineThickness);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);  
        
        if(debugMode)
        {
	        circlePaint = new Paint();
	        circlePath = new Path();
	        circlePaint.setAntiAlias(true);
	        circlePaint.setColor(Color.BLUE);
	        circlePaint.setStyle(Paint.Style.STROKE);
	        circlePaint.setStrokeJoin(Paint.Join.MITER);
	        circlePaint.setStrokeWidth(4f);
	        circlePaint2 = new Paint();
	        circlePath2 = new Path();
	        circlePaint2.setAntiAlias(true);
	        circlePaint2.setColor(Color.BLUE);
	        circlePaint2.setStyle(Paint.Style.STROKE);
	        circlePaint2.setStrokeJoin(Paint.Join.MITER);
	        circlePaint2.setStrokeWidth(4f);
        }
        
    	//get the letterPointData
        letterPointData = new LetterPointData(letterData, letterDataConfig);
    	
        mysteriousLeft = 0;
    	mysteriousTop = 0;
        
    	//add point capture radius
    	mysteriousLeft = mysteriousLeft + ((4.74 / 100) * drawingAreaWidth);
    	mysteriousTop = mysteriousTop + ((3.55 / 100) * drawingAreaHeight);
    	
    	float mysteriousOffsetAmount = drawingAreaHeight/10;
    	
    	JSONObject letterPosition;
		try {
			letterPosition = letterDataConfig.getJSONObject("drawingPluginPosition");
	    	letterOffsetTop = (float)((letterPosition.getDouble("top")/100) * mysteriousOffsetAmount);
	    	letterOffsetLeft = (float)((letterPosition.getDouble("left")/100) * mysteriousOffsetAmount);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(debugMode)
		{
			drawDebugCircles();
		}
    }
    
    public double getMysteriousLeft() {
		return mysteriousLeft;
	}

	public void setMysteriousLeft(double mysteriousLeft) {
		this.mysteriousLeft = mysteriousLeft;
	}

	public double getMysteriousTop() {
		return mysteriousTop;
	}

	public void setMysteriousTop(double mysteriousTop) {
		this.mysteriousTop = mysteriousTop;
	}

	public float getDrawingAreaLeft() {
		return drawingAreaLeft;
	}

	public void setDrawingAreaLeft(float drawingAreaLeft) {
		this.drawingAreaLeft = drawingAreaLeft;
	}

	public float getDrawingAreaTop() {
		return drawingAreaTop;
	}

	public void setDrawingAreaTop(float drawingAreaTop) {
		this.drawingAreaTop = drawingAreaTop;
	}

	public float getDrawingAreaWidth() {
		return drawingAreaWidth;
	}

	public void setDrawingAreaWidth(float drawingAreaWidth) {
		this.drawingAreaWidth = drawingAreaWidth;
	}

	public float getDrawingAreaHeight() {
		return drawingAreaHeight;
	}

	public void setDrawingAreaHeight(float drawingAreaHeight) {
		this.drawingAreaHeight = drawingAreaHeight;
	}

	protected void drawDebugCircles()
    {
    	ArrayList<Segment> segments = letterPointData.getSegments();
    	int totalSegments = segments.size();
    	
    	circlePath2.reset();
    	
    	circlePath2.addCircle(drawingAreaLeft, drawingAreaTop, scaledPointRadius, Path.Direction.CW);
    	circlePath2.addCircle(drawingAreaLeft + (drawingAreaWidth), drawingAreaTop, scaledPointRadius, Path.Direction.CW);
    	circlePath2.addCircle(drawingAreaLeft, drawingAreaTop + (drawingAreaHeight), scaledPointRadius, Path.Direction.CW);
    	circlePath2.addCircle(drawingAreaLeft + (drawingAreaWidth),  drawingAreaTop + (drawingAreaHeight), scaledPointRadius, Path.Direction.CW);
    	
    	for(int i=0; i<totalSegments; i++)
    	{
    		Segment thisSegment = (Segment)(segments.get(i));
    		
    		ArrayList<LetterPoint> points = thisSegment.getPoints();
    		int totalPoints = points.size();
    		
    		for(int j=0; j<totalPoints; j++)
        	{
    			LetterPoint point = points.get(j);
    			
    			float x = (float)(((point.x/100) * drawingAreaWidth) + drawingAreaLeft + mysteriousLeft) + letterOffsetLeft;
    			float y = (float)(((point.y/100) * drawingAreaHeight) + drawingAreaTop + mysteriousTop) + letterOffsetTop;
    			
                circlePath2.addCircle(x, y, scaledPointRadius, Path.Direction.CW);
        	}
    	}
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inPurgeable = true;
        //mBitmap = Bitmap.createBitmap(w, h, o);
        
        Resources res = getContext().getResources();
        
        Bitmap b = BitmapFactory.decodeResource(res, R.drawable.background, o);
        
        mBitmap = Bitmap.createScaledBitmap(b, w, h, false);
        
        Drawable d = new BitmapDrawable(da.getResources(),mBitmap);
        
        da.setMainBackground(d);
        
        b.recycle();
        
        b = null;
        
        mCanvas = new Canvas();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
    	
    	//canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);

    	//TODO : Avoid drawing when demoing?
        if(canvas != null)
        { 
	        if(debugMode)
	        {
	        	//canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
	        	
	        	canvas.drawPath( circlePath2,  circlePaint2);
	        }
	
	        canvas.drawPath( mPath,  mPaint);
	
	        if(debugMode)
	        {
	        	canvas.drawPath( circlePath,  circlePaint);
	        }
        }
    }

    private void touch_start_draw(float x, float y, boolean valid) {
        
    	mPath.moveTo(x, y);
        
        mX = x;
        mY = y;
        
        mPath.lineTo(mX+1, mY+1);
    }
    
    private void touch_move_draw(float x, float y, boolean valid) {

    	float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        	
            mX = x;
            mY = y;
            
            mPath.lineTo(mX, mY);
            
            if(debugMode)
            {
            	circlePath.reset();
            	circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

    }
    
    private void touch_up_draw() {
        mPath.lineTo(mX, mY);
        
        if(debugMode)
        {
        	circlePath.reset();
        }
        
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(da.isEnabled())
        {
	    	float rawX = event.getX();
	        float rawY = event.getY();
	        
	        float x = (float)((rawX - drawingAreaLeft));
			float y = (float)((rawY - drawingAreaTop));
			
			int offset = 120;
	
			if(x > offset && x < drawingAreaWidth - offset)
			{
				
		        switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	handleTouchStart(x, y, rawX, rawY);
		                break;
		            case MotionEvent.ACTION_MOVE:
		            	handleTouchMove(x, y, rawX, rawY);
		                break;
		            case MotionEvent.ACTION_UP:
		                handleTouchEnd();
		            	touch_up_draw();
		                break;
		        }
		        
		        invalidate();
			}
			else
			{
				mX = -1;
				mY = -1;
			}
        }
        else
        {
        	reset();
        }
        
        return true;
    } 
    
    private void handleTouchStart(float x, float y, float rawX, float rawY)
    {
    	oldX = -1;
    	oldY = -1;
    	mX = -1;
        mY = -1;
    	currentPoint = -1;
    	
    	boolean valid = handleTouch(x,y, true);
    	
    	touch_start_draw(rawX, rawY, valid);
    }
    
    private void handleTouchMove(float x, float y, float rawX, float rawY)
    {
    	oldX = x;
    	oldY = y;
    	
    	boolean start = false;
    	
    	if(mX == -1 || mY == -1)
    	{
    		mPath.moveTo(rawX, rawY);
    		
    		start = true;
    	}
    	
    	boolean valid = handleTouch(x,y, start);
    	
    	touch_move_draw(rawX, rawY, valid);
    }
    
    private boolean handleTouch(float x, float y, boolean touchStart)
    {
    	ArrayList<Segment> segments = letterPointData.getSegments();
    	
    	boolean located = false;
    	
        //loop through the segments
        for (int j = 0; j < segments.size(); j++)
        {
            //get the segment
            Segment thisSegment = segments.get(j);
            
            if(j == currentSegment)
            {
            	ArrayList<LetterPoint> points = thisSegment.getPoints();
            	
                //loop through the individual segments and detect points
                for(int i = 0; i < points.size(); i++)
                {
                    //gedt the point
                    LetterPoint thisPoint = points.get(i);
                    
                    //remap points from center of circle (convert % to px)
                    //float pointXpos = (float) (((thisPoint.x / 100) * drawingAreaWidth) + mysteriousLeft) - letterOffsetLeft;  
                    //float pointYpos = (float) (((thisPoint.y / 100) * drawingAreaHeight) + mysteriousTop) - letterOffsetTop; 
                    
                    float pointXpos = (float)(((thisPoint.x/100) * drawingAreaWidth) + mysteriousLeft) + letterOffsetLeft;
        			float pointYpos = (float)(((thisPoint.y/100) * drawingAreaHeight) + mysteriousTop) + letterOffsetTop;
                    
                    //check circle to point intersect
                    boolean found = checkCircleIntersect(pointXpos, pointYpos, x, y, (float)(scaledPointRadius));
                    
                    //if its found and not the current point
                    if(touchStart && found)
                    {
                        //correct first point
                        if(i == 0)
                        {
                            //this is a correct first point
                            correctFirstPoint = true;
                            correctInitialFirstPoint = true;
                            
                            //Log.d(getClass().getName(), "CORRECT FIRST POINT DETECTED");
                            
                            //set previous to done
                        	thisPoint.done = true;
                        	
                        	//Log.d(getClass().getName(), "Setting point " + i + " to DONE");
                        }
                    }
                    else if(found && i != currentPoint) //??? Extra stuff
                    {
                        //set the current point and increment the no times passed through
                        currentPoint = i;
                        thisPoint.passedThrough++;
                        
                        //if not first, set previous to done
                        if(i >= 1)
                        {
                            //set previous to done
                        	thisPoint.done = true;
                        	
                        	//Log.d(getClass().getName(), "Setting point " + i + " to DONE");
                        }
                    }

                    //if found carry on
                    if(found)
                    {
                    	ArrayList<Integer> strictPointOrder = thisSegment.getStrictPointOrder();
                    	ArrayList<Integer> strictPointsDone = thisSegment.getStrictPointsDone();
                    	
                        //Add to strict points done
                        if(strictPointOrder.size() > 0 && strictPointOrder.indexOf(i) >= 0)
                        {
                            if(strictPointsDone.indexOf(i) < 0)
                            {
                            	strictPointsDone.add(i);
                            }
                        }
                        
                        //activate located flag
                        located = true;
                        
                        //exit the loop
                        break;
                    }
                }
            }
        }
        
        //if its wasn't located
        if(!located)
        {
        	//Log.d(getClass().getName(), "NOT LOCATED! Outside all point boundaries!");
        	
            //automatically fail for being outside area
            outsideLetterBoundry = true;
            previouslyOutsideLetterBoundry = true;
            
            return false;
        }
        
        return true;
    }
    
    //detect the end of the touch
    private void handleTouchEnd()
    {
    	if(disableDrawing)
        {
            return;
        }
    	
        ArrayList<Segment> segments = letterPointData.getSegments();
        
        //if first segment drawn correctly move on to the next segment
        if(currentSegment < segments.size() - 1 && !outsideLetterBoundry && !previouslyOutsideLetterBoundry && passedThroughMainPoints())
        {
            currentSegment++;
            
            //console.log('Moving on to the next segment!!!');
            return;
        }
    	
        /*
        //if test style
        if(?)
        {
        	boolean result = testDrawing();
        }*/
    }
    
    private boolean testDrawing()
    {
        boolean letterDrawnSucessfully = false;
    	
        //reset the old x and y
        oldX = -1;
        oldY = -1;
        mX = -1;
        mY = -1;

        boolean illegalPassBackThroughDetected = false;

        boolean strictOrderCorrect = true;
        
        String pointsList = "";
        
        int pointsPast = 0;
        
        ArrayList<Segment> segments = letterPointData.getSegments();
        
        //loop through the segments
        for (int j = 0; j < segments.size(); j++)
        {
            Segment thisSegment = segments.get(j);
            
            pointsList = "";
            
            ArrayList<LetterPoint> points = thisSegment.getPoints();
            
            for(int i = 0; i<points.size(); i++)
            {
                LetterPoint thisPoint = points.get(i);

                //if the point is active
                if(thisPoint.done)
                {
                    //count the point
                    pointsPast++;
                    
                    pointsList = pointsList + " " + i;
                }
                
                //check if the number of passes has been breached
                if(i != (points.size() - 1) && thisPoint.passedThrough > pointTouchMoveThreshold)
                { 
                    //activate the illegal pass back through
                    illegalPassBackThroughDetected = true;
                }
            }
            
            if(thisSegment.getStrictPointOrder().size() > 0 && strictOrderCorrect)
            {
                strictOrderCorrect = thisSegment.getStrictPointsDone().equals(thisSegment.getStrictPointOrder());
            }
        }

        //allow drawing in sections and backwards if in free-writing mode or if we want to ignore the starting point for some reason
        if(((mode != "test-writing")) || (ignoreStartingPoint ))
        {
            correctFirstPoint = true;
        }
        
        //check if drawn correctly
        if((passedThroughMainPoints()) && (!illegalPassBackThroughDetected) && (!outsideLetterBoundry)
           && (!previouslyOutsideLetterBoundry) && (correctFirstPoint) && (mode != "test-writing" || strictOrderCorrect))
        {
            //sucess
            letterDrawnSucessfully = true;
        }
        //otherwise they fail
        else
        {
            //fail
            letterDrawnSucessfully = false;
        }
        
        //reset found and hide the wand hand
        illegalPassBackThroughDetected = false;
        
        //reset correct first point
        correctFirstPoint = false;
        outsideLetterBoundry = false;
        pointsPast = 0;
        
        //TODO : Feedback
        
        Log.d("letterDrawnSucessfully : " , String.valueOf(letterDrawnSucessfully));
        
        if(mode == "test-writing")
        {
            disableDrawing = true;
        }
        
        return letterDrawnSucessfully;
    }
    
    //check they have passed through all main points
    private boolean passedThroughMainPoints()
    {
    	ArrayList<Segment> segments = letterPointData.getSegments();
        Segment thisSegment = segments.get(currentSegment);
        ArrayList<Integer> keyPoints = thisSegment.getKeyPoints();
                
        //if it's a segmented letter retuen true on next segment
        if((currentSegment > 0 && mode == "free-writing") ||
            (currentSegment > 0 && currentLetter == "i") ||
            (currentSegment > 0 && currentLetter == "j"))
        {
            return true;
        }
        
        //if passed through the selected points return true else false
        return checkSelectedPoints(keyPoints);
    }
    
    //check the selected points
    private boolean checkSelectedPoints(ArrayList<Integer> keyPoints)
    {
        int count = 0;

        //get the current segment
        ArrayList<Segment> segments = letterPointData.getSegments();
        Segment thisSegment = segments.get(currentSegment);
        
        for(int j = 0; j < keyPoints.size(); j++)
        {
        	ArrayList<LetterPoint> points = thisSegment.getPoints();
        	int thisKeyPoint = keyPoints.get(j);
        	
            //loop through the segments
            for(int k = 0; k < points.size(); k++)
            {
                //get the current point
                LetterPoint thisPoint = points.get(k);
        
                //if the point was passed through or the points either side
                if((thisKeyPoint == thisPoint.index) && (thisPoint.done)) 
                {
                    //increment count
                    count++;
                }
            }
        }
        
        //if the count was >= the number of points
        return (count >= (keyPoints.size()));
    }
    
    private boolean checkCircleIntersect(float x, float y, float cx, float cy, float r)
    {
        float dx = x - cx;
        float dy = y - cy;
        
        return ((dx * dx) + (dy * dy)) <= r * r;
    }
    
    protected void clearCanvas()
    {
        // kill this so we don't double draw
        mPath.reset();
    	
    	mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    	
    	//mCanvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
    	
    	invalidate();
    }
    
    protected void reset()
    {
    	if(da.isEnabled())
    	{
    		clearCanvas();
    	}
    	
    	letterPointData.reset();
    	
    	disableDrawing = false;
    	
    	outsideLetterBoundry = false;
    	previouslyOutsideLetterBoundry = false;
    	
    	currentSegment = 0;
    	currentPoint = -1;
    	
    	/*
    	ImageView feedbackTick = (ImageView)da.findViewById(R.id.tick);
    	feedbackTick.clearAnimation();
    	
    	ImageView feedbackCross = (ImageView)da.findViewById(R.id.cross);
    	feedbackCross.clearAnimation();*/
    }
 
    
    protected void submit()
    {
    	lastResultSuccess = testDrawing();

    	da.feedback(lastResultSuccess);
    	
    	Timer myTimer = new Timer();
    	
		myTimer.schedule(new TimerTask() {			
			@Override
			public void run() {
				TimerMethod();
			}
			
		}, 2500);
    }
    
    private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		da.runOnUiThread(Timer_Tick);
	}
    
    
    private Runnable Timer_Tick = new Runnable() {
		public void run() {
			da.setEnabled(true);
			reset();
			
			da.feedbackFinished(lastResultSuccess);
		}
	};

	public float getScaleFactor() {
		// TODO Auto-generated method stub
		return (float)(scaleFactor);
	}
	
	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	
	public void recycleBitmaps()
	{
		if(mBitmap != null)
		{
			mBitmap.recycle();
			mBitmap = null;
		}
	}
}