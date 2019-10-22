package tools.violationsAnalyzer;

import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;

public class ViolDepthFirstIterator extends DepthFirstIterator<String, String> {

	@Override
	protected void encounterVertex(String arg0, String arg1) {
		// TODO Auto-generated method stub
		super.encounterVertex(arg0, arg1);
	}

	@Override
	protected void encounterVertexAgain(String arg0, String arg1) {
		// TODO Auto-generated method stub
		super.encounterVertexAgain(arg0, arg1);
	}

	@Override
	protected boolean isConnectedComponentExhausted() {
		// TODO Auto-generated method stub
		return super.isConnectedComponentExhausted();
	}

	@Override
	protected String provideNextVertex() {
		// TODO Auto-generated method stub
		return super.provideNextVertex();
	}

	public ViolDepthFirstIterator(Graph<String, String> graph) {
		super(graph);
	}

}
