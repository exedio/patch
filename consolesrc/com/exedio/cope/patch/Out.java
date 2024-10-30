package com.exedio.cope.patch;

import com.exedio.cope.util.XMLEncoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.exedio.cops.Resource;


final class Out
{
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final StringBuilder bf;

	private final SimpleDateFormat dateFormatFull  = createDateFomat();


	static SimpleDateFormat createDateFomat()
	{
		return new SimpleDateFormat("yyyy-MM-dd'&nbsp;'HH:mm:ss'<small>'.SSS'</small>'", Locale.ENGLISH);
	}

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
		XMLEncoder.append(bf, s);
	}

	void write(final int i)
	{
		bf.append(i);
	}

	void write(final long l)
	{
		bf.append(l);
	}

	void write(final boolean b)
	{
		bf.append(b);
	}

	void write(final Date date)
	{
		bf.append(dateFormatFull.format(date));
	}

	void write(final Resource resource)
	{
		bf.append(resource.getURL(request));
	}

	void sendBody() throws IOException
	{
		final byte[] body = bf.toString().getBytes(StandardCharsets.UTF_8);
		response.setContentLength(body.length); // avoid chunked transfer

		try(final ServletOutputStream stream = response.getOutputStream())
		{
			stream.write(body);
		}
	}

}
