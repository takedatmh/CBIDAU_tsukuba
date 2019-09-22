package simple.client;

import simple.logic.Logic;

public class Client {
	
	private static String code;
	
	public static void main(String[] args) {
		
		Logic logic = new Logic(0, "0", null, "SmallSample.log");
		
		code = logic.create();
		System.out.println(code);
		
		code = logic.read();
		System.out.println(code);
		
		code = logic.update("UPDATE");
		System.out.println(code);
		
		code = logic.delete();
		System.out.println(code);

		
	}
	


}
