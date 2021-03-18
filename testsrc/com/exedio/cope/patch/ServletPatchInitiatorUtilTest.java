package com.exedio.cope.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

public class ServletPatchInitiatorUtilTest
{
	@Test
	void testServlet()
	{
		final ServletConfig config = mock(ServletConfig.class);
		when(config.getServletName()).thenReturn("TestServlet");
		assertEquals("TestServlet", ServletPatchInitiatorUtil.create(config).getInitiator());
	}

	@Test
	void testServletWithRequest()
	{
		final ServletConfig config = mock(ServletConfig.class);
		when(config.getServletName()).thenReturn("TestServlet");
		{
			final HttpServletRequest request = mock(HttpServletRequest.class);
			when(request.getRemoteUser()).thenReturn(null);
			assertEquals("TestServlet / anonymous", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
		{
			final HttpServletRequest request = mock(HttpServletRequest.class);
			when(request.getRemoteUser()).thenReturn("aUserLogin");
			assertEquals("TestServlet / aUserLogin", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
	}

	@Test
	void testFilter()
	{
		final FilterConfig config = mock(FilterConfig.class);
		when(config.getFilterName()).thenReturn("TestFilter");
		assertEquals("TestFilter", ServletPatchInitiatorUtil.create(config).getInitiator());
	}


	@Test
	void testFilterWithRequest()
	{
		final FilterConfig config = mock(FilterConfig.class);
		when(config.getFilterName()).thenReturn("TestFilter");
		{
			final HttpServletRequest request = mock(HttpServletRequest.class);
			when(request.getRemoteUser()).thenReturn(null);
			assertEquals("TestFilter / anonymous", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
		{
			final HttpServletRequest request = mock(HttpServletRequest.class);
			when(request.getRemoteUser()).thenReturn("aUserLogin");
			assertEquals("TestFilter / aUserLogin", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
		// test with non-Http ServletRequest
		{
			final ServletRequest request = mock(ServletRequest.class);
			assertEquals("TestFilter", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
		// test with HttpServletRequest on method with signature of just ServletRequest
		{
			final ServletRequest request = mock(HttpServletRequest.class);
			assertEquals("TestFilter / anonymous", ServletPatchInitiatorUtil.create(config, request).getInitiator());
		}
	}
}
