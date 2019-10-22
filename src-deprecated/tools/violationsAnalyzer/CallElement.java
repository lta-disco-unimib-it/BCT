package tools.violationsAnalyzer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;


public class CallElement {
	private String id;
	private HashMap<String, CallElement> children = new HashMap<String, CallElement>();
	private ArrayList<ViolationPoint> ids = new ArrayList<ViolationPoint>();
	private HashMap<ViolationPoint,Integer> reachableViolations = new HashMap<ViolationPoint, Integer>();
	private CallElement parent;
	
	public CallElement(String id) {
		this.id = id;
	}

	public boolean hasChild(String string) {
		return children.containsKey(string);
	}

	public CallElement getChild(String key) {
		return children.get(key);
	}

	public CallElement addChild(String key, CallElement value) {
		value.setParent(this);
		return children.put(key, value);
	}

	private void setParent(CallElement element) {
		parent = element;
	}

	public void addViolationId(ViolationPoint vp) {
		ids.add( vp );
	}

	public ArrayList<ViolationPoint> getViolationPoints() {
		return ids;
	}

	public boolean hasViolations() {
	
		return ids.size() > 0;
	}

	public String getId() {
		return id;
	}

	public Collection<CallElement> getChildren() {
		return children.values();
	}

	public CallElement getParent() {
		return parent;
	}

	public void accept(CEVisitor v) {
		if ( v.before(this) ){
			for ( CallElement child : getChildren() ){
				child.accept(v);
			}
		}
		v.after(this);
	}

	public void addReachableViolation(ViolationPoint vp, int steps) {
		System.out.println("Adding "+this.id+" : "+vp.getViolation().getId()+" "+steps);
		reachableViolations.put(vp,steps);
	}

	public Set<Entry<ViolationPoint, Integer>> getReachableViolations() {
		return reachableViolations.entrySet();
	}
	
	
}
