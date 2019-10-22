package traceReaders.raw;

import java.io.Serializable;
import java.util.List;

public interface TokenMetaData extends Serializable {

	public List getCurrentTests();
	
	public List getCurrentActions();
	
	public void setCurrentTests( List tests );
	
	public void setCurrentActions( List actions );
	
	public String getCalledObjectId();
	
	public long getTimestamp();
	
	public void setCalledObjectId( String id );
	
	public List getContextData();

	public void setContextData(List contextData);
}
