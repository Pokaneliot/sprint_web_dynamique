package mg.pokaneliot.util;
import java.util.Map;
import java.util.HashMap;

public class ModelAndView {
	
	String url;
	Map<String,Object> data;
	public ModelAndView(){
		 this.data = new HashMap<String,Object>();
	}
	public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String,Object> data){
    	this.data=data;
    }
    public void addObject(String key,Object data) {
        this.data.put(key, data);
    }
}