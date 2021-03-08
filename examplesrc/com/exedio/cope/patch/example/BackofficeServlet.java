package com.exedio.cope.patch.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BackofficeServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException
	{

		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getOutputStream().println("Mock backoffice initialized");
	}


}
