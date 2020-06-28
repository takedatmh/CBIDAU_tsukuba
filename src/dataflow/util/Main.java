package dataflow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {
	  /**
	   * Iterate through each line of input.
	   */
	  public static void main(String[] args) throws IOException {
	    InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
	    BufferedReader in = new BufferedReader(reader);
	    String line;
	    while ((line = in.readLine()) != null) {
	      System.out.println(line);
	    }
	    
	    int[] array = {1};
	    System.out.println(array[0]);
	  }
	}


