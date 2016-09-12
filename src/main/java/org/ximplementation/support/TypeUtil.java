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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for type.
 * 
 * @author earthangry@gmail.com
 * @date 2016-9-1
 *
 */
public class TypeUtil
{
	private TypeUtil()
	{
	}

	/**
	 * Returns the wrapper type of the specified type if it is a primitive,
	 * returns itself if not.
	 * 
	 * @param type
	 * @return
	 */
	public static Class<?> toWrapperType(Class<?> type)
	{
		if (!type.isPrimitive())
			return type;
		else if (boolean.class.equals(type))
			return Boolean.class;
		else if (byte.class.equals(type))
			return Byte.class;
		else if (char.class.equals(type))
			return Character.class;
		else if (double.class.equals(type))
			return Double.class;
		else if (float.class.equals(type))
			return Float.class;
		else if (int.class.equals(type))
			return Integer.class;
		else if (long.class.equals(type))
			return Long.class;
		else if (short.class.equals(type))
			return Short.class;
		else
			return type;
	}

	/**
	 * Resolve type parameter map.
	 * <p>
	 * It resolves {@linkplain TypeVariable}s and their mapped types of a
	 * specified type hierarchy.
	 * </p>
	 * <p>
	 * For example :
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * class A&lt;T&gt;
	 * {
	 * }
	 *
	 * class B extends A&lt;Integer&gt;
	 * {
	 * }
	 * 
	 * class C&lt;U extends Serializable&gt; extends A&lt;U&gt;
	 * {
	 * }
	 * 
	 * class D extends C&lt;Integer&gt;
	 * {
	 * }
	 * </code>
	 * </pre>
	 * <p>
	 * The result of {@code resolve(B.class)} will be :
	 * </p>
	 * 
	 * <pre>
	 * T  ---&gt;  Integer
	 * </pre>
	 * <p>
	 * And the result of {@code resolve(D.class)} will be :
	 * </p>
	 * 
	 * <pre>
	 * T  ---&gt;  U
	 * U  ---&gt;  Integer
	 * </pre>
	 * 
	 * @param type
	 * @return
	 */
	public static Map<TypeVariable<?>, Type> resolveTypeParams(Type type)
	{
		Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();

		doResolveTypeParams(type, map);

		return map;
	}

	/**
	 * Do resolve type parameters.
	 * 
	 * @param type
	 * @param typeVariablesMap
	 */
	protected static void doResolveTypeParams(Type type,
			Map<TypeVariable<?>, Type> typeVariablesMap)
	{
		if (type == null)
			return;
		else if (type instanceof Class<?>)
		{
			Class<?> clazz = (Class<?>) type;

			// super interfaces
			Type[] genericInterfaces = clazz.getGenericInterfaces();
			if (genericInterfaces != null)
			{
				for (Type t : genericInterfaces)
					doResolveTypeParams(t, typeVariablesMap);
			}

			// super class
			Type genericSuperType = clazz.getGenericSuperclass();
			Class<?> superClass = clazz.getSuperclass();
			while (superClass != null && !Object.class.equals(superClass))
			{
				doResolveTypeParams(genericSuperType, typeVariablesMap);

				genericSuperType = superClass.getGenericSuperclass();
				superClass = superClass.getSuperclass();
			}

			// outer class
			Class<?> outerClass = clazz;
			while (outerClass != null && outerClass.isMemberClass())
			{
				Type genericOuterType = outerClass.getGenericSuperclass();
				doResolveTypeParams(genericOuterType, typeVariablesMap);

				outerClass = outerClass.getEnclosingClass();
			}
		}
		else if (type instanceof ParameterizedType)
		{
			ParameterizedType pt = (ParameterizedType) type;

			if (pt.getRawType() instanceof Class<?>)
			{
				Type[] actualTypeArgs = pt.getActualTypeArguments();
				TypeVariable<?>[] typeVariables = ((Class<?>) pt.getRawType())
						.getTypeParameters();

				for (int i = 0; i < actualTypeArgs.length; i++)
				{
					TypeVariable<?> var = typeVariables[i];
					Type value = actualTypeArgs[i];

					typeVariablesMap.put(var, value);
				}
			}
		}
	}

	/**
	 * Returns the final type of a specified {@linkplain TypeVariable}.
	 * <p>
	 * For example, it will returns {@code Integer} for {@code T} of the
	 * following map :
	 * </p>
	 * 
	 * <pre>
	 * T  ---&gt;  U
	 * U  ---&gt;  Integer
	 * </pre>
	 * 
	 * @param typeVariable
	 * @param typeVariablesMap
	 * @return
	 */
	public static Type getFinalType(TypeVariable<?> typeVariable,
			Map<TypeVariable<?>, Type> typeVariablesMap)
	{
		Type finalType = typeVariable;
	
		if (typeVariablesMap != null)
		{
			while (finalType instanceof TypeVariable<?>)
			{
				Type ttype = typeVariablesMap.get(finalType);
	
				if (ttype == null)
					break;
	
				finalType = ttype;
			}
		}
	
		return finalType;
	}

	/**
	 * Returns if the type parameter in super class is overridden-equals with
	 * the type parameter in sub class.
	 * <p>
	 * See JLS chapters about overridden.
	 * </p>
	 * 
	 * @param superTypeParam
	 * @param subTypeParam
	 * @param subTypeParams
	 * @return
	 */
	public static boolean isOverriddenEquals(Type superTypeParam, Type subTypeParam,
			Map<TypeVariable<?>, Type> subTypeParams)
	{
		// get the final type first
		if (superTypeParam instanceof TypeVariable<?> && subTypeParam != null)
		{
			superTypeParam = getFinalType(
					(TypeVariable<?>) superTypeParam, subTypeParams);
		}
	
		if (superTypeParam.equals(subTypeParam))
		{
			return true;
		}
		else if (superTypeParam instanceof TypeVariable<?>)
		{
			if (!(subTypeParam instanceof TypeVariable<?>))
				return false;
	
			Type[] abounds = ((TypeVariable<?>) superTypeParam).getBounds();
			Type[] bbounds = ((TypeVariable<?>) subTypeParam).getBounds();
	
			if (abounds.length != bbounds.length)
				return false;
			
			for(int i=0; i<abounds.length; i++)
			{
				if (!isOverriddenEquals(abounds[i], bbounds[i],
						subTypeParams))
					return false;
			}
			
			return true;
		}
		else if (superTypeParam instanceof WildcardType)
		{
			if (!(subTypeParam instanceof WildcardType))
				return false;
	
			Type[] auppers = ((WildcardType) superTypeParam).getUpperBounds();
			Type[] buppers = ((WildcardType) subTypeParam).getUpperBounds();
	
			if (auppers.length != buppers.length)
				return false;
	
			for (int i = 0; i < auppers.length; i++)
			{
				if (!isOverriddenEquals(auppers[i], buppers[i],
						subTypeParams))
					return false;
			}
	
			Type[] alowers = ((WildcardType) superTypeParam).getLowerBounds();
			Type[] blowers = ((WildcardType) subTypeParam).getLowerBounds();
	
			if (alowers.length != blowers.length)
				return false;
	
			for (int i = 0; i < alowers.length; i++)
			{
				if (!isOverriddenEquals(alowers[i], blowers[i],
						subTypeParams))
					return false;
			}
	
			return true;
		}
		else if (superTypeParam instanceof ParameterizedType)
		{
			if (!(subTypeParam instanceof ParameterizedType))
				return false;
	
			ParameterizedType pa = (ParameterizedType) superTypeParam;
			ParameterizedType pb = (ParameterizedType) subTypeParam;
	
			if (!isNullableEquals(pa.getRawType(), pb.getRawType()))
				return false;
	
			if (!isNullableEquals(pa.getOwnerType(), pb.getOwnerType()))
				return false;
	
			Type[] paa = pa.getActualTypeArguments();
			Type[] pba = pb.getActualTypeArguments();
	
			if (paa.length != pba.length)
				return false;
	
			for (int i = 0; i < paa.length; i++)
			{
				if (!isOverriddenEquals(paa[i], pba[i], subTypeParams))
					return false;
			}
	
			return true;
		}
		else if (superTypeParam instanceof GenericArrayType)
		{
			if (!(subTypeParam instanceof GenericArrayType))
				return false;
	
			Type acmp = ((GenericArrayType) superTypeParam).getGenericComponentType();
			Type bcmp = ((GenericArrayType) subTypeParam).getGenericComponentType();
	
			return isOverriddenEquals(acmp, bcmp, subTypeParams);
		}
		else
			return false;
	}

	/**
	 * Returns if the two objects are equals for null resolved.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean isNullableEquals(Object a, Object b)
	{
		if (a == null)
			return (b == null);
		else
			return a.equals(b);
	}
}
