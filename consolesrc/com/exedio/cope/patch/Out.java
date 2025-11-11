package com.exedio.cope.patch;

import com.exedio.cope.util.XMLEncoder;
import com.exedio.cops.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


final class Out
{
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final StringBuilder sb;

	private final SimpleDateFormat dateFormatFull  = createDateFomat();


	static SimpleDateFormat createDateFomat()
	{
		return new SimpleDateFormat("yyyy-MM-dd'&nbsp;'HH:mm:ss'<small>'.SSS'</small>'", Locale.ENGLISH);
	}

	Out(
			final HttpServletRequest request,
			final HttpServletResponse response)
	{
		this.sb = new StringBuilder();
		this.request = request;
		this.response = response;
	}

	void writeStatic(final String s)
	{
		sb.append(s);
	}

	void write(final String s)
	{
		XMLEncoder.append(sb, s);
	}

	void write(final int i)
	{
		sb.append(i);
	}

	void write(final long l)
	{
		sb.append(l);
	}

	void write(final boolean b)
	{
		sb.append(b);
	}

	void write(final Date date)
	{
		sb.append(dateFormatFull.format(date));
	}

	void write(final Resource resource)
	{
		sb.append(resource.getURL(request));
	}

	void sendBody() throws IOException
	{
		final byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
		response.setContentLength(body.length); // avoid chunked transfer

		try(final ServletOutputStream stream = response.getOutputStream())
		{
			stream.write(body);
		}
	}

}
