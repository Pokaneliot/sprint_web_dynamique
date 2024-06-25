package mg.pokaneliot.util;

public class Mapping {
	String className;
	String methodName;
	
	public Mapping(String cl, String meth){
		this.setClassName(cl);
		this.setMethodName(meth);
	}
	public void setClassName(String cl){
		this.className=cl;
	}
	public String getClassName(){
		return this.className;
	}
	public void setMethodName(String meth){
		this.methodName=meth;
	}
	public String getMethodName(){
		return this.methodName;
	}
	
}