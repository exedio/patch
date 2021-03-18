package com.exedio.cope.patch;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public final class ServletPatchInitiatorUtil
{
	private ServletPatchInitiatorUtil()
	{
	}

	public static PatchInitiator create(final ServletConfig servletConfig)
	{
		return new PatchInitiator(servletConfig.getServletName());
	}

	public static PatchInitiator create(final ServletConfig servletConfig, final HttpServletRequest request)
	{
		final String user = request.getRemoteUser();
		return new PatchInitiator(servletConfig.getServletName() + " / " + (user != null ? user : "anonymous"));
	}

	public static PatchInitiator create(final FilterConfig filterConfig)
	{
		return new PatchInitiator(filterConfig.getFilterName());
	}

	public static PatchInitiator create(final FilterConfig filterConfig, final ServletRequest request)
	{
		return request instanceof HttpServletRequest ? create(filterConfig, (HttpServletRequest) request) : create(filterConfig);
	}

	public static PatchInitiator create(final FilterConfig filterConfig, final HttpServletRequest request)
	{
		final String user = request.getRemoteUser();
		return new PatchInitiator(filterConfig.getFilterName() + " / " + (user != null ? user : "anonymous"));
	}
}
