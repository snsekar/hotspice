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
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("category")
@Api(position = 2, value = "category")
public class CategoryApi {

	static ObjectMapper objectMapper = new ObjectMapper();		
	
	@POST
	@Consumes("application/json")
	@ApiOperation(value = "Create a new Category", response = String.class,position = 40)	
	public String createCategory(Category category) throws Exception{		
		DBObject d = CATEGORIES.findOne(new BasicDBObject("category",category.category));
		if(d == null){
			BasicDBObject n = new BasicDBObject(				
					"category",category.category);
			CATEGORIES.insert(n);
			return apiResponse("ok","Category created succesfully");
		}else{			
			return apiResponse("error","A category with same name already exists");
		}
	}
	
	@GET
	@ApiOperation(value = "Get all categories", response = String.class,position = 80)	
	public String getAllCategories() throws Exception{	
		ArrayList<String> categories =  (ArrayList<String>) CATEGORIES.distinct("category");
		return apiResponse("ok","Get all categories",objectMapper.writeValueAsString(categories));		
	}
	
	@GET
	@Path("/{category}/dishes")
	@ApiOperation(value = "Get dishes from categories", response = String.class,position = 90)	
	public String getDishesCategory(@PathParam("category") String category ) throws Exception{	
		ArrayList<String> dishes =  (ArrayList<String>) DISHES.distinct("dish_name",new BasicDBObject("category",category));
		return apiResponse("ok","Get dishes from categories",objectMapper.writeValueAsString(dishes));		
	}
	
}