/*
 * Copyright (C) 2004-2015  exedio GmbH (www.exedio.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.exedio.cope.patch.example;


import com.exedio.cope.Model;
import com.exedio.cope.Transaction;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionLeakFilter implements Filter
{
	private static final Logger logger = LoggerFactory.getLogger(TransactionLeakFilter.class);

	private final Model model = Main.model;


	@Override
	public void doFilter(
			final ServletRequest request,
			final ServletResponse response,
			final FilterChain chain)
	throws IOException, ServletException
	{
		doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
	}

	@SuppressWarnings("MethodMayBeStatic") // false positive, bug in idea
	private void doFilter(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final FilterChain chain)
	throws IOException, ServletException
	{
		chain.doFilter(request, new Response(request, response));
	}

	@Override
	public void init(final FilterConfig filterConfig)
	{
		logger.warn("init");
	}

	@Override
	public void destroy()
	{
		logger.warn("destroy");
	}

	private final class Response extends HttpServletResponseWrapper
	{
		private final HttpServletRequest request;

		Response(
				final HttpServletRequest request,
				final HttpServletResponse response)
		{
			super(response);
			this.request = request;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException
		{
			if(needsLog())
				log("getOutputStream");

			return super.getOutputStream();
		}

		@Override
		public PrintWriter getWriter() throws IOException
		{
			if(needsLog())
				log("getWriter");

			return super.getWriter();
		}

		@Override
		public void sendError(final int sc) throws IOException
		{
			if(needsLog())
				log("sendError(" + sc + ')');

			super.sendError(sc);
		}

		@Override
		public void sendError(final int sc, final String msg) throws IOException
		{
			if(needsLog())
				log("sendError(" + sc + ',' + msg + ')');

			super.sendError(sc,msg);
		}

		@Override
		public void sendRedirect(final String location) throws IOException
		{
			if(needsLog())
				log("sendRedirect(" + location + ')');

			super.sendRedirect(location);
		}

		private boolean needsLog()
		{
			return logger.isWarnEnabled() && model.hasCurrentTransaction();
		}

		private void log(final String name)
		{
			final Transaction tx = model.currentTransaction();
			logger.warn(
					"transaction leaked: {} ({}) at {}-{}-{} in {} ()",
					tx.getID(),
					tx.getName(),
					request.getContextPath(),
					request.getServletPath(),
					request.getPathInfo(),
					name);
		}
	}
}
