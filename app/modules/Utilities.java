package modules;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Utilities {
	
	//get md5 hash
	public static String getMD5Hash(String s){
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.update(s.getBytes(),0,s.length());
		return new BigInteger(1,m.digest()).toString(16);
	}
	
	public static boolean isNumber(String number){
		return Pattern.matches("[0-9]+", number);
	}
	
	public static boolean isValidDate(Date maybeDate, String format){
		boolean isValid = false;
		org.joda.time.DateTime date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			String stringDate = sdf.format(maybeDate );
			System.out.println(stringDate);
			DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		    date =  fmt.parseDateTime(stringDate);
		    isValid = true;
		  } catch (Exception e) {
			  
		  }
		  return isValid;
	}
	
	public static boolean isValidUsername(String username){
		  Pattern pattern = Pattern.compile("^[a-z0-9_-]{3,15}$");
		  Matcher matcher = pattern.matcher(username);
		  if( username.length() > 6 && matcher.matches() ){
			  return true;
		  }
		  return false;
	}

}
