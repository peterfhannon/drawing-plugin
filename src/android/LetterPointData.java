package com.unit11apps.drawing;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LetterPointData {
	private JSONObject originalJSON;
	private ArrayList<Segment> segments;
	
	public LetterPointData(JSONObject letterData, JSONObject letterDataConfig)
	{
		originalJSON = letterData;
		segments = new ArrayList<Segment>();
		
		//loop over and create the data
		try {
			JSONArray keyPointsJSON = letterDataConfig.getJSONArray("thePoints");

			JSONArray segmentsJSON = originalJSON.getJSONArray("segments");
			
			int arrLen = segmentsJSON.length();
			
			for (int i = 0; i < arrLen; i++) {
				Segment newSegment = new Segment(segmentsJSON.getJSONObject(i));
				
				JSONArray segmentKeyPoints = keyPointsJSON.getJSONArray(i);
				
				//add the key points
				newSegment.setKeyPoints(segmentKeyPoints);
				
				segments.add(newSegment);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void reset()
	{
		for(int i=0; i<segments.size(); i++)
		{
			Segment thisSegment = segments.get(i);
			
			thisSegment.reset();
		}
	}
	
	public ArrayList<Segment> getSegments()
	{
		return segments;
	}
	
	public class Segment {
		private JSONObject originalJSON;
		private ArrayList<LetterPoint> points;
		private int index;
		private ArrayList<Integer> strictPointOrder;
		private ArrayList<Integer> strictPointsDone;
		private ArrayList<Integer> keyPoints;
		
		public Segment(JSONObject segmentJSON)
		{
			originalJSON = segmentJSON;
			setStrictPointOrder(new ArrayList<Integer>());
			points = new ArrayList<LetterPoint>();
			keyPoints = new ArrayList<Integer>();
			strictPointsDone = new ArrayList<Integer>();
			
			try {
				index = originalJSON.getInt("index");
				
				JSONArray strictPointOrderArray = originalJSON.getJSONArray("strictPointOrder");
				
				if(strictPointOrderArray != null)
				{
					for(int i=0; i<strictPointOrderArray.length(); i++)
					{
						getStrictPointOrder().add(strictPointOrderArray.getInt(i));
					}
				}
				
				JSONArray pointsArray = originalJSON.getJSONArray("points");
				
				for(int i=0; i<pointsArray.length(); i++)
				{
					LetterPoint newPoint = new LetterPoint(pointsArray.getJSONObject(i));
					
					points.add(newPoint);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void setKeyPoints(JSONArray segmentKeyPoints) {
			for(int i=0; i<segmentKeyPoints.length(); i++)
			{
				try {
					int index = segmentKeyPoints.getInt(i);
					
					keyPoints.add(index);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}

		public ArrayList<LetterPoint> getPoints()
		{
			return points;
		}

		public ArrayList<Integer> getStrictPointOrder() {
			return strictPointOrder;
		}

		public void setStrictPointOrder(ArrayList<Integer> strictPointOrder) {
			this.strictPointOrder = strictPointOrder;
		}

		public ArrayList<Integer> getStrictPointsDone() {
			return strictPointsDone;
		}

		public ArrayList<Integer> getKeyPoints() {
			return keyPoints;
		}
		
		public void reset()
		{
			strictPointsDone = new ArrayList<Integer>();
			
			for(int i=0; i<points.size(); i++)
			{
				LetterPoint thisPoint = points.get(i);
				
				thisPoint.done = false;
			}
		}
	}
	
	public class LetterPoint {
		private JSONObject originalJSON;
		public float x;
		public float y;
		public int index;
		public boolean wandPoint;
		public int passes;
		public int passedThrough = 0;
		public boolean done = false;
		
		public LetterPoint(JSONObject letterPointData)
		{
			originalJSON = letterPointData;
			
			try {
				x = (float)(originalJSON.getDouble("x"));
				y = (float)(originalJSON.getDouble("y"));
				
				index = originalJSON.getInt("index");
				
				wandPoint = originalJSON.getBoolean("wandPoint");
				
				passes = originalJSON.getInt("passes");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
