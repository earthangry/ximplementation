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

package org.ximplementation.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ximplementation.Implement;
import org.ximplementation.Implementor;
import org.ximplementation.NotImplement;
import org.ximplementation.support.ProxyImplementeeBeanBuilder.ProxyImplementeeInvocationHandler;

/**
 * {@linkplain ProxyImplementeeInvocationHandler} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-12-15
 *
 */
public class ProxyImplementeeInvocationHandlerTest extends AbstractTestSupport
{
	private ImplementationResolver implementationResolver;

	private ProxyImplementeeBeanBuilder proxyImplementeeBeanBuilder;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception
	{
		this.implementationResolver = new ImplementationResolver();
		this.proxyImplementeeBeanBuilder = new ProxyImplementeeBeanBuilder();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void invokeTest() throws Throwable
	{
		Implementation<Implementee> implementation = this.implementationResolver
				.resolve(Implementee.class, Implementor0.class,
						Implementor1.class);
		ImplementorBeanFactory implementorBeanFactory = SimpleImplementorBeanFactory
				.valueOf(new Implementor0(), new Implementor1());

		Implementee implementee = this.proxyImplementeeBeanBuilder
				.build(implementation, implementorBeanFactory);

		ProxyImplementeeInvocationHandler handler = (ProxyImplementeeInvocationHandler) Proxy
				.getInvocationHandler(implementee);

		// if (isEqualsMethod(method))
		{
			Method implementeeMethod = getMethodByName(implementee.getClass(),
					"equals");

			boolean equals = (Boolean) handler.invoke(implementee,
					implementeeMethod, new Object[] { implementee });

			Assert.assertTrue(equals);
		}

		// if (isHashCodeMethod(method))
		{
			Method implementeeMethod = getMethodByName(implementee.getClass(),
					"hashCode");

			int hashCode = (Integer) handler.invoke(implementee,
					implementeeMethod, new Object[] {});

			Assert.assertEquals(handler.hashCode(), hashCode);
		}

		// if (isToStringMethod(method))
		{
			Method implementeeMethod = getMethodByName(implementee.getClass(),
					"toString");

			String toString = (String) handler.invoke(implementee,
					implementeeMethod,
					null);

			Assert.assertEquals(
					ProxyImplementeeInvocationHandler.class.getSimpleName()
							+ " [implementee="
					+ implementation.getImplementee() + "]", toString);
		}

		{
			Method implementeeMethod = getMethodByName(Implementee.class,
					"plus");

			Number re = (Number) handler.invoke(implementee, implementeeMethod,
					new Object[] { 1, 2 });

			Assert.assertEquals(Implementor1.RE, re);
		}
	}

	@Test
	public void equalsTest() throws Throwable
	{
		Implementation<Implementee> implementation = this.implementationResolver
				.resolve(Implementee.class, Implementor0.class,
						Implementor1.class);
		ImplementorBeanFactory implementorBeanFactory = SimpleImplementorBeanFactory
				.valueOf(new Implementor0(), new Implementor1());

		Implementee implementee = this.proxyImplementeeBeanBuilder
				.build(implementation, implementorBeanFactory);

		ProxyImplementeeInvocationHandler handler = (ProxyImplementeeInvocationHandler) Proxy
				.getInvocationHandler(implementee);

		Assert.assertTrue(handler.equals(handler));
		Assert.assertFalse(handler.equals(null));
		Assert.assertTrue(handler.equals(new ProxyImplementeeInvocationHandler(
				implementation, implementorBeanFactory,
				this.proxyImplementeeBeanBuilder
						.getImplementeeMethodInvocationFactory())));
		Assert.assertTrue(handler.equals(implementee));
		Assert.assertFalse(handler.equals(new Object()));
	}

	public static interface Implementee
	{
		Number plus(Number a, Number b);

		Number minus(Number a, Number b);
	}

	public static class Implementor0 implements Implementee
	{
		public static final int RE = 1;

		@Override
		public Number plus(Number a, Number b)
		{
			return RE;
		}

		@NotImplement
		@Override
		public Number minus(Number a, Number b)
		{
			return null;
		}
	}

	@Implementor(Implementee.class)
	public static class Implementor1
	{
		public static final int RE = 2;

		@Implement
		public Number plus(Integer a, Integer b)
		{
			return RE;
		}
	}
}
