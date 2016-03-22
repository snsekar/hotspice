package com.hotspice.api.v1.endpoints;

import static com.hotspice.constants.Constants.*;
import static com.hotspice.SystemInit.*;
import static com.hotspice.util.Util.apiResponse;
import static com.hotspice.security.AuthUtil.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.codec.binary.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotspice.api.v1.models.Credentials;
import com.hotspice.api.v1.models.User;
import com.hotspice.security.Secured;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
@Path("user")
@Api(position = 4, value = "user")
public class UserApi {
	static ObjectMapper objectMapper = new ObjectMapper();		

	@POST
	@Path("/signup")
	@Consumes("application/json")
    @Produces("application/json")
	@ApiOperation(value = "Create a new user", response = String.class,position = 20)	
	public String signup(User user) throws Exception{		
		DBObject d = USERS.findOne(new BasicDBObject("user_id",user.user_id));
		if(d == null){
			BasicDBObject newUser = new BasicDBObject(					
					"user_id", user.user_id).append(
					"password", getHashedPassword(user.password,PASSWORD_SALT)).append(
					"name", user.name).append(
					"email", user.email);
			USERS.insert(newUser);
			return apiResponse("ok","User created succesfully");
		}else{			
			return apiResponse("error","A user with same id already exists");
		}		
	}
	
	@POST
	@Path("/signin")
    @Produces("application/json")
    @Consumes("application/json")
	@ApiOperation(value = "Signin user", response = String.class,position = 10)

    public String signin(Credentials credentials) throws JsonProcessingException {

        String userId = credentials.user_id;
        String password = credentials.password;
     
        DBObject user = USERS.findOne(new BasicDBObject("user_id",userId));
        if(user == null)
        {
        	return apiResponse("error","Invalid user");
        }
        String dbPassword = user.get("password").toString();
        String inputPassword = getHashedPassword(password, PASSWORD_SALT);
        
        if(!dbPassword.equals(inputPassword))
        {
        	return apiResponse("error","Invalid password");
        }

        String token = tokenGenerator(userId);
		USERS.update(new BasicDBObject("user_id", userId), new BasicDBObject("$push",new BasicDBObject("token",token) ),false,false);		
		
        // Authenticate the user, issue a token and return a response
        
        return apiResponse("ok","Signin succes",objectMapper.writeValueAsString(token));	
    }
	
	@POST
	@Secured
	@Path("/signout")
	@ApiOperation(value = "Signout user", response = String.class,position = 20)	
	public String signOut(@HeaderParam("Authorization") String authorizationHeader) throws Exception{	
		String userId = getIDfromToken(authorizationHeader);		 
		USERS.update(new BasicDBObject("user_id", userId), new BasicDBObject("$pull",new BasicDBObject("token",authorizationHeader) ),false,false);
		return apiResponse("ok","Signout success");	
	}
	

     
}
