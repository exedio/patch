package com.exedio.cope.patch.example;

import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.misc.ServletUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FrontendServletFilter implements Filter
{
	static final String ATTRIBUTE_NAME_MODEL = "model";

	private ConnectToken connectToken = null;
	private FilterConfig filterConfig = null;

	@Override
	public void init(final FilterConfig filterConfig)
	{
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException
	{

		if (connectToken == null)
		{
			connectToken = ServletUtil.getConnectedModel(this, filterConfig).returnOnFailureOf(connectToken ->	MainPatches.run());
		}
		request.setAttribute(ATTRIBUTE_NAME_MODEL, connectToken.getModel());
		chain.doFilter(request, response);
	}

	@Override
	public void destroy()
	{
		if(connectToken!=null)
		{
			connectToken.returnStrictly();
			connectToken = null;
		}
	}
}
