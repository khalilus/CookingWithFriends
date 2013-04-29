/**
 * 
 */
package UserInfo;

import java.io.Serializable;
import java.util.HashSet;

/**
 * @author hacheson
 * The class for a user, has address information about the user.
 */
public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashSet<String[]> _kitchens;
	private String _address;
	private String _id;
	//private String _password;
	//private Preferences _preferences;
	private HashSet<String> _restrictions;
	
	public User(HashSet<String[]> kitchensPairs, String address, String id, HashSet<String> restrictions){
		_kitchens = kitchensPairs;
		_address = address;
		_id = id;
		_restrictions = restrictions;
		//_password = password;
	}
	
	public User(String id){
		_id = id;
		//_password = password;
		_kitchens = new HashSet<String[]>();
		_restrictions = new HashSet<String>();
	}
	
	/**
	 * Returns the user's address.
	 * @return String user's address.
	 */
	public String getAddress(){
		return _address;
	}
	
	/**
	 * Returns the user name.
	 * @return String the user's name.
	 */
	public String getID(){
		return _id;
	}
	
	/**
	 * Returns the password.
	 * @return String password.
//	 */
//	public String getPassword(){
//		return _password;
//	}
	
	/**
	 * Adds a kitchen to the user.
	 * @param k Kitchen the user's kitchen.
	 */
	public void addKitchen(String[] kID){
		_kitchens.add(kID);
	}
	
	/**
	 * Removes a kitchen from the user.
	 * @param k Kitchen kitchen to remove.
	 */
	public void removeKitchen(String kID){
		_kitchens.remove(kID);
	}
	
	/**
	 * Returns a list of kitchens to which the user belongs.
	 */
	public HashSet<String[]> getKitchens(){
		return _kitchens;
	}
	
	public HashSet<String> getRestrictions(){
		return _restrictions;
	}
	
	public void addRestrictions(String restriction){
		_restrictions.add(restriction);
	}
	
	public void setRestrictions(HashSet<String> restrictions){
		_restrictions = restrictions;
	}
	
	/**
	 * Sets the user's preferences.
	 * @param pref Preferences for the user to set.
	 */
//	public void setPreferences(Preferences pref){
//		_preferences = pref;
//	}
	
	public void setAddress(String add){
		_address = add;
	}

	@Override
	public String toString() {
		return "User [_kitchens=" + _kitchens + ", _address=" + _address
				+  "]";
	}
	
	
}
