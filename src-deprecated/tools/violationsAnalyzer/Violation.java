package tools.violationsAnalyzer;
import java.util.HashSet;

/**
 * This class represent a model violation
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class Violation {
	private HashSet<TestCaseInfo> set = new HashSet<TestCaseInfo>();
	private String id;
	private String name;
	
	public Violation(String id, String name) {
		this.id = id;
		this.name = name; 
	}

	public void add(TestCaseInfo tc) {
		set.add(tc);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getTcOccurencies() {
		return set.size();
	}

	public int getTcPassOccurrencies() {
		int x = 0;
		for ( TestCaseInfo tc : set ){
			if ( tc.isPassed() )
				++x;
		}
		return x;
	}

}
