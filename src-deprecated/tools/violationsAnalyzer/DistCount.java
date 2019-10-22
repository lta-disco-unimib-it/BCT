package tools.violationsAnalyzer;

public class DistCount {
	private int count = 0;
	private double value = 0;
	
	void addValue( double totalDist ){
		++count;
		value += totalDist;
	}

	public Double getAvg() {
		return (double)value/(double)count;
	}

	public int getCount() {
		return count;
	}
}
