/*
 * Copyright (C) 2004-2012  exedio GmbH (www.exedio.com)
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

package com.exedio.cope.live.cope;

import com.exedio.cope.ConnectProperties;
import com.exedio.cope.Model;
import com.exedio.cope.junit.CopeModelTest;
import com.exedio.cope.util.Properties;
import org.junit.After;
import org.junit.Before;

public class CopeModel4Test
{
	private static final class Adaptee extends CopeModelTest
	{
		Adaptee(final Model model, final CopeModel4Test adapter)
		{
			super(model);
			this.adapter = adapter;
		}

		private final CopeModel4Test adapter;

		/**
		 * Just to make them visible to the adapter.
		 */
		@Override
		protected void setUp() throws Exception
		{
			super.setUp();
		}

		/**
		 * Just to make them visible to the adapter.
		 */
		@Override
		protected void tearDown() throws Exception
		{
			super.tearDown();
		}

		@Override
		public ConnectProperties getConnectProperties()
		{
			return adapter.getConnectProperties();
		}

		@Override
		protected boolean doesManageTransactions()
		{
			return adapter.doesManageTransactions();
		}
	}

	private final Adaptee test;

	protected CopeModel4Test(final Model model)
	{
		this.test = new Adaptee(model, this);
	}

	/**
	 * Override this method to provide your own connect properties
	 * to method {@link #setUp()} for connecting.
	 */
	protected ConnectProperties getConnectProperties()
	{
		return new ConnectProperties(Properties.getSystemPropertySource());
	}

	protected boolean doesManageTransactions()
	{
		return true;
	}

	@Before
	public final void setUpModel() throws Exception
	{
		test.setUp();
	}

	@After
	public final void tearDownModel() throws Exception
	{
		test.tearDown();
	}
}
