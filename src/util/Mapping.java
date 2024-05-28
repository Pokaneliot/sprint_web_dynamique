package mg.pokaneliot.util;

public class Mapping {
	String className;
	String methodeName;
	
	public Mapping(String cl, string meth){
		this.setClassName(cl);
		this.setMethodeName(meth);
	}
	public void setClassName(String cl){
		this.className=cl;
	}
	public String getClassName(){
		return this.className;
	}
	public void setMethodeName(String meth){
		this.methodeName=meth;
	}
	public String getMethodeName(){
		return this.methodeName;
	}
	
}