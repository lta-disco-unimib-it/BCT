package tools.violationsAnalyzer;

import modelsViolations.BctModelViolation;

import org.jgrapht.graph.DefaultWeightedEdge;

public class BctViolationEdge extends DefaultWeightedEdge {

	private BctModelViolation modelViolation;

	public BctModelViolation getModelViolation() {
		return modelViolation;
	}

	public void setModelViolation(BctModelViolation modelViolation) {
		this.modelViolation = modelViolation;
	}

	public BctViolationEdge(BctModelViolation modelViolation) {
		this.modelViolation = modelViolation;
	}

}
