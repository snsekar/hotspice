package com.hotspice;

import static com.hotspice.constants.Constants.*;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;

public class SystemInit {
	
	//Initialize DB connection
	
	public static DB DB;

	public static DBCollection DISHES;
	public static DBCollection ORDERS;
	public static DBCollection CATEGORIES;
	public static DBCollection ORDERITEMS;
	public static DBCollection USERS;

	static {
	
		MongoClientURI uri = new MongoClientURI("mongodb://root:SHVLD68Z39tz@"+DB_HOST+"/?authSource=admin");
		MongoClient mongoClient = new MongoClient(uri);
		DB = mongoClient.getDB(DB_NAME);		 
		DISHES				= 	DB.getCollection(DB_DISHES);
		ORDERS				= 	DB.getCollection(DB_ORDERS);
		CATEGORIES			= 	DB.getCollection(DB_CATEGORIES);
		ORDERITEMS			= 	DB.getCollection(DB_ORDERITEMS);
		USERS				= 	DB.getCollection(DB_USERS);

		
	}

}
