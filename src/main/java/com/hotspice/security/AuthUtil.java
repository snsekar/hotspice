package com.hotspice.security;

import static com.hotspice.SystemInit.USERS;
import static com.hotspice.constants.Constants.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.NotAuthorizedException;

import org.apache.commons.codec.binary.Base64;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class AuthUtil {

	public static String getHashedPassword(String passwordToHash, String salt)
    {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt.getBytes());
            //Get the hash's bytes 
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } 
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }
	
	public static String tokenGenerator(String userId){
        byte[] encodedKey     = Base64.decodeBase64(API_SECRET);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.MINUTE, TOKEN_EXPIRATION_TIME);
		Date expireAt = cal.getTime();
		String token = Jwts.builder().setId(userId).setIssuedAt(now).setExpiration(expireAt).signWith(SignatureAlgorithm.HS256, key).compact();
		return token;
	}
	
	public static void tokenValidator(String token){
		 byte[] encodedKey     = Base64.decodeBase64(API_SECRET);
		 SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		 String userId = "";
		 try {
			    Claims c = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
			    userId = c.getId();			    
			    DBObject user = USERS.findOne(new BasicDBObject("user_id",userId).append("token", token));
		        if(user == null)
		        {
		        	throw new  NotAuthorizedException("Invalid token");	        	
		        } 

			} catch (SignatureException e) {
				
				throw new  NotAuthorizedException("Invalid token");	  
				
			}catch(ExpiredJwtException ee){
				
				USERS.update(new BasicDBObject("user_id", userId), new BasicDBObject("$pull",new BasicDBObject("token",token) ),false,false);
				throw new  NotAuthorizedException("Token expired");	 
				
			}
	}
	
	public static String getIDfromToken(String token){
		 byte[] encodedKey     = Base64.decodeBase64(API_SECRET);
		 SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		 Claims c = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
		return c.getId();
	}
}
