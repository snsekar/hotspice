package com.hotspice.util;


public class Util {
	
	public static String apiResponse(String status,String message){		
		return "{ \"status\":\""+status+"\",\"message\": \""+message+"\"}";		
	}
	
	public static String apiResponse(String status,String message, String data){		
		return "{ \"status\":\""+status+"\",\"message\": \""+message+"\",\"data\": "+data+"}";		
	}
}
