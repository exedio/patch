package com.exedio.cope.patch.example;

import com.exedio.cope.misc.ConnectToken;
import java.io.IOException;
import java.io.Serial;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BackofficeServlet extends HttpServlet
{
	@Serial
	private static final long serialVersionUID = 1L;

	private ConnectToken connectToken = null;

	@Override
	public final void init()
	{
		connectToken = MainPatches.getConnectedModel(this);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse response) throws IOException
	{

		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getOutputStream().println("Mock backoffice initialized, Model connected and patches run.");
		response.getOutputStream().println("Model is: "+connectToken.getModel());
	}

	@Override
	public final void destroy()
	{
		if(connectToken!=null)
		{
			connectToken.returnStrictly();
			connectToken = null;
		}
		super.destroy();
	}

}
