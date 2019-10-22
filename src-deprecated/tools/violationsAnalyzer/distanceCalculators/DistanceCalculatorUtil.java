package tools.violationsAnalyzer.distanceCalculators;

public class DistanceCalculatorUtil {

	/**
	 * Given a value that represent a distance between two nodes, returns the distance value without considering the line arch.
	 *  
	 * @return
	 */
	public static double calculateDistanceWithoutLines( double completeDistance ){
		return Math.floor(completeDistance/2);
	}
}
