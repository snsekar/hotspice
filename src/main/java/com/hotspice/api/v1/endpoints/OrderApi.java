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

@Path("order")
@Api(position = 3, value = "order")
public class OrderApi {

	static ObjectMapper objectMapper = new ObjectMapper();		
	
	@POST	
	@Consumes("application/json")
	@Secured
	@ApiOperation(value = "Place a new order", response = String.class,position = 50)	
	public String createOrder(Order order, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{	
		DBObject newOrder = new BasicDBObject();
		long now = System.currentTimeMillis();
		newOrder.put("ordered_time",now);
		newOrder.put("status", "New");
		ORDERS.insert(newOrder);
		String orderID = ((ObjectId)newOrder.get( "_id" )).toString();
		for(DishOrder orderItem : order.dishes){
			DBObject oi = (DBObject)JSON.parse(objectMapper.writeValueAsString(orderItem));
			oi.put("order_id", orderID);
			oi.put("ordered_time", now);
			ORDERITEMS.insert(oi);			
		}
		return apiResponse("ok","Order placed succesfully");		
	}
	
	@GET
	@Path("/{order_id}")
	@Secured
	@ApiOperation(value = "View order", response = String.class,position = 60)	
	public String viewOrder(@PathParam("order_id") String order_id , @HeaderParam("Authorization") String authorizationHeader ) throws Exception{	
		DBObject stream = ORDERS.findOne(new BasicDBObject("_id", new ObjectId(order_id)));
		stream.put("_id", order_id);
		return apiResponse("ok","View Order",objectMapper.writeValueAsString(stream));		
	}
	
	@GET
	@Secured
	@ApiOperation(value = "View all orders", response = String.class,position = 70)	
	public String viewOrders(@QueryParam("sort_by") String sort_by, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		ArrayNode ordersResult = objectMapper.readValue("[]", ArrayNode.class);
		if(sort_by == null || "".equals(sort_by)){
			sort_by = "ordered_time";
		}
		DBCursor orders = ORDERS.find().sort(new BasicDBObject(sort_by,-1));
		for(DBObject order: orders){
			Date d = new Date((long)order.get("ordered_time"));
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy hh:mm:ss a");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			order.put("ordered_time", sdf.format(d));			
			String orderID = order.get("_id").toString();
			DBCursor orderItems = ORDERITEMS.find(new BasicDBObject("order_id",orderID));
			List<DBObject> db = new ArrayList<DBObject>();
			for(DBObject oi: orderItems){				
				db.add(oi);
			}
			order.put("dishes", db);			
				ordersResult.add(objectMapper.readValue(JSON.serialize(order), JsonNode.class));
			}		
		return apiResponse("ok","View Orders",objectMapper.writeValueAsString(ordersResult));		
	}	

	@PUT
	@Consumes("application/json")
	@Secured
	@ApiOperation(value = "Update order", response = String.class,position = 100)	
	public String updateOrder(String jsonContent, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		ObjectNode content = objectMapper.readValue(jsonContent, ObjectNode.class);		
		content.remove("ordered_time");
		System.out.println(objectMapper.writeValueAsString(content));
		DBObject data = (DBObject) JSON.parse(objectMapper.writeValueAsString(content));
		ORDERS.update(new BasicDBObject("_id", new ObjectId(content.path("_id").path("$oid").asText())), new BasicDBObject("$set",data),false,false);
		return  apiResponse("ok","Update success");
	}
	
	@PUT
	@Path("/status")
	@Consumes("application/json")
	@Secured
	@ApiOperation(value = "Update order status", response = String.class,position = 110)	
	public String updateOrderStaus(OrderStatus os, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{		
		
		DBObject data = new BasicDBObject("status",os.status);
		ORDERS.update(new BasicDBObject("_id", new ObjectId(os.order_id)), new BasicDBObject("$set",data),false,false);
		return  apiResponse("ok","Update success");
	}
	
	@PUT
	@Path("/itemquantity")
	@Consumes("application/json")
	@Secured
	@ApiOperation(value = "Update order item quantity", response = String.class,position = 120)	
	public String updateOrderItemQuantity(OrderItemQuantity oiq, @HeaderParam("Authorization") String authorizationHeader ) throws Exception{			
		DBObject data = new BasicDBObject("quantity",oiq.quantity);
		ORDERITEMS.update(new BasicDBObject("_id", new ObjectId(oiq.order_item_id)), new BasicDBObject("$set",data),false,false);
		return  apiResponse("ok","Update success");
	}
	

}
