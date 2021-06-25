package com.exedio.cope.patch;

import com.exedio.cope.Model;
import com.exedio.cope.TransactionTry;
import com.exedio.cope.misc.ConnectToken;
import com.exedio.cope.misc.ServletUtil;
import com.exedio.cope.util.EmptyJobContext;
import com.exedio.cops.CopsServlet;
import com.exedio.cops.Resource;
import com.exedio.dsmf.Node.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to inspect / run patches
 *
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass") // OK: when used by jspms
public abstract class PatchConsoleServlet extends CopsServlet
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(PatchConsoleServlet.class);

	static final Resource stylesheet = new Resource("patch.css");
	static final Resource script = new Resource("patchconsole.js");
	static final Resource logo = new Resource("logo.png");
	static final Resource shortcutIcon = new Resource("shortcutIcon.png");

	static final String CONNECT_ACTION = "connect";
	static final String RUN_ACTION = "run";
	static final String PREEMPT_ACTION = "preempt";
	static final String PREEMPT_SINGLE_ACTION = "preemptSingle";
	static final String RELEASE_MUTEX_ACTION = "releaseMutex";

	static final String STATUS_PATH = "/status/";

	static final String PARAM_PATCH_ID = "patchid";

	private ConnectToken connectToken = null;

	enum StatusOption
	{
		model,
		mutex,
		patches,
		schema
	}

	private static void setHeaders(final HttpServletResponse response)
	{
		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy
		response.setHeader("Content-Security-Policy",
				"default-src 'none'; " +
				"style-src 'self'; " +
				"script-src 'self'; " +
				"img-src 'self'; " +
				"frame-ancestors 'none'; " +
				"block-all-mixed-content; " +
				"base-uri 'none'");

		// Do not leak information to external servers, not even the (typically private)
		// hostname.
		// We need the referer within the servlet, because typically there is a
		// StrictRefererValidationFilter.
		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referrer-Policy
		response.setHeader("Referrer-Policy", "same-origin");

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options
		response.setHeader("X-Content-Type-Options", "nosniff");

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options
		response.setHeader("X-Frame-Options", "deny");

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection
		response.setHeader("X-XSS-Protection", "1; mode=block");
	}


	@Override
	protected final void doRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException
	{
		setHeaders(response);

		// Post requests are actions
		// after action is performed, we send a redirect to root path of this servlet,
		// exception: executions like run or preempt will send a plain text output of the execution
		if ("POST".equals(request.getMethod()))
		{
			String action = request.getPathInfo();
			if (action != null && action.startsWith("/"))
				action = action.substring(1);

			if (CONNECT_ACTION.equals(action))
			{
				connect();
			}
			else if (RELEASE_MUTEX_ACTION.equals(action))
			{
				connect();
				releasePatchMutex();
			}
			else if (PREEMPT_ACTION.equals(action))
			{
				connect();
				final Patches patches = getPatches();
				patches.preempt(ServletPatchInitiatorUtil.create(getServletConfig(), request));
			}
			else if (PREEMPT_SINGLE_ACTION.equals(action))
			{
				connect();
				final String id = request.getParameter(PARAM_PATCH_ID);
				if (id == null)
					throw new NullPointerException("Request parameter " + PARAM_PATCH_ID + " must not be null");
				final Patches patches = getPatches();
				patches.preempt(id, ServletPatchInitiatorUtil.create(getServletConfig(), request));
			}
			else if (RUN_ACTION.equals(action))
			{
				executePatches(request, response);
				return;
			}

			response.sendRedirect(request.getContextPath() + request.getServletPath() + "/");
			return;
		}

		if (request.getPathInfo().startsWith(STATUS_PATH))
		{
			try
			{
				final StatusOption option = StatusOption.valueOf(request.getPathInfo().substring(STATUS_PATH.length()));
				response.setContentType("text/plain; charset=" + java.nio.charset.StandardCharsets.UTF_8.name());
				response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
				response.setStatus(HttpServletResponse.SC_OK);
				final PrintWriter writer = response.getWriter();
				switch (option)
				{
					case model:
						writer.println(isConnected() ? "Connected" : "Not connected");
						return;
					case mutex:
						writer.println(getPatchMutexInfo());
						return;
					case patches:
						writer.println(getPatchInfo());
						return;
					case schema:
						writer.println(getSchemaInfo());
						return;
				}
			}
			catch (final IllegalArgumentException ignored)
			{
				// ignore invalid enum value here, request with /status/ but no valid option gets redirected later
			}
		}

		// all invalid path's will get redirected to root path of this servlet
		//noinspection LiteralAsArgToStringEquals
		if (!request.getPathInfo().equals("/"))
		{
			response.sendRedirect(request.getContextPath() + request.getServletPath() + "/");
			return;
		}

		// send the Status JSPM
		response.setStatus(HttpServletResponse.SC_OK);
		final Out out = new Out(request, response);
		Status_Jspm.write(this, out, request);
		out.sendBody();
	}

	/**
	 * Executes patches using the PatchExecutor and send a plain text output of the execution to client
	 */
	private void executePatches(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException
	{
		response.setContentType("text/plain; charset=" + java.nio.charset.StandardCharsets.UTF_8.name());
		response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_OK);

		final PrintWriterPatchExecution execution = new PrintWriterPatchExecution(response.getWriter());
		try
		{
			connect();
			final Patches patches = getPatches();
			response.getWriter()
						.println("Start patching, this will continue even if connection is interrupted.");
			patches.run(execution, ServletPatchInitiatorUtil.create(getServletConfig(), request));
			execution.notifySuccess();
		}
		catch (final RuntimeException ex)
		{
			execution.notifyFail(ex);
			// we catch and print the RuntimeExceptions here
			logger.error("Exception on patch execution", ex);
		}
	}

	/**
	 * Returns the Patches managed by this servlet.
	 */
	public abstract Patches getPatches();

	private static Model getModel()
	{
		return PatchMutex.TYPE.getModel();
	}

	boolean isConnected()
	{
		return connectToken != null;
	}

	void connect()
	{
		if (!isConnected())
		{
			connectToken = ServletUtil.getConnectedModel(this);
			getModel().reviseIfSupportedAndAutoEnabled();
		}
	}

	@Override
	public final void destroy()
	{
		if (connectToken != null)
		{
			connectToken.returnStrictly();
			connectToken = null;
		}
		super.destroy();
	}

	String getPatchInfo()
	{
		switch(getPatches().getDone())
		{
			case NOT_CONNECTED: return "Unknown (not connected)";
			case PENDING: return isPatchMutexPresent() ? "Failed" : "Not Done";
			case RUNNING: return "Currently executing";
			case DONE: return "Done";
			default:
				throw new RuntimeException();
		}
	}

	String getPatchMutexInfo()
	{
		if (!isConnected())
			return "Unknown (not connected)";
		final Model model = getModel();
		try (TransactionTry tx = model.startTransactionTry("AbstractPatchServlet#getPatchMutexInfo"))
		{
			final List<PatchMutex> seachResult = PatchMutex.TYPE.search();
			final PatchMutex mutex = seachResult.isEmpty() ? null : seachResult.get(0);
			return tx.commit( mutex == null ? "None"
					: String.format(Locale.ENGLISH, "Stage: %d Patches: %d Host: %s Finished: %tF %tT SavePoint: %s",
							mutex.getStage(), mutex.getNumberOfPatches(), mutex.getHost(), mutex.getFinished(),
							mutex.getFinished(), mutex.getSavepoint()));
		}
	}

	String getSchemaInfo()
	{
		if (!isConnected())
			return "Unknown (not connected)";
		// we could return Node.COLOR.name() instead of the switch but then we miss when this enum is refactored
		final Color cumulativeColor = getModel().getVerifiedSchema().getCumulativeColor();
		switch(cumulativeColor)
		{
			case OK: return "Ok";
			case WARNING: return "Warning";
			case ERROR: return "Error";
			default:
				throw new RuntimeException("Unexpected schema color: "+cumulativeColor);
		}
	}

	List<PatchView> getPatchList()
	{
		// TODO allow to sort by stage?!

		// copy into own map as we use remove method later
		final Map<String, Patch> patchesMap = new LinkedHashMap<>(getPatches().getPatches());

		if (isConnected())
		{
			final Model model = getModel();
			try (final TransactionTry tx = model.startTransactionTry("AbstractPatchServlet#getPatchList"))
			{
				final List<PatchRun> patchRuns = PatchRun.TYPE.search(null, PatchRun.finished, true);
				final List<SchemaPatchRun> schemaPatchRuns = SchemaPatchRun.TYPE.search(null, SchemaPatchRun.finished, true);

				return tx.commit(constructPatchList(patchesMap, patchRuns, schemaPatchRuns));
			}
		}
		else
			return constructPatchList(patchesMap, Collections.emptyList(), Collections.emptyList());
	}

	private static List<PatchView> constructPatchList(final Map<String, Patch> patchesMap, final List<PatchRun> patchRuns,
			final List<SchemaPatchRun> schemaPatchRuns)
	{
		final List<PatchView> result = new ArrayList<>();
		final Map<String, List<PatchViewSchema>> schemaViewsByPatch = new LinkedHashMap<>();

		for (final SchemaPatchRun schemaPatchRun : schemaPatchRuns)
		{
			final PatchViewSchema view = new PatchViewSchema(schemaPatchRun);
			if (!schemaViewsByPatch.containsKey(schemaPatchRun.getPatch()))
				schemaViewsByPatch.put(schemaPatchRun.getPatch(), new ArrayList<>());
			schemaViewsByPatch.get(schemaPatchRun.getPatch()).add(view);
		}

		// add the patches from database with the associated java patch (if found)
		for (final PatchRun patchRun : patchRuns)
		{
			final String id = patchRun.getPatch();
			result.add(new PatchView(id, patchesMap.remove(id), patchRun, schemaViewsByPatch.remove(id)));
		}

		// add the java patches left
		for (final Map.Entry<String, Patch> patchEntry : patchesMap.entrySet())
		{
			result.add(new PatchView(patchEntry.getKey(), patchEntry.getValue(), null,
					schemaViewsByPatch.remove(patchEntry.getKey())));
		}

		// newest patches on top
		Collections.reverse(result);

		// schema patch runs with no java patch and no patch run are added to the bottom
		for (final Map.Entry<String, List<PatchViewSchema>> entry : schemaViewsByPatch.entrySet())
		{
			result.add(new PatchView(entry.getKey(), null, null, entry.getValue()));
		}
		return result;
	}

	boolean isPatchMutexPresent()
	{
		final Model model = getModel();
		try (TransactionTry tx = model.startTransactionTry("AbstractPatchServlet#isPatchMutexPresent"))
		{
			return tx.commit(PatchMutex.TYPE.newQuery().total())>0;
		}
	}

	void releasePatchMutex()
	{
		final Model model = getModel();
		try (TransactionTry tx = model.startTransactionTry("AbstractPatchServlet#releasePatchMutex"))
		{
			PatchMutex.release();
			tx.commit();
		}
	}


	/**
	 * View class for a patch, identified by patch id and has sub views for the Java class of a patch
	 * the database PatchRun of a patch and the SchemaPatchRun's of a patch.
	 * All sub views are optional.
	 */
	static class PatchView
	{
		private final String id;
		private final PatchViewJava javaView;
		private final PatchViewDatabase databaseView;
		private final List<PatchViewSchema> schemaViews;

		PatchView(final String id, final Patch patch, final PatchRun run, final List<PatchViewSchema> schemaViews)
		{
			this.id = id;
			this.javaView = patch == null ? null : new PatchViewJava(patch);
			this.databaseView = run == null ? null : new PatchViewDatabase(run);
			this.schemaViews = schemaViews == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(schemaViews));
		}

		String getId()
		{
			return id;
		}

		PatchViewJava getJavaView()
		{
			return javaView;
		}

		PatchViewDatabase getDatabaseView()
		{
			return databaseView;
		}

		List<PatchViewSchema> getSchemaViews()
		{
			return schemaViews;
		}
	}

	/**
	 * View on the Java aspect of a patch, especially which class (StalePatch, SchemaPatch or something else).
	 */
	static class PatchViewJava
	{
		private final String className;
		private final int stage;
		private final boolean transactionally;

		PatchViewJava(final Patch patch)
		{
			String fullName;
			{
				if (patch instanceof StalePatch)
					fullName = StalePatch.class.getName();
				else if (patch instanceof SchemaPatch)
					fullName = SchemaPatch.class.getName();
				else
					fullName = patch.getClass().getName();
			}
			if (fullName.lastIndexOf('.') >= 0)
				fullName = fullName.substring(fullName.lastIndexOf('.') + 1);
			className = fullName;
			stage = patch.getStage();
			transactionally = patch.isTransactionally();
		}

		String getClassName()
		{
			return className;
		}

		int getStage()
		{
			return stage;
		}

		boolean isTransactionally()
		{
			return transactionally;
		}
	}

	/**
	 * View on the Database aspect of a patch, if patch was run or preempted.
	 */
	static class PatchViewDatabase
	{
		private final int stage;
		private final boolean transactionally;
		private final String host;
		private final String savepoint;
		private final Date finished;
		private final Long elapsed;

		PatchViewDatabase(final PatchRun patchRun)
		{
			stage = patchRun.getStage();
			transactionally = patchRun.getIsTransactionally();
			host = patchRun.getHost();
			savepoint = patchRun.getSavepoint();
			finished = patchRun.getFinished();
			elapsed = patchRun.getElapsed();
		}

		int getStage()
		{
			return stage;
		}

		boolean isTransactionally()
		{
			return transactionally;
		}

		String getHost()
		{
			return host;
		}

		String getSavepoint()
		{
			return savepoint;
		}

		Date getFinished()
		{
			//noinspection ReturnOfDateField
			return finished;
		}

		Long getElapsed()
		{
			return elapsed;
		}
	}

	/**
	 * View on a SQL statement of a SchemaPatch.
	 */
	static class PatchViewSchema
	{
		private final int position;
		private final String sql;
		private final int rows;
		private final Date finished;
		private final Long elapsed;

		PatchViewSchema(final SchemaPatchRun schemaRun)
		{
			position = schemaRun.getPosition();
			rows = schemaRun.getRows();
			sql = schemaRun.getSql();
			finished = schemaRun.getFinished();
			elapsed = schemaRun.getElapsed();
		}

		int getPosition()
		{
			return position;
		}

		String getSql()
		{
			return sql;
		}

		int getRows()
		{
			return rows;
		}

		Date getFinished()
		{
			//noinspection ReturnOfDateField
			return finished;
		}

		Long getElapsed()
		{
			return elapsed;
		}
	}

	/**
	 * JobContext which delegates output additionally to a PrintWriter (of the response)
	 * Additional requirement is that output must end with 'ok' if execution was successful and must not
	 * end with 'ok' if failed.
	 */
	private static class PrintWriterPatchExecution extends EmptyJobContext
	{
		private final PrintWriter writer;

		PrintWriterPatchExecution(final PrintWriter writer)
		{
			this.writer = writer;
		}

		@Override
		public void setMessage(final String message)
		{
			writer.println(message);
			writer.flush();
		}

		@Override
		public void incrementProgress(final int delta)
		{
			super.incrementProgress(delta);
			writer.println(String.join("", Collections.nCopies(delta, ".")));
			writer.flush();
		}

		@Override
		public void setCompleteness(final double completeness)
		{
			super.setCompleteness(completeness);
			writer.println(String.format(Locale.ENGLISH, "%.1f%%", completeness * 100d));
			writer.flush();
		}

		@Override
		public boolean supportsMessage()
		{
			return true;
		}

		@Override
		public boolean supportsCompleteness()
		{
			return true;
		}

		@Override
		public boolean supportsProgress()
		{
			return true;
		}

		void notifySuccess()
		{
			writer.println("ok");
		}

		void notifyFail(final Exception ex)
		{
			ex.printStackTrace(writer);
			// ensure the last line is no 'ok'
			writer.println("failed");
		}
	}
}
