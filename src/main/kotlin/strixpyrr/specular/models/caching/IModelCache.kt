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
import strixpyrr.specular.models.IModelProvider

/**
 * @since 0.5
 */
interface IModelCache<K>
{
	val map: Map<K, INode>;
	
	operator fun contains(key: K) = key in map;
	operator fun contains(node: INode) = map.containsValue(node);
	
	operator fun <T, Km, L, Lp> get(key: K): IModel<T, Km, L, Lp>;
	fun <T, Km, L, Lp> get(key: K, provider: IModelProvider<T, Km, L, Lp>) = get(key, provider::model);
	fun <M : IModel<*, *, *, *>> get(key: K, default: M): M;
	fun <M : IModel<*, *, *, *>> get(key: K, factory: () -> M): M;
	
	fun cache(key: K, provider: IModelProvider<*, *, *, *>) = cache(key, provider::model);
	fun cache(key: K, factory: () -> IModel<*, *, *, *>): Boolean; // Whether that key didn't exist.
	fun cache(key: K, model: IModel<*, *, *, *>): Boolean;
	
	fun cacheAll(map: Map<K, INode>);
	
	/**
	 * Optimizes the underlying data structure; the details of optimization depend
	 * on the implementation.
	 */
	fun optimize();
	// fun prune(count: Int): Int;
	
	fun copyTo(cache: IModelCache<K>) = cache.cacheAll(map);
	
	interface INode
	{
		val model: IModel<*, *, *, *>;
	}
}