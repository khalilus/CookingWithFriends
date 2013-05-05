/**
 * 
 */
package Database;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;
import Test.MockUser;
import UserInfo.Account;
import UserInfo.Kitchen;
import UserInfo.KitchenName;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

//import sun.security.provider.SecureRandom;
//import org.apache.commons.codec.binary.Base6;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * @author hacheson
 *
 */

public class DBHelper implements DBHelperInterface{
	private Mongo mongo_;
	private DBCollection userCollection_;
	private DB userDB_;
	private DB kitchenDB_;
	private DBCollection kitchenCollection_;
	private DB userPassDB_;
	private DBCollection userPassCollection_;
	
	public DBHelper(){
		Process p = null;
		try{
			p = Runtime.getRuntime().exec("whoami");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    String inputLine;
		    String result = "";
		    while ((inputLine = in.readLine()) != null) {
		        result += inputLine;
		    }
		    in.close();
			String s = "mongod --port 27017 -dbpath /course/cs032/asgn/lab2_git/CWF/" + result + "/Data/";
			String[] args = s.split(" ");
			p = Runtime.getRuntime().exec(s);
		} catch(IOException e){
			e.printStackTrace();
		} 
		try {
			mongo_ = new Mongo("localhost", 27017);
			userDB_ = mongo_.getDB("users");
			userCollection_ = userDB_.getCollection("userCollection");
			kitchenDB_ = mongo_.getDB("kitchens");
			kitchenCollection_ = kitchenDB_.getCollection("kitchenCollection");
			userPassDB_ = mongo_.getDB("usernamePasswords");
			userPassCollection_ = userPassDB_.getCollection("usernamePasswordsCollection");
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Could not connect to mongodb, unknown host.");
			e.printStackTrace();
		} catch (MongoException e) {
			System.err.println("ERROR: Could not connect to mongodb.");
			e.printStackTrace();
		}
		
	/*	storeUsernamePassword("CWF", "cook");
		MockUser mu = new MockUser();
		storeAccount(mu.getAccount());
		for(KitchenName kn: mu.getAccount().getKitchens()){
			Kitchen k = new Kitchen(kn.getName(), kn.getID());
			k.addActiveUser(mu.getAccount());
			storeKitchen(k);
		} */
		
	}

	@Override
	public Account getAccount(String username) {
		System.out.println("Making object in db helper get account.");
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		DBCursor cursor = userCollection_.find(searchQuery);
		
		while (cursor.hasNext()) {
			String s  = cursor.next().get("account").toString();
			return (Account)getObjectFromString(s);
			//System.out.println(cursor.next());
		}
		return null;
	}

	@Override
	public void storeAccount(Account a) {
		BasicDBObject document = new BasicDBObject();
		document.put("username", a.getID());
		document.put("account", getObjectString(a));
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", a.getID());
		//Adds it if it doesn't exist  currently.
		if(userCollection_.find(searchQuery).length() == 0){
			userCollection_.insert(document);
		}
		else{
			userCollection_.remove(searchQuery);
			userCollection_.insert(document);
		}
	}

	@Override
	public Kitchen getKitchen(String id) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("id", id);
		
		DBCursor cursor = kitchenCollection_.find(searchQuery);
		while (cursor.hasNext()) {
			//System.out.println(cursor.next());
			String s = cursor.next().get("kitchen").toString();
			return (Kitchen)getObjectFromString(s);
		}
		return null;
	}

	@Override
	public void storeKitchen(Kitchen k) {
		
		BasicDBObject document = new BasicDBObject();
		document.put("id", k.getID());
		document.put("kitchen", getObjectString(k));
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("id", k.getID());
		
		//Adds it if it doesn't exist  currently.
		if(kitchenCollection_.find(searchQuery).length() == 0){
			System.out.println("kitchen wasn't in DB");
			kitchenCollection_.insert(document);
		}
		//Otherwise remove the current object, and add the new kitchen.
		else{
			kitchenCollection_.remove(searchQuery);
			kitchenCollection_.insert(document);
		}
	}
	
	/**
	 * Changes the username to generate a random password.
	 * @param username
	 */
	public void changePassword(String username, String password){
		storeUsernamePassword(username, password);
	}
	
	public void storeUsernamePassword(String username, String password){
		String encryptedPassword = getEncrypted(password);
		BasicDBObject document = new BasicDBObject();
		document.put("username", username.trim());
		document.put("password", encryptedPassword);
		//document.put("encryptKey", getEncryptedKey(password));
		//Adds it if it doesn't exist  currently.
		//if(uniqueUsername(username)){
		if(!inDatabase(username)){
			userPassCollection_.insert(document);
		}
		
		//Changes the password of the username if it already exists in the method.
		else{
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", username.trim());
			DBCursor cursor = userPassCollection_.find(searchQuery);
			userPassCollection_.remove(searchQuery);
			userPassCollection_.insert(document);
		}
	}
	
	@Override
	public boolean checkUsernamePassword(String username, String password) {
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		DBCursor cursor = userPassCollection_.find(searchQuery);
		//Username doesn't exist in database.
		if(!cursor.hasNext()){
			return false;
		}
		else{
			//System.out.println("next: " + cursor.next());
			String storedPassword = cursor.next().get("password").toString();
			return check(password, storedPassword);
		}
		//encode the password that you're given and check if it matches.
	}
	
	
	/** Checks whether given plaintext password corresponds 
    to a stored salted hash of the password. */
	public static boolean check(String password, String stored){
	    String[] saltAndPass = stored.split("\\$");
	    if (saltAndPass.length != 2)
	        return false;
	    String hashOfInput = hash(password, Base64.decodeBase64(saltAndPass[0]));
	    return hashOfInput.equals(saltAndPass[1]);
	}
	
	/** Computes a salted PBKDF2 hash of given plaintext password
    suitable for storing in a database. 
    Empty passwords are not supported. */
	/**
	 * Returns the encrypted password using the salt and 
	 * @param password String password of encryption.
	 * @return String encyrpted password.
	 */
	//source: http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
	public static String getEncrypted(String password) {
		try{
			int saltLen = 32;
		    byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
		    // store the salt with the password
		    return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
		} catch(Exception e){
			System.out.println("ERROR: In getEncyrpted in DBHelper." + e.getMessage());
		}
		//TODO: Change back to empty string.
		return null;
		
	}
	
    // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
    // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
	//source: http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
    private static String hash(String password, byte[] salt){
    	try{
    		int iterations = 10*1024;
        	int desiredKeyLen = 256;
            if (password == null || password.length() == 0)
                throw new IllegalArgumentException("Empty passwords are not supported.");
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey key = f.generateSecret(new PBEKeySpec(
                password.toCharArray(), salt, iterations, desiredKeyLen)
            );
            return Base64.encodeBase64String(key.getEncoded());
    	} catch(Exception e){
    		System.out.println("ERROR: Unable to hash password." + e.getMessage());
    		return null; //TODO: Make empty string later.
    	}
    	
    	
    }
	
    /**
     * Returns true if it is a valid username, ie if nobody already has that username.
     */
	public boolean uniqueUsername(String username){
		System.out.println("IN DATABASE: " + username);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		DBCursor cursor = userPassCollection_.find(searchQuery);
		System.out.println("CURSOR: " + cursor);
		if(cursor.size() !=0) {
			
			return false;
		}
		return true;
	}
	
    /**
     * Returns true if it is a valid username, ie if nobody already has that username.
     */
	@Override
	public boolean inDatabase(String username){
		System.out.println("IN DATABASE: " + username);
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		DBCursor cursor = userPassCollection_.find(searchQuery);
		System.out.println("CURSOR: " + cursor);
		if(cursor.size() !=0) {
			
			return true;
		}
		return false;
	}
	
	
    /**
     * Returns true if it is a valid username, ie if nobody already has that username.
     * An active user that is already in the database. Does the opposite of 
     * uniqueUsername.
     */
	/*public boolean validUser(String username){
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("username", username);
		//DBCursor cursor = kitchenCollection_.find(searchQuery);
		DBCursor cursor = userPassCollection_.find(searchQuery);
		if(cursor.hasNext()) {
			return true;
		}
		return false;
	}*/
	
	
	
	/**
	 * Creates a new, unique kitchen id by finding if an id is in the db already.
	 * @return String id The new, unique kitchen id.
	 */
	public String createKitchenId(){
		//Generate a random number and see if it's not in the db already.
		while(true){
			int num = (int)(Math.random()*1000000);
			BasicDBObject searchQuery = new BasicDBObject();
			String id = "/k/" + num;
			searchQuery.put("id", id);
			DBCursor cursor = kitchenCollection_.find(searchQuery);
			if(!cursor.hasNext()){
				//return "/k/0";
				return id;
			}	
		
		}
	}
	
	  /** Read the object from Base64 string. */
    private static Object getObjectFromString( String s ) {
    	try{
    		BASE64Decoder decoder = new BASE64Decoder();
        	byte [] data = decoder.decodeBuffer( s );
            ObjectInputStream ois = new ObjectInputStream( 
                                            new ByteArrayInputStream(  data ) );
            Object o  = ois.readObject();
            ois.close();
            return o;
    	} catch(IOException  e){
    		System.out.println("ERROR: Could not convert from object string: " + e.getMessage());
    		return null;
    	} catch(ClassNotFoundException e){
    		System.out.println("ERROR: Could not convert from object string: " + e.getMessage());
    		return null;
    	}
    }
    
	
	/**
	 * Returns the serialized object in string form.
	 * @param o Serializable object o.
	 * @return String form of object.
	 */
	public static String getObjectString(Serializable o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
	        oos.close();
		} catch (IOException e) {
			System.out.println("ERROR: Could not make serializable object." + e.getMessage());
		}
		//Imports all of this so it doesn't conflict with the other Base64 import above.
        return new String(com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(baos.toByteArray()));
    }
}
