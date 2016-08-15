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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

/**
 * 基于{@linkplain Proxy}的接口实例构建器。
 * 
 * @author earthangry@gmail.com
 * @date 2015年12月3日
 *
 */
public class ProxyImplementeeBeanBuilder implements ImplementeeBeanBuilder
{
	private ImplementeeMethodInvocationInfoEvaluator implementeeMethodInvocationInfoEvaluator;

	public ProxyImplementeeBeanBuilder()
	{
		super();
		this.implementeeMethodInvocationInfoEvaluator = new DefaultImplementeeMethodInvocationInfoEvaluator();
	}

	public ImplementeeMethodInvocationInfoEvaluator getImplementeeMethodInvocationInfoEvaluator()
	{
		return implementeeMethodInvocationInfoEvaluator;
	}

	public void setImplementeeMethodInvocationInfoEvaluator(
			ImplementeeMethodInvocationInfoEvaluator implementeeMethodInvocationInfoEvaluator)
	{
		this.implementeeMethodInvocationInfoEvaluator = implementeeMethodInvocationInfoEvaluator;
	}

	@Override
	public Object build(Implementation implementation,
			Map<Class<?>, ? extends Collection<?>> implementorBeansMap)
	{
		return doBuild(implementation,
				new SimpleImplementorBeanFactory(implementorBeansMap));
	}

	@Override
	public Object build(Implementation implementation,
			ImplementorBeanFactory implementorBeanFactory)
	{
		return doBuild(implementation, implementorBeanFactory);
	}

	/**
	 * 构建接口实例。
	 * 
	 * @param implementation
	 * @param implementorBeanFactory
	 * @return
	 */
	protected Object doBuild(Implementation implementation,
			ImplementorBeanFactory implementorBeanFactory)
	{
		Class<?> implementee = implementation.getImplementee();
		if (!implementee.isInterface())
			throw new IllegalArgumentException("[implementee] must be an interface");

		Object proxy = Proxy.newProxyInstance(implementee.getClassLoader(),
				new Class<?>[] { implementee, ProxyImplementee.class },
				new ProxyImplementeeInvocationHandler(implementation,
						implementorBeanFactory,
						this.implementeeMethodInvocationInfoEvaluator));

		return proxy;
	}

	protected static class ProxyImplementeeInvocationHandler extends AbstractImplementeeBeanInvocationHandler
			implements InvocationHandler
	{
		private Implementation implementation;

		private ImplementorBeanFactory implementorBeanFactory;

		public ProxyImplementeeInvocationHandler()
		{
			super();
		}

		public ProxyImplementeeInvocationHandler(Implementation implementation,
				ImplementorBeanFactory implementorBeanFactory,
				ImplementeeMethodInvocationInfoEvaluator implementeeMethodInvocationInfoEvaluator)
		{
			super(implementeeMethodInvocationInfoEvaluator);
			this.implementation = implementation;
			this.implementorBeanFactory = implementorBeanFactory;
		}

		public Implementation getImplementation()
		{
			return implementation;
		}

		public void setImplementation(Implementation implementation)
		{
			this.implementation = implementation;
		}

		public ImplementorBeanFactory getImplementorBeanFactory()
		{
			return implementorBeanFactory;
		}

		public void setImplementorBeanFactory(
				ImplementorBeanFactory implementorBeanFactory)
		{
			this.implementorBeanFactory = implementorBeanFactory;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			if (Object.class.equals(method.getDeclaringClass()))
				return method.invoke(this, args);

			ImplementeeMethodInvocationInfo invocationInfo = evaluateImplementeeMethodInvocationInfo(
					this.implementation, method, args,
					this.implementorBeanFactory);

			if (invocationInfo == null)
				throw new UnsupportedOperationException("No valid implement is found for [" + method + "]");

			Method implementMethod = invocationInfo.getImplementMethodInfo().getImplementMethod();
			Object implementBean = invocationInfo.getImplementorBean();

			return implementMethod.invoke(implementBean, args);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [implementee="
					+ this.implementation.getImplementee() + "]";
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((implementation == null) ? 0
					: implementation.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProxyImplementeeInvocationHandler other = (ProxyImplementeeInvocationHandler) obj;
			if (implementation == null)
			{
				if (other.implementation != null)
					return false;
			}
			else if (!implementation.equals(other.implementation))
				return false;
			return true;
		}
	}
}