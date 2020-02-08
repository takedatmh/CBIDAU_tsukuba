package sample.functionA;

public class MainA {

	public static void main(String[] args) {
		
		MethodsA methodsA = new MethodsA();
		try {
			methodsA.method01(1, 20);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
