package mg.pokaneliot.util;


public class VerbAction {
	String action;
	String verb;
	
	// Constructor
	public VerbAction(String act,String v){
		this.setAction(act);
		this.setVerb(v);
	}
	public void setAction(String meth){
		this.action=meth;
	}
	public String getAction(){
		return this.action;
	}
	public void setVerb(String verb){
		this.verb=verb;
	}
	public String getVerb(){
		return this.verb;
	}
	
}