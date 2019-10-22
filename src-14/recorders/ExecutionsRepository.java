package recorders;

import java.util.Set;

import util.FileIndex.FileIndexException;

public interface ExecutionsRepository {

	public abstract String newExecution(String string) throws FileIndexException;

	public abstract Set getExecutionsIds() throws FileIndexException;

}