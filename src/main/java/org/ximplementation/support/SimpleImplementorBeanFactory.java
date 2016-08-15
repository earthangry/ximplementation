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

import java.util.Collection;
import java.util.Map;

/**
 * 简单实现者Bean工厂。
 * 
 * @author earthangry@gmail.com
 * @date 2016年8月15日
 *
 */
public class SimpleImplementorBeanFactory implements ImplementorBeanFactory
{
	private Map<Class<?>, ? extends Collection<?>> implementorBeansMap;
	
	public SimpleImplementorBeanFactory()
	{
		super();
	}

	public SimpleImplementorBeanFactory(
			Map<Class<?>, ? extends Collection<?>> implementorBeansMap)
	{
		super();
		this.implementorBeansMap = implementorBeansMap;
	}

	public Map<Class<?>, ? extends Collection<?>> getImplementorBeansMap()
	{
		return implementorBeansMap;
	}

	public void setImplementorBeansMap(
			Map<Class<?>, ? extends Collection<?>> implementorBeansMap)
	{
		this.implementorBeansMap = implementorBeansMap;
	}

	@Override
	public Collection<?> getImplementorBeans(Class<?> implementor)
	{
		return (this.implementorBeansMap == null ? null
				: this.implementorBeansMap.get(implementor));
	}
}