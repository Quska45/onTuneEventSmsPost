package com.onTune.Log;

import com.onTune.logger.RollingType;
import com.onTune.logger.onTuneLogManager;

//import org.apache.log4j.Logger;
//import org.apache.log4j.chainsaw.Main;

public class onTuneLog {
	
	public static onTuneLogManager logger = new onTuneLogManager(RollingType.rtDate);
	
//	public static Logger logger = Logger.getLogger(Main.class.getName());
//			
//	public static String getExceptionLog(Exception e){
//		return "CLASS["+ e.getClass() + "] " + e.getMessage();
//	}
//	
//	public static String getExceptionLog(String name, Exception e){
//		return  "["+name+"] CLASS["+ e.getClass() + "] " + e.getMessage();
//	}
//	
//	public static String getLog(String name, String desc){
//		return  "["+name+"] " + desc;
//	}
		
}
