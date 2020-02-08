package sample.functionB;

import sample.functionA.MethodsA;

public class MainB {

	public static void main(String[] args) {
		MethodsB methodsB = new MethodsB();
		try {
			methodsB.method01(1, 20);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
