package org.apache.catalina.manager;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Wrapper;

public class ManagerServletTest extends HttpServlet implements ContainerServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static HttpServletRequest request = null;
	static HttpServletResponse response = null;
	
	public static void main(String[] args) throws IOException, ServletException {
		ManagerServlet ms = new ManagerServlet();
		ms.doGet(request, response);
	}

	@Override
	public Wrapper getWrapper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWrapper(Wrapper wrapper) {
		// TODO Auto-generated method stub
		
	}

}
