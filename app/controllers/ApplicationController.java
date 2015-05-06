package controllers;

import java.util.HashMap;

import models.User;
import modules.Utilities;

import org.json.JSONObject;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.FormData;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;


public class ApplicationController extends Controller {

	private static final String serverUrl = Play.application().configuration().getString("multitenant.server");
	
	public static Result index() {
    	return ok(views.html.index
        		.render("Welcome to Multitenant SaaS demo"));
    }
  
	//authenticate user
    
    
    //register
    public static Result register(){
    	return ok(views.html.register
    			.render("New Registration"));
    }
    
    //register new user
    public static Result submitUser(){
    	DynamicForm registerForm = Form.form().bindFromRequest();
    	String email = registerForm.get("email");
    	String password = registerForm.get("password");
    	String rePassword = registerForm.get("rePassword");
    	String name = registerForm.get("name");
    	String preference = registerForm.get("preference");
    	String message = "";
    	if( email.length() > 0 && email!= null && password.length() > 0 && password!= null && rePassword.length() > 0 && rePassword!= null 
    			&& name.length() > 0 && name != null ){
    		if( password.equals(rePassword) ){
    			HttpResponse<JsonNode> response;
    			String jsonBody = "{ email: \""+email+"\" , name: \""+ name +"\" , password: \"" + Utilities.getMD5Hash(password) +"\" , preference:\""+ preference +"\"  }";
				try {
					response = Unirest.post(serverUrl+"/user/register")
						  .header("accept", "application/json")
						  .body(jsonBody)
						  .asJson();
					message = response.getBody().getObject().getString("message").toString();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return ok(views.html.thanks
	  						.render("Some Problem", true));
				}
	  			if( message.equals("Success") ){
	  				return ok(views.html.thanks
	  						.render("Registration Complete", false));
	  			}else{
	  				return ok(views.html.thanks
	  						.render("Some Problem", true));
	  			}
	  			
    		}
    		
    	}
    	return ok(registerForm.get("email"));
    }
    
    //test
    public static Result test(){
    	return ok();
    }
    
    public static Result logout(){
    	session().clear();
    	return redirect(routes.ApplicationController.index());
    }
    
    public static Result thanks(){
    	return ok();
    }

}
