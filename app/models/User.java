package models;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class User {

	private Integer userId;
	

	private String email;
	
	private String name;
	
	private String password;
	
	private Integer preference;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public static User getUserInfo(String userId){
		String message = "";
		User user = null;
		HttpResponse<JsonNode> response;
		try {
			response = Unirest.get("http://192.168.0.36:8080/user/"+ userId)
					  .asJson();
			JSONObject jsonObj = response.getBody().getObject();
			user = new User();
			user.setEmail(jsonObj.getString("email").toString());
			user.setName(jsonObj.getString("name").toString());
			user.setPreference(jsonObj.getInt("preference"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return user;
	}

	public Integer getPreference() {
		return preference;
	}

	public void setPreference(Integer preference) {
		this.preference = preference;
	}
	

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
}
