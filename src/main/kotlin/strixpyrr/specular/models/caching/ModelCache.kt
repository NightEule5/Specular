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
package strixpyrr.specular.models.caching

import strixpyrr.specular.models.IModel
import strixpyrr.specular.models.caching.IModelCache.INode
import uy.klutter.core.collections.asReadOnly

/**
 * @since 0.5
 */
abstract class ModelCache<K : Any, N : INode> : IModelCache<K>
{
	@Suppress("LeakingThis")
	override val map = storage.asReadOnly();
	
	protected abstract val storage: MutableMap<K, N>;
	
	@Suppress("UNCHECKED_CAST")
	override fun <T, Km, L, Lp> get(key: K): IModel<T, Km, L, Lp>
	{
		if (key in storage)
			return (storage[key] as INode).model as IModel<T, Km, L, Lp>;
		
		throw NoSuchElementException(
			"No model with a key of $key was found in the cache."
		)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun <M : IModel<*, *, *, *>> get(key: K, factory: () -> M)
		= storage.getOrPut(key) { create(factory) } as M;
	
	@Suppress("UNCHECKED_CAST")
	override fun <M : IModel<*, *, *, *>> get(key: K, default: M)
		= storage.getOrPut(key) { create(default) } as M;
	
	override fun cache(key: K, factory: () -> IModel<*, *, *, *>): Boolean
	{
		if (key in storage) return true;
		
		storage[key] = create(factory);
		return false;
	}
	
	override fun cache(key: K, model: IModel<*, *, *, *>): Boolean
	{
		if (key in storage) return true;
		
		storage[key] = create(model);
		return false;
	}
	
	override fun cacheAll(map: Map<K, INode>)
	{
		for ((key, node) in map)
		{
			if (isNode(node))
			{
				when (node)
				{
					is Node -> cache(key, node.factory)
					else    -> cache(key, node.model)
				}
			}
		}
	}
	
	protected abstract fun create(factory: () -> IModel<*, *, *, *>): N;
	protected abstract fun create(model: IModel<*, *, *, *>): N;
	protected abstract fun isNode(node: INode): Boolean;
	
	protected open class Node(
		val factory: () -> IModel<*, *, *, *>
	): INode
	{
		protected var value: IModel<*, *, *, *>? = null;
		
		// This is annoying. In C# I could've done "value ??= factory()".
		// I did some reading, and apparently this case was one reason "also" was
		// added: https://discuss.kotlinlang.org/t/nullable-assigner/3931/6.
		// I just happened to write their example before reading that... weird.
		override val model get() = value ?: factory().also { value = it };
		
		override fun equals(other: Any?): Boolean
		{
			if (this === other) return true
			if (other !is Node) return false
			
			if (factory != other.factory) return false
			if (value != other.value) return false
			
			return true
		}
		
		override fun hashCode(): Int
		{
			var result = factory.hashCode()
			result = 31 * result + (value?.hashCode() ?: 0)
			return result
		}
	}
	
	protected open class ValueNode(
		override val model: IModel<*, *, *, *>
	): INode
	{
		override fun equals(other: Any?): Boolean
		{
			if (this === other) return true
			if (other !is ValueNode) return false
			
			if (model != other.model) return false
			
			return true
		}
		
		override fun hashCode() = model.hashCode();
	}
}