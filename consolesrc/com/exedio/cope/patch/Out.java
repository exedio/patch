package com.exedio.cope.patch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


final class Out
{
	private HttpServletRequest request;
	private HttpServletResponse response;
	private final StringBuilder bf;

	Out(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		this.bf = new StringBuilder();
		this.request = request;
		this.response = response;
	}

	void writeStatic(final String s)
	{
		bf.append(s);
	}

	void write(final String s)
	{
		bf.append(s);
	}

	void sendBody() throws IOException
	{
		byte[] body = bf.toString().getBytes(StandardCharsets.UTF_8);
		response.setContentLength(body.length); // avoid chunked transfer

		try(final ServletOutputStream stream = response.getOutputStream())
		{
			stream.write(body);
		}
	}
}
