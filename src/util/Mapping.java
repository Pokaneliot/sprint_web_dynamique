package mg.pokaneliot.util;
import java.util.ArrayList;


public class Mapping {
	String className;
	ArrayList<VerbAction> listVA;
	
	public Mapping(String cl){
		this.setClassName(cl);
		listVA=new ArrayList<VerbAction>();
	}
	public void setClassName(String cl){
		this.className=cl;
	}
	public String getClassName(){
		return this.className;
	}
	public void setListVA(ArrayList<VerbAction> va){
		this.listVA=va;
	}
	public ArrayList<VerbAction> getListVA(){
		return this.listVA;
	}
	public void addVA(VerbAction va){
		listVA.add(va);
	}
	public boolean contains(String verb){
		for (VerbAction va:listVA) {
			if (va.getVerb().compareToIgnoreCase(verb)==0) {
				return true;
			}
		}
		return false;
	}
	public String getMethodName(String verb){
		for (VerbAction va:listVA) {
			if (va.getVerb().compareToIgnoreCase(verb)==0) {
				return va.getAction();
			}
		}
		return null;
	}
	
}