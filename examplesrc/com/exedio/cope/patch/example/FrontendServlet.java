package com.exedio.cope.patch.example;

import java.io.IOException;
import java.io.Serial;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontendServlet extends HttpServlet
{
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getOutputStream().println("Mock frontend, model is: "+request.getAttribute(FrontendServletFilter.ATTRIBUTE_NAME_MODEL));
	}
}
