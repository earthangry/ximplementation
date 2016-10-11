/**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */

package org.ximplementation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementationResolver;
import org.ximplementation.support.ImplementeeBeanBuilder;
import org.ximplementation.support.ImplementorBeanFactory;
import org.ximplementation.support.ProxyImplementeeBeanBuilder;
import org.ximplementation.support.SimpleImplementorBeanFactory;

/**
 * Test case for showing concepts and design of this framework.
 * 
 * @author earthangry@gmail.com
 * @date 2015-12-5
 *
 */
public class FrameworkTest
{
	private ImplementationResolver implementationResolver;
	private ImplementeeBeanBuilder implementeeBeanBuilder;

	@Before
	public void setUp() throws Exception
	{
		this.implementationResolver = new ImplementationResolver();
		this.implementeeBeanBuilder = new ProxyImplementeeBeanBuilder();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test()
	{
		@SuppressWarnings("rawtypes")
		Implementation<TService> implementation = this.implementationResolver
				.resolve(
				TService.class, TServiceImplDefault.class, TServiceImplSpecial.class,
				TServiceImplInteger.class, TServiceImplDouble.class);

		ImplementorBeanFactory implementorBeanFactory = SimpleImplementorBeanFactory
				.valueOf(new TServiceImplDefault<Number>(),
						new TServiceImplSpecial<Number>(),
						new TServiceImplInteger(), new TServiceImplDouble());

		@SuppressWarnings("unchecked")
		TService<Number> tservice = this.implementeeBeanBuilder
				.build(implementation,
						implementorBeanFactory);

		{
			String re = tservice.handle(1.0F, 2.0F);
			Assert.assertEquals(TServiceImplDefault.MY_RE, re);
		}

		{
			String re = tservice.handle(1.0F, TServiceImplSpecial.B);
			Assert.assertEquals(TServiceImplSpecial.MY_RE, re);
		}

		{
			String re = tservice.handle(1, 2);
			Assert.assertEquals(TServiceImplInteger.MY_RE, re);
		}

		{
			String re = tservice.handle(1.0D, 2.0D);
			Assert.assertEquals(TServiceImplDouble.MY_RE, re);
		}
	}

	public static interface TService<T extends Number>
	{
		String handle(T a, T b);
	}

	public static class TServiceImplDefault<T extends Number>
			implements TService<T>
	{
		public static final String MY_RE = TServiceImplDefault.class
				.getSimpleName();

		@Override
		public String handle(T a, T b)
		{
			return MY_RE;
		}
	}

	public static class TServiceImplSpecial<T extends Number>
			implements TService<T>
	{
		public static final String MY_RE = TServiceImplSpecial.class
				.getSimpleName();

		public static final Number B = new Float(11.1F);

		@Validity("isValid")
		@Override
		public String handle(T a, T b)
		{
			return MY_RE;
		}

		public boolean isValid(@Index(1) T b)
		{
			return B.equals(b);
		}
	}

	public static class TServiceImplInteger implements TService<Integer>
	{
		public static final String MY_RE = TServiceImplInteger.class
				.getSimpleName();

		@Override
		@Implement("handle")
		public String handle(Integer a, Integer b)
		{
			return MY_RE;
		}
	}

	@Implementor(TService.class)
	public static class TServiceImplDouble
	{
		public static final String MY_RE = TServiceImplDouble.class
				.getSimpleName();

		@Implement("handle")
		public String handle(@Index(1) Double b)
		{
			return MY_RE;
		}
	}
}
