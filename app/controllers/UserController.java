package controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import models.User;
import modules.Utilities;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import controllers.security.SecuredUser;

public class UserController extends Controller{
	
	private static final String serverUrl = Play.application().configuration().getString("multitenant.server");
	
	//authenticate user
	public static Result authenticate(){
    	DynamicForm dynamicForm = Form.form().bindFromRequest();
    	String email = dynamicForm.get("email");
    	String password = dynamicForm.get("password");
    	String message = "";
    	String userId = "";
    	User user = null;
    	if( email.length() > 0 && password.length() > 0 && email != null && password != null){
    		try {
    			String jsonBody = "{ email: \""+email+"\" ,  password: \"" + Utilities.getMD5Hash(password) +"\" }";
				HttpResponse<JsonNode> response = Unirest.post(serverUrl+"/user/authenticate")
						  .header("accept", "application/json")
						  .body(jsonBody)
						  .asJson();
				
				JSONObject jsonObj = response.getBody().getObject();
				user = new User();
				user.setUserId(jsonObj.getInt("id"));
				user.setEmail(jsonObj.getString("email").toString());
				user.setName(jsonObj.getString("name").toString());
				user.setPreference(jsonObj.getInt("preference"));
				session().clear();
				if( user!= null ){
					session("userId", String.valueOf(user.getUserId()));
					session("preference", String.valueOf(user.getPreference()));
					return redirect(routes.UserController.dashboard());
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    		return redirect(routes.ApplicationController.index());
    	}
    	return redirect(routes.ApplicationController.index());
    }
	
	//render dashboard page
	@Security.Authenticated(SecuredUser.class)
	public static Result dashboard(){
		return ok(views.html.user.dashboard
				.render("Welcome", SecuredUser.isLoggedIn(ctx())
						));
	}
	
	//new project form
	@Security.Authenticated(SecuredUser.class)
	public static Result newProject(){
		HashMap<String,String> fieldDetails = getFieldMap();
		String activityName = getActivityName();
		return ok(views.html.user.project.newProject
				.render("Create New Project", SecuredUser.isLoggedIn(ctx()), fieldDetails, activityName));
	}
	
	//submit new project
	@Security.Authenticated(SecuredUser.class)
	public static Result submitProject(){
		DynamicForm projectForm = Form.form().bindFromRequest();
		Collection<Entry<String, String[]>> taskForm = request().body().asFormUrlEncoded().entrySet();
		Iterator<String[]> itrValue=request().body().asFormUrlEncoded().values().iterator();
		int taskCount = Integer.parseInt(projectForm.get("taskCount"));
		
		String projectId = generateUniqueId();
		String jsonBody = "{ \"project_id\": \""+ projectId +"\" , \"name\": \""+ projectForm.get("projectName")  +"\", "
				+ "\"description\": \""+projectForm.get("projectDescription")+"\"," 
						+ "\"tasks\":[";
		
		for(int pass=0;pass<taskCount;pass++)
		{
			jsonBody=jsonBody + "{";
			Iterator<String> itrKey=request().body().asFormUrlEncoded().keySet().iterator();
			Iterator<String[]> innerItrValue=request().body().asFormUrlEncoded().values().iterator();
			int counter=0;
			int max=request().body().asFormUrlEncoded().values().size();
			while(innerItrValue.hasNext())
			{	
				String key=itrKey.next();
				
				key=key.replace("[", "");
				key=key.replace("]", "");
				
				if( !key.equals("projectName") && !key.equals("projectDescription") && !key.equals("taskCount")){
					jsonBody=jsonBody+"\""+key+"\":";
				}
				String[] values=innerItrValue.next();
				
				if( !key.equals("projectName") && !key.equals("projectDescription") && !key.equals("taskCount")){
					
					if(counter==max-2)
					{
						jsonBody=jsonBody+"\""+values[pass]+"\"";
					}
					else
					{
						jsonBody=jsonBody+"\""+values[pass]+"\",";
					}
					
				}
				counter++;
			}
			
			if(pass==(taskCount-1)){
				jsonBody=jsonBody + "}";
			}	
			else{
				jsonBody=jsonBody + "},";
			}	

		}
				
		jsonBody = jsonBody + "]"
				+ "}";
		System.out.println(jsonBody);
		HttpResponse<JsonNode> response = null;
		String message = "";
		String url = serverUrl+"/project/new/?project_id="+projectId+"&&user_id=" + session("userId");
		System.out.println(url);
		try {
			response = Unirest.post(url)
				  .header("accept", "application/json")
				  .body(jsonBody)
				  .asJson();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return redirect(routes.UserController.allProjects());
	}
	
	//view all projects
	@Security.Authenticated(SecuredUser.class)
	public static Result allProjects(){
		HttpResponse<JsonNode> response = null;
		String url = serverUrl+"/projects/?user_id=" + session("userId");
	
		try {
			response = Unirest.get(url)
				  .header("accept", "application/json")
				  .asJson();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jsonObj = response.getBody().getObject();
		JSONArray projectArray = null;
		
		HashMap<String, String> projectMap = new HashMap<String,String>();
		int count = 0;
		try {
			projectArray = jsonObj.getJSONArray("projectDetails");
			count = projectArray.length();
			for( int i = 0; i < count; i++ ){
				JSONObject project = (JSONObject) projectArray.get(i);
				String projectName = project.getString("name");
				String projectId = project.getString("project_id");
				projectMap.put(projectId, projectName);
			} 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ok(views.html.user.project.all
				.render("All Projects", SecuredUser.isLoggedIn(ctx()), projectMap));
	}
	
	//edit project
	@Security.Authenticated(SecuredUser.class)
	public static Result editProject(String projectId){
		HashMap<String,String> fieldDetails = getFieldMap();
		String activityName = getActivityName();
		HashMap<String, Integer> activitChartMap = new HashMap<String, Integer>();
		HttpResponse<JsonNode> response = null;
		int preference = Integer.parseInt(session("preference"));
		String url = serverUrl+"/project/?user_id="+session("userId")+"&&project_id="+projectId;
		try {
			response = Unirest.get(url)
				  .header("accept", "application/json")
				  .asJson();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, String> projectMap = new HashMap<String, String>();
		JSONArray projectDetails = response.getBody().getObject().getJSONArray("projectDetails");
		JSONObject project = projectDetails.getJSONObject(0);
		projectMap.put("Project Name", project.getString("name"));
		projectMap.put("Project Description", project.getString("description"));
		projectMap.put("Project ID",project.getString("project_id"));
		response = null;
		url = serverUrl+"/project/status/?user_id="+session("userId")+"&&project_id="+projectId + "&&preference_id=" + session("preference");
		System.out.println(url);
		try {
			response = Unirest.get(url)
				  .header("accept", "application/json")
				  .asJson();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<HashMap<String,String>> chartList = new ArrayList<HashMap<String,String>>();
		if( Integer.parseInt(session("preference")) !=2 ){
			JSONArray chartArray = response.getBody().getArray();
			for( int i = 0; i < chartArray.length(); i++ ){
				HashMap<String,String> chartMap = new HashMap<String,String>();
				JSONObject chartObject = chartArray.getJSONObject(i);
				System.out.println(chartObject.toString());
				Iterator<String> it = chartObject.keys();
				while(it.hasNext()){
					String key = it.next();
					String value = String.valueOf(chartObject.get(key));
					chartMap.put(key, value);
				}
				chartList.add(chartMap);
			}
			
		}
		List<HashMap<String,String>> taskList = new ArrayList<HashMap<String,String>>();
		JSONArray taskArray = project.getJSONArray("tasks");
		int taskCount = taskArray.length();
		for(int i = 0; i < taskCount; i++){
			HashMap<String, String> taskMap = new HashMap<String, String>();
			JSONObject taskObject = taskArray.getJSONObject(i);
			Iterator<String> it = taskObject.keys();
			while(it.hasNext()){
				String key = it.next();
				String value = (String) taskObject.get(key);
				taskMap.put(key, value);
			}
			taskList.add(taskMap);
		}
		
		return ok(views.html.user.project.edit
				.render("View Project", SecuredUser.isLoggedIn(ctx()),projectMap, taskList, fieldDetails, activityName, chartList));
	}
	
	//update project
	public static Result updateProject(String projectId){
		DynamicForm projectForm = Form.form().bindFromRequest();
		Collection<Entry<String, String[]>> taskForm = request().body().asFormUrlEncoded().entrySet();
		Iterator<String[]> itrValue=request().body().asFormUrlEncoded().values().iterator();
		int taskCount = Integer.parseInt(projectForm.get("taskCount"));
		String jsonBody = "{ \"project_id\": \""+ projectId +"\" , \"name\": \""+ projectForm.get("projectName")  +"\", "
				+ "\"description\": \""+projectForm.get("projectDescription")+"\"," 
						+ "\"tasks\":[";
		
		for(int pass=0;pass<taskCount;pass++)
		{
			jsonBody=jsonBody + "{";
			Iterator<String> itrKey=request().body().asFormUrlEncoded().keySet().iterator();
			Iterator<String[]> innerItrValue=request().body().asFormUrlEncoded().values().iterator();
			int counter=0;
			int max=request().body().asFormUrlEncoded().values().size();
			while(innerItrValue.hasNext())
			{	
				String key=itrKey.next();
				
				key=key.replace("[", "");
				key=key.replace("]", "");
				
				if( !key.equals("projectName") && !key.equals("projectDescription") && !key.equals("taskCount")){
					jsonBody=jsonBody+"\""+key+"\":";
				}
				String[] values=innerItrValue.next();
				
				if( !key.equals("projectName") && !key.equals("projectDescription") && !key.equals("taskCount")){
					
					if(counter==max-2)
					{
						jsonBody=jsonBody+"\""+values[pass]+"\"";
					}
					else
					{
						jsonBody=jsonBody+"\""+values[pass]+"\",";
					}
					
				}
				counter++;
			}
			
			if(pass==(taskCount-1)){
				jsonBody=jsonBody + "}";
			}	
			else{
				jsonBody=jsonBody + "},";
			}	

		}
				
		jsonBody = jsonBody + "]"
				+ "}";
		System.out.println("UPDATE: " + jsonBody);
		HttpResponse<JsonNode> response = null;
		String message = "";
		String url = serverUrl+"/project/edit/?project_id="+projectId+"&&user_id=" + session("userId");
		System.out.println(url);
		try {
			response = Unirest.post(url)
				  .header("accept", "application/json")
				  .body(jsonBody)
				  .asJson();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return redirect(routes.UserController.editProject(projectId));
	}
	
	
	//generate random unique ID
	private static String generateUniqueId() {
	    return RandomStringUtils.randomAlphanumeric(7);
	}
	
	//get field map based on preference
	private static HashMap<String,String> getFieldMap(){
		HashMap<String,String> fieldDetails = new HashMap<String,String>();
		int preference = Integer.parseInt(session("preference"));
		
		HttpResponse<JsonNode> response = null;
		try {
			response = Unirest.get(serverUrl+"/user/addproject/?preference_id="+session("preference"))
					  .asJson();
			JSONObject jsonObj = response.getBody().getObject();
			Iterator<?> keys = jsonObj.keys();
			while( keys.hasNext() ) {
			    String key = (String)keys.next();
			    fieldDetails.put(key, jsonObj.getString(key));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fieldDetails;
		
	}
	
	//getactivity name by preference
	private static String getActivityName(){
		int preference = Integer.parseInt(session("preference"));
		String activityName = "";
		switch(preference){
			case 1:
				activityName = "Task";
				break;
			case 2:
				activityName = "Sprint";
				break;
				
			case 3:
				activityName = "Card";
				break;
		}
		return activityName;
	}


}
