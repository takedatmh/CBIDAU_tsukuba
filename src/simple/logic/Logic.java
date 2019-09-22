package simple.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logic {

	/**
	 * System Flag 0: System Open 1: System Close
	 */
	public int flag;

	/**
	 * Return Code 0: Success 254: Fail
	 */
	protected String code;

	/**
	 * Store data from read from file system.
	 */
	private List<String> data;

	/**
	 * Logger instance
	 */
	public Logger logger = Logger.getLogger("LOGGER");

	/**
	 * Initialize these data when user create this class instance in their
	 * client program.
	 * 
	 * @param flag
	 * @param code
	 * @param data
	 * @param logPath
	 * @throws SecurityException
	 * @throws IOException
	 */
	public Logic(int flag, String code, List<String> data, String logPath) {
		this.flag = flag;
		this.code = code;
		this.data = data;

		Handler handler;
		try {
			handler = new FileHandler(logPath);
			logger.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create an instance.
	 * 
	 * @return : success 0, false 254 as return code;
	 */
	public String create() {

		try {
			// Execute create an instance.
			data = new ArrayList<String>();

			// Set return code.
			code = "0";

		} catch (Exception e) {
			// Logging
			logger.log(Level.WARNING, "Fail to execute create method.");

			// Set return code.
			code = "254";
		}

		// Logging
		logger.log(Level.INFO, "Success create method execution.");

		// Return code.
		return code;
	}

	/**
	 * Read data from file and put this data into data object.
	 * 
	 * @return success 0, false 254;
	 */
	public String read() {

		//Read test.txt file.
		try {
			File file = new File("test.txt");
			FileReader filereader = new FileReader(file);

			int ch;
			while ((ch = filereader.read()) != -1) {
				data.add(String.valueOf((char)ch));
				logger.log(Level.INFO, "Read data which is " + String.valueOf((char)ch) + ".");
			}

			filereader.close();
		} catch (FileNotFoundException e) {
			//Close system
			flag = 1;
			
			//logger
			logger.log(Level.WARNING, e.toString());
			
			//Set return code
			return code = "254";
			
		} catch (IOException e) {
			//Close system
			flag = 1;
			
			//logger
			logger.log(Level.WARNING, e.toString());
			
			//Set return code
			return code = "254";
		}
		
		//Logger
		logger.log(Level.INFO, "Success read method execution.");

		//Return code
		return code;
	}

	public String update(String update_data) {

		//update
		data.set(0, "UPDATED");
		
		//logger
		logger.log(Level.INFO, "Success to update list data.");
		
		return code;
	}

	public String delete() {

		//delete
		data.remove(0);
		
		//logger
		logger.log(Level.INFO, "Success to delete list data.");
		
		return code;
	}

	/**
	 * For soot analysis.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//TODO nothing.
	}

}
