package dataflow.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogUtil {
	
	/**
	 * Logger for writing log information by Java API Logger class. 
	 * 
	 * When you use this utility for writing your designated log information, 
	 * coding the following code on your classe's field.
	 * 
	 * <sample>
	 *  Logger.createLogger("Log File path", TargetClassName.class);
	 * 
	 * @param filePath
	 * @param clazz
	 * @return Logger class instance
	 */
	public static Logger createLogger(String filePath, Class clazz){
		
		//Get Logger instance.
		 Logger logger = Logger.getLogger(clazz.getName());
		 
		 try {
			Handler handler =new FileHandler(filePath, true);
			logger.addHandler(handler);
			
			Formatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		
		  LogManager manager = LogManager.getLogManager();
		  manager.readConfiguration(new FileInputStream("logging.properties"));
		  
		} catch (SecurityException e) {
			logger.log(Level.INFO, "Got a security exception.", e);
		} catch (FileNotFoundException e) {
			logger.log(Level.INFO, "Got an file not found exception .", e);
		} catch (IOException e) {
			logger.log(Level.INFO, "Got an IO exception .", e);
		}
		 return logger;
	}
	
	public static void main(String[] args){
		Logger logger = createLogger(".\\Sample.log", LogUtil.class);
		logger.log(Level.INFO, "tset");
	}
 
}
