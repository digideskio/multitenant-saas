package controllers.security;

import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;
import controllers.routes;

public class SecuredUser extends Security.Authenticator{
	
	@Override
	public Result onUnauthorized(Context context) {
	    return redirect(routes.ApplicationController.index()); 
	}
	
	@Override
	public String getUsername(Context ctx){
		return ctx.session().get("userId");
	}
	
	public static String getUserId(Context ctx) {
		return ctx.session().get("userId");
	}
	
	public static boolean isLoggedIn(Context ctx) {
		return (getUserId(ctx) != null);
	}
	
	public static User getAdminInfo(Context ctx){
		
		return (isLoggedIn(ctx)? User.getUserInfo(getUserId(ctx)): null);
	}

}
