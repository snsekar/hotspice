package com.hotspice.api.v1.endpoints;

import static com.hotspice.SystemInit.*;
import static com.hotspice.util.Util.apiResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotspice.api.v1.models.Category;
import com.hotspice.api.v1.models.Dish;
import com.hotspice.api.v1.models.DishOrder;
import com.hotspice.api.v1.models.Order;
import com.hotspice.api.v1.models.OrderItemQuantity;
import com.hotspice.api.v1.models.OrderStatus;
import com.hotspice.security.Secured;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("dish")
@Api(position = 1, value = "dish")
public class DishApi {

	static ObjectMapper objectMapper = new ObjectMapper();		
	
	@GET
	@Path("/{dish_name}")	
	@Secured
	@ApiOperation(value = "Get dish details", response = String.class,position = 10)	
	public String getDish(@PathParam("dish_name") String dish_name, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{	
		DBObject dish = DISHES.findOne(new BasicDBObject("dish_name", dish_name));
		if(dish == null){			
			return apiResponse("error","Dish not exists");
		}else{
			dish.removeField("_id");
			return apiResponse("ok","Dish details",objectMapper.writeValueAsString(dish));
		}
	}
	
	@POST
	@Consumes("application/json")
	@Secured
	@ApiOperation(value = "Create a new dish", response = String.class,position = 20)	
	public String createDish(Dish dish, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		DBObject d = DISHES.findOne(new BasicDBObject("dish_name",dish.dish_name));
		if(d == null){
			BasicDBObject newDish = new BasicDBObject(					
					"dish_name", dish.dish_name).append(
					"category",dish.category);
			DISHES.insert(newDish);
			return apiResponse("ok","Dish created succesfully");
		}else{			
			return apiResponse("error","A dish with same name already exists");
		}		
	}
	
	@DELETE
	@Path("/{dish_name}")
	@Secured
	@ApiOperation(value = "Delete a dish", response = String.class,position = 30)	
	public String deleteDish(@PathParam("dish_name") String dish_name, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{	
		DBObject dish = DISHES.findOne(new BasicDBObject("dish_name", dish_name));
		if(dish == null){			
			return apiResponse("error","Dish not exists");
		}else{
			DISHES.remove(new BasicDBObject("dish_name",dish_name));
			return apiResponse("ok","Dish deleted sucessfully");
		}
	}
	
	@GET
	@Path("/search")	
	@Secured
	@ApiOperation(value = "Search dishes by keyword", response = String.class,position = 130)	
	public String searchDishes(@QueryParam("q") String q, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		ArrayNode ordersResult = objectMapper.readValue("[]", ArrayNode.class);		
		DBCursor orders = DISHES.find(new BasicDBObject("dish_name",new BasicDBObject("$regex",q).append("$options", 'i')));
		for(DBObject order: orders){			
			ordersResult.add(objectMapper.readValue(JSON.serialize(order), JsonNode.class));
		}		
		return apiResponse("ok","Search result",objectMapper.writeValueAsString(ordersResult));		
	}
	
	@GET
	@Path("/search/recent")	
	@Secured
	@ApiOperation(value = "Search recently ordered dishes", response = String.class,position = 140)	
	public String recentlyOrdered(@HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		ArrayNode ordersResult = objectMapper.readValue("[]", ArrayNode.class);		
		DBCursor orders = ORDERITEMS.find().sort(new BasicDBObject("ordered_time",-1));
		for(DBObject order: orders){	
			ObjectNode result = objectMapper.readValue("{}", ObjectNode.class);
			result.put("dish_name",order.get("dish_name").toString());		
			ordersResult.add(result);
		}		
		return apiResponse("ok","Search result",objectMapper.writeValueAsString(ordersResult));		
	}
	
	@GET
	@Path("/search/most")	
	@Secured
	@ApiOperation(value = "Search mostly ordered dishes", response = String.class,position = 150)	
	public String mostlyOrdered(@HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		ArrayNode results = objectMapper.readValue("[]", ArrayNode.class);
		DBObject groupFields = new BasicDBObject( "_id", "$dish_name");
		groupFields.put("count", new BasicDBObject( "$sum", 1));
		DBObject group = new BasicDBObject("$group", groupFields);
		DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
		List<DBObject> pipeline = Arrays.asList(group, sort);
		AggregationOutput groupOutput = ORDERITEMS.aggregate(pipeline);
		for(DBObject broadCaster: groupOutput.results()){
			ObjectNode result = objectMapper.readValue("{}", ObjectNode.class);
			String broadcasterID = broadCaster.get("_id").toString();
			result.put("dish_name",broadcasterID);		
			result.put("count",(int)broadCaster.get("count"));		
			results.add(result);			
		}
		return apiResponse("ok","Search result",objectMapper.writeValueAsString(results));	
	}
}