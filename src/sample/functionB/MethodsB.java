package sample.functionB;

public class MethodsB {
	
	public CallerFromB caller = null;
	
	public MethodsB(){
		this.caller = new CallerFromB();
	}
	
	public String method01(int a, int b) throws Exception {
		
		String str = null;
		
		if(a + b > 10){
			int ret = caller.update(a);
			str = String.valueOf(ret);
		} else if(a -b < 0){
			str = caller.read(a + b);
		} else {
			caller.create();
			throw new Exception();
		}
		
		return str;
		
	}
	
}
