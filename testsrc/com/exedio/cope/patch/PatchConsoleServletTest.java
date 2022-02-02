package com.exedio.cope.patch;

import static com.exedio.cope.junit.Assert.assertFails;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.exedio.cope.Model;
import com.exedio.cope.patch.cope.CopeModel4Test;
import com.exedio.cope.patch.cope.CopeRule.NoTransaction;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

@SuppressWarnings("HardcodedLineSeparator") // ok as line break correspond to jspm
public class PatchConsoleServletTest  extends CopeModel4Test
{
	private static final Model MODEL = PatchTest.MODEL;

	private static final String CONTEXT_PATH = "/context";
	private static final String SERVLET_PATH = "/servlet";

	public PatchConsoleServletTest()
	{
		super(MODEL);
	}

	@NoTransaction
	@Test void notConnectedAfterInit() throws ServletException,IOException
	{
		// disconnect first as we interact with Patches in an assumed disconnected state
		MODEL.disconnect();
		try
		{
			final PatchesBuilder builder = new PatchesBuilder();
			builder.insertAtStart(newSamplePatch("one"));
			final Patches patches = builder.build();
			final TestServlet servlet = new TestServlet(patches);
			servlet.init(createServletConfig());
			assertFalse(servlet.isConnected());

			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Not connected. Connect to see status of mutex and patch runs.",
					call.getDivContent("modelStatus"));
			assertEquals(
					"Mutex: Unknown (not connected)",
					call.getDivContent("mutexStatus"));
			assertEquals(
					"Patches: Unknown (not connected)",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/connect", CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Not connected\n", requestGetStatus(servlet, "model"));
			assertEquals("Unknown (not connected)\n", requestGetStatus(servlet, "mutex"));
			assertEquals("Unknown (not connected)\n", requestGetStatus(servlet, "patches"));
			assertEquals("Unknown (not connected)\n", requestGetStatus(servlet, "schema"));
		}
		finally
		{
			// connect again
			MODEL.connect(getConnectProperties());
		}
	}

	@NoTransaction
	@Test void get() throws ServletException,IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		final TestServlet servlet = new TestServlet(patches);
		servlet.init(createServletConfig());
		servlet.setConnectedOverwrite(Boolean.TRUE);

		final TestHttpCall call = createRequestResponse("GET", "/");
		servlet.doRequest(call.request, call.response);
		verify(call.response).setStatus(HttpServletResponse.SC_OK);
		// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
		assertEquals(
				"Model: Connected.",
				call.getDivContent("modelStatus"));
		assertEquals(
				"Mutex: None",
				call.getDivContent("mutexStatus"));
		assertEquals(
				"Patches: Not Done",
				call.getDivContent("patchesStatus"));
		assertEquals(
				Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
				call.getGlobalActions());
		assertEquals("Connected\n", requestGetStatus(servlet, "model"));
		assertEquals("None\n", requestGetStatus(servlet, "mutex"));
		assertEquals("Not Done\n", requestGetStatus(servlet, "patches"));
		assertEquals("Ok\n", requestGetStatus(servlet, "schema"));

		// check invalid status url
		final TestHttpCall invalidStatusCall = createRequestResponse("GET", "/status/invalid");
		servlet.doRequest(invalidStatusCall.request, invalidStatusCall.response);
		verify(invalidStatusCall.response).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@NoTransaction
	@Test void preempt() throws ServletException,IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		final Patches patches = builder.build();
		final TestServlet servlet = new TestServlet(patches);
		servlet.init(createServletConfig());
		servlet.setConnectedOverwrite(Boolean.TRUE);

		// POST preempt
		{
			final TestHttpCall call = createRequestResponse("POST", "/preempt");
			assertFalse(patches.isDone());
			servlet.doRequest(call.request, call.response);
			assertTrue(patches.isDone());
			verify(call.response).sendRedirect(ArgumentMatchers.eq(CONTEXT_PATH+SERVLET_PATH+"/"));
		}

		// GET after POST
		{
			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Connected.",
					call.getDivContent("modelStatus"));
			assertEquals(
					"Mutex: None",
					call.getDivContent("mutexStatus"));
			assertEquals(
					"Patches: Done",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Connected\n", requestGetStatus(servlet, "model"));
			assertEquals("None\n", requestGetStatus(servlet, "mutex"));
			assertEquals("Done\n", requestGetStatus(servlet, "patches"));
			assertEquals("Ok\n", requestGetStatus(servlet, "schema"));
		}

		// POST preempt again
		{
			final TestHttpCall call = createRequestResponse("POST", "/preempt");
			servlet.doRequest(call.request, call.response);
			assertTrue(patches.isDone());
			verify(call.response).sendRedirect(ArgumentMatchers.eq(CONTEXT_PATH+SERVLET_PATH+"/"));
		}
	}


	@NoTransaction
	@Test void run() throws ServletException,IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		builder.insertAtStart(newSamplePatch("two"));
		final Patches patches = builder.build();
		final TestServlet servlet = new TestServlet(patches);
		servlet.init(createServletConfig());
		servlet.setConnectedOverwrite(Boolean.TRUE);

		// POST run
		{
			final TestHttpCall call = createRequestResponse("POST", "/run");
			assertFalse(patches.isDone());
			servlet.doRequest(call.request, call.response);
			assertTrue(patches.isDone());
			verify(call.response).setContentType("text/plain; charset=UTF-8");
			assertEquals("Start patching, this will continue even if connection is interrupted.\n"
					+ "run s0 1/2 two\n"
					+ ".\n"
					+ "run s0 2/2 one\n"
					+ ".\n"
					+ "ok\n", call.output.toString());
		}

		// GET after POST
		{
			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Connected.",
					call.getDivContent("modelStatus"));
			assertEquals(
					"Mutex: None",
					call.getDivContent("mutexStatus"));
			assertEquals(
					"Patches: Done",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Connected\n", requestGetStatus(servlet, "model"));
			assertEquals("None\n", requestGetStatus(servlet, "mutex"));
			assertEquals("Done\n", requestGetStatus(servlet, "patches"));
			assertEquals("Ok\n", requestGetStatus(servlet, "schema"));
		}

		// POST run again
		{
			final TestHttpCall call = createRequestResponse("POST", "/run");
			servlet.setConnectedOverwrite(Boolean.TRUE);
			servlet.doRequest(call.request, call.response);
			assertTrue(patches.isDone());
			verify(call.response).setContentType("text/plain; charset=UTF-8");
			assertEquals("Start patching, this will continue even if connection is interrupted.\n"
					+ "ok\n", call.output.toString());
		}
	}

	@NoTransaction
	@Test void runFailed() throws ServletException,IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		builder.insertAtStart(newSamplePatch("fail"));
		final Patches patches = builder.build();
		final TestServlet servlet = new TestServlet(patches);
		servlet.init(createServletConfig());
		servlet.setConnectedOverwrite(Boolean.TRUE);

		// POST run
		{
			final TestHttpCall call = createRequestResponse("POST", "/run");
			assertFalse(patches.isDone());
			servlet.doRequest(call.request, call.response);
			assertFalse(patches.isDone());
			verify(call.response).setContentType("text/plain; charset=UTF-8");
			assertThat(call.output.toString(), matchesPattern(
					"Start patching, this will continue even if connection is interrupted.\n"
					+ "run s0 1/2 fail\n"
					+ "java.lang.RuntimeException: failed\n"
					+ "(?:\tat .+\n)*"
					+ "failed\n"));
		}

		// GET after POST
		{
			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Connected.",
					call.getDivContent("modelStatus"));
			assertThat(call.getDivContent("mutexStatus"), matchesPattern(
					"Mutex: Stage: 0 Patches: 2 Host: .+ Finished: .+ SavePoint: FAILURE: not supported by com.exedio.cope.HsqldbDialect"));
			assertEquals(
					"Patches: Failed",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/releaseMutex", CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Connected\n", requestGetStatus(servlet, "model"));
			assertThat(requestGetStatus(servlet, "mutex"), matchesPattern(
					"Stage: 0 Patches: 2 Host: .+ Finished: .+ SavePoint: FAILURE: not supported by com.exedio.cope.HsqldbDialect\\n"));
			assertEquals("Failed\n", requestGetStatus(servlet, "patches"));
			assertEquals("Ok\n", requestGetStatus(servlet, "schema"));
		}

		// POST release mutex
		{
			final TestHttpCall call = createRequestResponse("POST", "/releaseMutex");
			servlet.doRequest(call.request, call.response);
			assertFalse(patches.isDone());
			verify(call.response).sendRedirect(ArgumentMatchers.eq(CONTEXT_PATH+SERVLET_PATH+"/"));
		}

		// GET after POST
		{
			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Connected.",
					call.getDivContent("modelStatus"));
			assertEquals(
					"Mutex: None",
					call.getDivContent("mutexStatus"));
			assertEquals(
					"Patches: Not Done",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Connected\n", requestGetStatus(servlet, "model"));
			assertEquals("None\n", requestGetStatus(servlet, "mutex"));
			assertEquals("Not Done\n", requestGetStatus(servlet, "patches"));
			assertEquals("Ok\n", requestGetStatus(servlet, "schema"));
		}
	}

	@NoTransaction
	@Test void preemptSingle() throws ServletException,IOException
	{
		final PatchesBuilder builder = new PatchesBuilder();
		builder.insertAtStart(newSamplePatch("one"));
		builder.insertAtStart(newSamplePatch("two"));
		final Patches patches = builder.build();
		final TestServlet servlet = new TestServlet(patches);
		servlet.init(createServletConfig());
		servlet.setConnectedOverwrite(Boolean.TRUE);

		// success
		{
			final TestHttpCall call = createRequestResponse("POST", "/preemptSingle");
			when(call.request.getParameter("patchid")).thenReturn("two");
			assertFalse(patches.isDone());
			servlet.doRequest(call.request, call.response);
			assertFalse(patches.isDone());
			verify(call.response).sendRedirect(ArgumentMatchers.eq(CONTEXT_PATH+SERVLET_PATH+"/"));
		}

		// GET after POST
		{
			final TestHttpCall call = createRequestResponse("GET", "/");
			servlet.doRequest(call.request, call.response);
			verify(call.response).setStatus(HttpServletResponse.SC_OK);
			// Note: no check of the content type to be text/html here as not set by doRequest() of the servlet but in doRequestPrivate of the parent CopsServlet which isn't tested here
			assertEquals(
					"Model: Connected.",
					call.getDivContent("modelStatus"));
			assertEquals(
					"Mutex: None",
					call.getDivContent("mutexStatus"));
			assertEquals(
					"Patches: Not Done",
					call.getDivContent("patchesStatus"));
			assertEquals(
					Arrays.asList(CONTEXT_PATH+SERVLET_PATH+"/run", CONTEXT_PATH+SERVLET_PATH+"/preempt"),
					call.getGlobalActions());
			assertEquals("Connected\n", requestGetStatus(servlet, "model"));
			assertEquals("None\n", requestGetStatus(servlet, "mutex"));
			assertEquals("Not Done\n", requestGetStatus(servlet, "patches"));
			assertEquals("Ok\n", requestGetStatus(servlet, "schema"));
		}

		// no param
		{
			final TestHttpCall call = createRequestResponse("POST", "/preemptSingle");
			when(call.request.getParameter("patchid")).thenReturn(null);
			assertFalse(patches.isDone());
			assertFails(() ->
				servlet.doRequest(call.request, call.response),
				NullPointerException.class,
				"Request parameter patchid must not be null");
			assertFalse(patches.isDone());
		}

		// invalid id
		{
			final TestHttpCall call = createRequestResponse("POST", "/preemptSingle");
			when(call.request.getParameter("patchid")).thenReturn("doesNotExist");
			assertFalse(patches.isDone());
			assertFails(() ->
				servlet.doRequest(call.request, call.response),
				NoSuchElementException.class,
				"Patch with id doesNotExist does not exist");
			assertFalse(patches.isDone());
		}

		// valid id but already run
		{
			final TestHttpCall call = createRequestResponse("POST", "/preemptSingle");
			when(call.request.getParameter("patchid")).thenReturn("two");
			assertFalse(patches.isDone());
			servlet.doRequest(call.request, call.response);
			assertFalse(patches.isDone());
		}
	}

	private static final class TestServlet extends PatchConsoleServlet
	{
		private static final long serialVersionUID = 1L;
		private final Patches patches;
		private Boolean connectedOverwrite;

		private TestServlet(final Patches patches)
		{
			this.patches = patches;
		}

		@Override
		public Patches getPatches()
		{
			return patches;
		}

		public void setConnectedOverwrite(final Boolean connectedOverwrite)
		{
			this.connectedOverwrite = connectedOverwrite;
		}

		@Override
		boolean isConnected()
		{
			return connectedOverwrite == null ? super.isConnected() : connectedOverwrite;
		}
	}

	private static ServletConfig createServletConfig()
	{
		final ServletConfig config = mock(ServletConfig.class);
		when(config.getInitParameter("model")).thenReturn("com.exedio.cope.patch.PatchConsoleServletTest#MODEL");
		when(config.getServletName()).thenReturn("TestServlet");
		return config;
	}

	private static TestHttpCall createRequestResponse(final String requestMethod, final String path) throws IOException
	{
		final HttpServletRequest request = mock(HttpServletRequest.class);
		final HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getContextPath()).thenReturn(CONTEXT_PATH);
		when(request.getServletPath()).thenReturn(SERVLET_PATH);
		when(request.getMethod()).thenReturn(requestMethod);
		when(request.getPathInfo()).thenReturn(path);
		final StringBuilder output = new StringBuilder();
		final ServletOutputStream outputStream = new ServletOutputStream()
		{
			@Override
			public void write(final byte[] b, final int off, final int len)
			{
				output.append(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(b, off, len)));
			}

			@Override
			public void write(final int b)
			{
				write(new byte[] { (byte)b }, 0, 1);
			}
		};
		when(response.getOutputStream()).thenReturn(outputStream);
		final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
		when(response.getWriter()).thenReturn(printWriter);
		return new TestHttpCall(request, response, output);
	}

	private static final class TestHttpCall
	{
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private final StringBuilder output;
		private final Pattern divPattern = Pattern.compile("<div\\s+id=\"(.+?)\">(.*?)</div>");
		private final Pattern actionPattern = Pattern.compile("<form\\s+method=\"post\"\\s+class=\"inline\"\\s+action=\"(.+?)\">");

		private TestHttpCall(final HttpServletRequest request, final HttpServletResponse response, final StringBuilder output)
		{
			this.request = request;
			this.response = response;
			this.output = output;
		}

		private String getDivContent(final String id)
		{
			final Matcher matcher = divPattern.matcher(output.toString());
			while (matcher.find())
			{
				final String foundId = matcher.group(1);
				final String foundContent = matcher.group(2);
				if (foundId.equals(id))
					return foundContent;
			}
			return null;
		}

		private List<String> getGlobalActions()
		{
			final ArrayList<String> result = new ArrayList<>();
			final Matcher matcher = actionPattern.matcher(output.toString());
			while (matcher.find())
			{
				result.add(matcher.group(1));
			}
			return result;
		}
	}

	private static SamplePatch newSamplePatch(final String id)
	{
		return new SamplePatch(MODEL, id, null, true);
	}

	/**
	 * Run an HTTP-GET request to the plain text status URL of the given option
	 */
	private static String requestGetStatus(final TestServlet servlet, final String option) throws IOException
	{
		final TestHttpCall call = createRequestResponse("GET", "/status/"+option);
		servlet.doRequest(call.request, call.response);
		verify(call.response).setStatus(HttpServletResponse.SC_OK);
		verify(call.response).setContentType("text/plain; charset=UTF-8");
		return call.output.toString();
	}
}
