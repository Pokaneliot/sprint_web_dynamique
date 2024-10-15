package mg.pokaneliot.util;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Mapping {
	String className;
	Set<VerbAction> listVA;
	
	public Mapping(String cl){
		this.setClassName(cl);
		listVA=new HashSet<VerbAction>();
	}
	public void setClassName(String cl){
		this.className=cl;
	}
	public String getClassName(){
		return this.className;
	}
	public void setListVA(Set<VerbAction> va){
		this.listVA=va;
	}
	public Set<VerbAction> getListVA(){
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