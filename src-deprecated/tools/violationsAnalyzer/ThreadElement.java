package tools.violationsAnalyzer;
import java.util.Collection;
import java.util.HashMap;


public class ThreadElement {

	HashMap<String, CallElement> children = new HashMap<String, CallElement>();
	private String threadID;
	
	public ThreadElement(String threadID) {
		this.threadID = threadID;
	}

	public boolean hasChild(String string) {
		return children.containsKey(string);
	}

	public CallElement getChild(String key) {
		return children.get(key);
	}

	public CallElement addChild(String key, CallElement value) {
		return children.put(key, value);
	}

	public Collection<CallElement> getChildren() {
		return children.values();
	}

	public String getThreadID() {
		return threadID;
	}

	
}
