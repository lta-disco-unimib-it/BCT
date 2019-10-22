package recorders;

//import conf.DataRecorderSettings;
import java.io.File;
import java.lang.reflect.Field;

import conf.EnvironmentalSetter;
//import conf.ViolationsRecorderSettings;

public class RecorderFactory {
	public static final FileDataRecorder loggingRecorder = getLoggingRecorder();
	
//	private static ViolationsRecorder violationsRecorder = null;
	
//	Does not work
//	public static synchronized void setLoggingRecorder(DataRecorder recorder ) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
//		Field recorderField = RecorderFactory.class.getField("loggingRecorder");
//		recorderField.set(null, recorder);
//	}
	
	/**
	 * This method return a DataRecorder as configured in BCT.properties file
	 * The method is synchronized since we want a common recorder for all threads
	 * 
	 * @return
	 */
	private static synchronized FileDataRecorder getLoggingRecorder(){
		FileDataRecorder loggingRecorder;
		
				
			
		loggingRecorder = new FileDataRecorder();
		String bctHome = EnvironmentalSetter.getBctHome();

		loggingRecorder.init(new File( bctHome+"/DataRecording"));
			
			
		
		return loggingRecorder;
	}

	
}
