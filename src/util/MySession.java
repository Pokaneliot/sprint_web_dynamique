package mg.pokaneliot.util;
import jakarta.servlet.http.HttpSession;

public class MySession {
	HttpSession session;

	public Object get(String key){
		return session.getAttribute(key);
	}

	public void add(String key,Object object){
		session.setAttribute(key,object);
	}
	public void delete(String key){
		session.removeAttribute(key);
	}


}