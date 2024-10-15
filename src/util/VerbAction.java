package mg.pokaneliot.util;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerbAction verbAction = (VerbAction) o;
        return Objects.equals(action, verbAction.action) && Objects.equals(verb, verbAction.verb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, verb);
    }
	
}