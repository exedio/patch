package com.exedio.cope.patch.example;

import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.misc.ServletUtil;
import com.exedio.cope.patch.ServletPatchInitiatorUtil;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

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
			connectToken = ServletUtil.getConnectedModel(this, filterConfig).returnOnFailureOf(connectToken ->	MainPatches.run(ServletPatchInitiatorUtil.create(filterConfig, request)));
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
