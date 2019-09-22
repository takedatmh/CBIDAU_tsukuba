package org.apache.catalina.manager;

import java.io.IOException;

import javax.servlet.ServletException;

public class TestManagerServlet {

		public static void main(String[] args) throws IOException, ServletException {
			
			ManagerServlet ms = new ManagerServlet();
			ms.doGet(null, null);
			
		}
		

}
