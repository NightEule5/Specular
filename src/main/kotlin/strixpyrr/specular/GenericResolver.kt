// Copyright 2020 Strixpyrr
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package strixpyrr.specular

import uy.klutter.core.common.whenNotNull
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure

// Todo: Consider using a cache to speed up generic resolving.
// An improvement to the "drill down" procedure below may be achieved via caching.
// Rather than caching every type resolved, we could store the highest subclass
// where the specific type parameter is passed to its supertype. In this example:
// IntList -> ArrayList<Int> -> MutableList<Int> -> List<Int> -> Collection<Int>
// We could store the fact that ArrayList passes its type parameter to the first
// type parameter of MutableList. Known type hierarchies would be faster to look up
// the second time.

object GenericResolver
{
	fun KType.resolveCollectionTypeParameter() =
		resolveRootTypeParameter(
			Collection::class,
			parameterIndex = 0
		)
	
	fun KType.resolveMapKeyTypeParameter() =
		resolveRootTypeParameter(
			Map::class,
			parameterIndex = 0
		)
	
	fun KType.resolveMapValueTypeParameter() =
		resolveRootTypeParameter(
			Map::class,
			parameterIndex = 1
		)
	
	/**
	 * @throws IllegalArgumentException [R] is not a parent of the specified type;
	 * or [parameterIndex] isn't in the range [0,n), where n is [R] type parameter
	 * count.
	 */
	inline fun <reified R : Any> KType.resolveRootTypeParameter(parameterIndex: Int) =
		resolveRootTypeParameter(R::class, parameterIndex)
	
	/**
	 * @throws IllegalArgumentException The [root] is not a parent of the specified
	 * type; or [parameterIndex] isn't in the range [0,n), where n is the [root]'s
	 * type parameter count.
	 */
	fun KType.resolveRootTypeParameter(root: KClass<*>, parameterIndex: Int): KTypeProjection
	{
		require(parameterIndex > -1) { "The parameter index cannot be negative." }
		
		val rootType = drillDown(root)
		
		requireNotNull(rootType)
		{
			"The root $root is not a parent of the specified type $this."
		}
		
		val parameters = rootType.arguments
		
		require(parameterIndex < parameters.size)
		{
			"The parameter index must be within the root's type parameter count."
		}
		
		return parameters[parameterIndex]
	}
	
	private fun KType.drillDown(root: KClass<*>): KType?
	{
		// While I've tested the "drill down" method used here for finding generic
		// types of, for example, collection types where it worked great, I cannot
		// attest to its speed. It's likely abysmal. It was the easiest to come up
		// with at the time, and the speed should be good enough for now.
		// In theory I could've returned the type parameter at the given index. In
		// practise this is flaky at best. What if the type parameter exists at an
		// index but it isn't what's passed to the super type? What if the type is
		// concrete, like "StringMap : Map<String, String>"? Too many common cases
		// where it would fail or give an unexpected result.
		
		val current = jvmErasure
		
		if (current == root) return this
		
		for (parent in current.supertypes)
		{
			parent.drillDown(root) whenNotNull { return it }
		}
		
		return null
	}
	
	@Suppress("NOTHING_TO_INLINE")
	private inline fun parameterIndexOutOfRange(type: KClass<*>, parameterIndex: Int, cause: Throwable): Throwable
	{
		val count = type.typeParameters.size
		
		return IllegalArgumentException(
			"The parameter index $parameterIndex is out of the valid range: it " +
			"must be greater than 0 and within the type parameter count $count.",
			cause
		)
	}
}