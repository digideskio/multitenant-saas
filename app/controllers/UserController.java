package controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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
	
	@Security.Authenticated(SecuredUser.class)
	public static Result dashboard(){
		
		
		return ok(views.html.user.dashboard
				.render("Welcome", SecuredUser.isLoggedIn(ctx())
						));
	}
	
	@Security.Authenticated(SecuredUser.class)
	public static Result newProject(){
		HashMap<String,String> fieldDetails = new HashMap<String,String>();
				int preference = Integer.parseInt(session("preference"));
				try {
					HttpResponse<JsonNode> response = Unirest.get(serverUrl+"/user/addproject/?preference_id=2")
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
				String activityName = "";
				switch(preference){
					case 1:
						activityName = "Task";
						break;
					case 2:
						activityName = "Sprint";
						break;
						
					case 3:
						activityName = "XYZ";
						break;
				}
		return ok(views.html.user.project.newProject
				.render("Create New Project", SecuredUser.isLoggedIn(ctx()), fieldDetails, activityName));
	}
	
	 
	
	@Security.Authenticated(SecuredUser.class)
	public static Result submitProject(){
		DynamicForm projectForm = Form.form().bindFromRequest();
		Collection<Entry<String, String[]>> taskForm = request().body().asFormUrlEncoded().entrySet();
		Iterator<String[]> itrValue=request().body().asFormUrlEncoded().values().iterator();
		String projectId = generateUniqueId();
		String jsonBody = "{ \"project_id\": \""+ projectId +"\" , \"name\": \""+ projectForm.get("projectName")  +"\", "
				+ "\"description\": \""+projectForm.get("projectDescription")+"\"," 
						+ "\"tasks\":[";
		Iterator<String[]> test=request().body().asFormUrlEncoded().values().iterator();
		String[] te=test.next();
		int n = te.length;

		
		for(int pass=0;pass<=n;pass++)
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
				if( !key.equals("projectName") && !key.equals("projectDescription")){
					jsonBody=jsonBody+"\""+key+"\":";
				}
				String[] values=innerItrValue.next();
				if( !key.equals("projectName") && !key.equals("projectDescription")){
					if(counter==max-1)
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
			if(pass==n)
			jsonBody=jsonBody + "}";
			else
			jsonBody=jsonBody + "},";


		}
				
		jsonBody = jsonBody + "]"
				+ "}";
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
		
		System.out.println(jsonBody);
		return ok(jsonBody);
	}
	
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
	
	@Security.Authenticated(SecuredUser.class)
	public static Result editProject(String projectId){
		HttpResponse<JsonNode> response = null;
		String url = serverUrl+"/project/edit";
		try {
			response = Unirest.post(url)
				  .header("accept", "application/json")
				  .field("user_id", session("userId"))
				  .field("project_id", projectId)
				  .asJson();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return ok(response.getStatusText().toString());
	}
	
	private static String generateUniqueId() {
	    return RandomStringUtils.randomAlphanumeric(7);
	}


}
