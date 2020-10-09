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

import org.apache.commons.collections4.map.LinkedMap
import strixpyrr.specular.models.IModel
import strixpyrr.specular.models.caching.IModelCache.INode
import strixpyrr.specular.models.internal.all
import strixpyrr.specular.models.internal.sortDescendingByValue

/**
 * @since 0.5
 */
open class UsageAwareModelCache<K : Any> protected constructor(
	override val storage: MutableMap<K, IUsageAwareNode>,
	protected val removalThreshold: Int = -1,
	
) : ModelCache<K, UsageAwareModelCache.IUsageAwareNode>()
{
	constructor(capacity: Int = 8, threshold: Int = -1, optimizationHitThreshold: Int = -1) :
		this(LinkedHashMap<K, IUsageAwareNode>(capacity), threshold)
	{
		hitThreshold = optimizationHitThreshold;
	}
	
	override fun create(factory: () -> IModel<*, *, *, *>) = UsageAwareNode(this, factory);
	
	override fun create(model: IModel<*, *, *, *>) = UsageAwareValueNode(this, model);
	
	override fun isNode(node: INode) = node is IUsageAwareNode;
	
	/**
	 * Optimizes the underlying storage map. The storage map is pruned and sorted.
	 *
	 * During pruning, models whose usage remained under the set threshold will be
	 * culled. When a model is culled, its usage is reset and its value is set to
	 * null. A reference to a culled model's factory remains in storage until the
	 * model is requested again, at which point it is reconstructed. If, however,
	 * a model was cached as a value (ie `cache(K, IModel)`), it will be removed.
	 *
	 * During sorting, models are sorted from greatest to least usage. This speeds
	 * up access to models that are likely to be used again.
	 */
	override fun optimize()
	{
		pruneStorage(removalThreshold);
		
		sortStorage();
	}
	
	protected open fun pruneStorage(threshold: Int)
	{
		if (threshold < 0) return;
		
		// Remove nodes from storage if their model usage is below the threshold.
		// If a node doesn't have a fixed value, its usage will be reset and its
		// model set to null.
		for ((key, node) in storage)
			if (node.usageCount < threshold)
				if (!node.cull()) storage.remove(key);
	}
	
	protected open fun sortStorage()
	{
		// There's no point in sorting a map without insertion order.
		if (storage !is LinkedHashMap || storage !is LinkedMap) return;
		
		// Skip if there aren't enough nodes to sort.
		if (storage.size <= 1) return;
		
		// Skip if the map is already sorted.
		if (storage.values.all { prev: IUsageAwareNode, curr ->
				curr.usageCount >= prev.usageCount
			}) return;
		
		storage.sortDescendingByValue();
	}
	
	interface IUsageAwareNode : INode, Comparable<IUsageAwareNode>
	{
		/**
		 * Returns the number of times the model was accessed from this instance.
		 */
		val usageCount: Int;
		
		fun cull(): Boolean;
		
		override fun compareTo(other: IUsageAwareNode)
			= usageCount.compareTo(other.usageCount);
	}
	
	protected open class UsageAwareNode(
		parent: UsageAwareModelCache<*>,
		factory: () -> IModel<*, *, *, *>
	) : IUsageAwareNode, Node(parent, factory)
	{
		final override var usageCount: Int = 0; private set;
		
		override val model get() = super.model.also { usageCount++ };
		
		override fun cull(): Boolean
		{
			value = null;
			usageCount = 0;
			
			return true;
		}
	}
	
	protected open class UsageAwareValueNode(
		parent: UsageAwareModelCache<*>,
		model: IModel<*, *, *, *>
	) : IUsageAwareNode, ValueNode(parent, model)
	{
		final override var usageCount: Int = 0; private set;
		
		override val model get() = super.model.apply { usageCount++ };
		
		override fun cull(): Boolean
		{
			usageCount = 0;
			
			return false;
		}
	}
}