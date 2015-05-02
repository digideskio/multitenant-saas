package controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

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
	
	@Security.Authenticated(SecuredUser.class)
	public static Result dashboard(){
		
		
		return ok(views.html.user.dashboard
				.render("Welcome", SecuredUser.isLoggedIn(ctx())
						));
	}
	
	@Security.Authenticated(SecuredUser.class)
	public static Result newProject(){
		HashMap<String,String> fieldDetails = new HashMap<String,String>();
				
				try {
					HttpResponse<JsonNode> response = Unirest.get("http://192.168.0.36:8080/user/addproject/?preference_id=2")
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
		return ok(views.html.user.project.newProject
				.render("Create New Project", SecuredUser.isLoggedIn(ctx()), fieldDetails));
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
		String url = "http://192.168.0.36:8080/project/new/?project_id="+projectId+"&&user_id=" + session("userId");
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
	public Static Result allProjects(){
		
		return ok();
		
	}
	
	private static String generateUniqueId() {
	    return RandomStringUtils.randomAlphanumeric(7);
	}


}
