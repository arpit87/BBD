package com.bakarapp.HelperClasses;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BeepList {
 
static BeepList instance = null;
ArrayList<ArrayList <Beep>> listOfbeepList =  new ArrayList <ArrayList <Beep>>();
ArrayList <Beep> myBeepList = new ArrayList<Beep>();
ArrayList <Beep> dailyTrendBeepList = new ArrayList<Beep>();
ArrayList <Beep> weeklyTrendBeepList = new ArrayList<Beep>();
ArrayList <Beep> monthlyTrendBeepList = new ArrayList<Beep>();
int newDailyBeeps = 0;
int totalDailyBeeps = 0;
int newWeeklyBeeps = 0;
int totalWeeklyBeeps = 0;
int newMonthlyBeeps = 0;
int totalMonthlyBeeps = 0;

private BeepList()
{}

public static BeepList getInstance()
{ 
	if (instance == null)
	 instance = new BeepList();
	return instance;
	
}

public List<Beep> getLastBeepList()
{
	return listOfbeepList.get(listOfbeepList.size()-1);
}

public int getBeepListCount()
{
	return listOfbeepList.size();
}

public List<Beep> getFirstBeepList()
{
	if(listOfbeepList.size()>0)
		return listOfbeepList.get(0);
	else
		return null;
}

public List<Beep> getBeepListAt(int index)
{
	if(index >= listOfbeepList.size() || index < 0)
		return null;
	else
		return listOfbeepList.get(index);
}

public List<Beep> getFavBeepList()
{
	return listOfbeepList.get(0);
}

public List<Beep> getMyBeepList()
{
	return myBeepList;
}

public List<Beep> getTrendList(int trend_type)
{
	switch(trend_type)
	{
		case 1:
			return dailyTrendBeepList;
		
		case 2:
			return weeklyTrendBeepList;
		
		case 3:
			return monthlyTrendBeepList;
		
	}
	
	return dailyTrendBeepList;
}

public int getTrendNewBeeps(int trend_type)
{
	switch(trend_type)
	{
		case 1:
			return newDailyBeeps;
		
		case 2:
			return newWeeklyBeeps;
		
		case 3:
			return newMonthlyBeeps;
		
	}
	
	return newDailyBeeps;
}

public int getTrendTotalBeeps(int trend_type)
{
	switch(trend_type)
	{
		case 1:
			return totalDailyBeeps;
		
		case 2:
			return totalWeeklyBeeps;
		
		case 3:
			return totalMonthlyBeeps;
		
	}
	
	return newDailyBeeps;
}


public void updateMyBeepList(JSONArray beeps){
	ArrayList<Beep> beepList = new ArrayList<Beep>();
	try {			
					
		//JSONArray beeps = beepListJSON.getJSONArray("BeepList");
		
		for(int i=0;i<beeps.length();i++)
		{
			JSONObject thisBeep=beeps.getJSONObject(i);
			//if (Platform.getInstance().isLoggingEnabled()) Log.d("json",thisOtherUser.toString());
			Beep b = new Beep(thisBeep);
			beepList.add((b));				
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	myBeepList = beepList;
		
}

public void updateTrendBeepList(JSONArray beeps,int trend_type,int newBeepNum, int totalBeepNum){
	ArrayList<Beep> beepList = new ArrayList<Beep>();
	try {			
					
		//JSONArray beeps = beepListJSON.getJSONArray("BeepList");
		
		for(int i=0;i<beeps.length();i++)
		{
			JSONObject thisBeep=beeps.getJSONObject(i);
			//if (Platform.getInstance().isLoggingEnabled()) Log.d("json",thisOtherUser.toString());
			Beep b = new Beep(thisBeep);
			beepList.add((b));				
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	switch(trend_type)
	{
		case 1:
		dailyTrendBeepList = beepList;
		newDailyBeeps = newBeepNum;
		totalDailyBeeps = totalBeepNum;
		break;
		case 2:
		weeklyTrendBeepList = beepList;
		newWeeklyBeeps = newBeepNum;
		totalWeeklyBeeps = totalBeepNum;
		break;
		case 3:
		monthlyTrendBeepList = beepList;
		newMonthlyBeeps = newBeepNum;
		totalMonthlyBeeps = totalBeepNum;
		break;
	}
		
}

public void updateBeepList(JSONArray beeps){
	ArrayList<Beep> beepList = new ArrayList<Beep>();
	try {			
					
		//JSONArray beeps = beepListJSON.getJSONArray("BeepList");
		
		for(int i=0;i<beeps.length();i++)
		{
			JSONObject thisBeep=beeps.getJSONObject(i);
			//if (Platform.getInstance().isLoggingEnabled()) Log.d("json",thisOtherUser.toString());
			Beep b = new Beep(thisBeep);
			beepList.add((b));				
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	listOfbeepList.add(beepList);
	
}

}
