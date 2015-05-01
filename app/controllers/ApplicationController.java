package controllers;

import java.util.HashMap;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.FormData;

import com.fasterxml.jackson.databind.JsonNode;


public class ApplicationController extends Controller {

    public static Result index() {
    	System.out.println(Play.application().configuration().getString("multitenant.server"));
        return ok("Index");
    }
    
    public static Result login(){
    	FormData formData = new FormData();
    	String requestType = "login";
    	HashMap<String, String> formFields =  new HashMap<String, String>();
    	formFields.put("username", "text");
    	formFields.put("password", "password");
    	formData.setFormFields(formFields);
    	return ok(views.html.login
    			.render("My App", formData, requestType));
	}
    
    
    public static Result authenticate(){
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
    	return ok(dynamicForm.get("username"));
    }
    
    //register
    public static Result register(){
    	return ok(views.html.register
    			.render("New Registration"));
    }
    
    //register new user
    public static Result submitUser(){
    	DynamicForm registerForm = Form.form().bindFromRequest();
    	return ok(registerForm.get("email"));
    }
    
    //test
    public static Result test(){
//    	String url = "http://192.168.0.36:8080/testController";
//    	Promise<JsonNode> jsonPromise = WS.url(url).get().map(
//    	        new Function<WSResponse, JsonNode>() {
//    	            public JsonNode apply(WSResponse response) {
//    	                JsonNode json = response.asJson();
//    	                return json;
//    	            }
//    	        }
//    	);
//    	
//    	String email = jsonPromise.get(OK).findPath("email").textValue();
//    	System.out.println("*****" + email);
    	
    	return ok();
    }

}
