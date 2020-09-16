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
package strixpyrr.specular.models

/**
 * @since 0.5
 */
interface IModel<T, K, L, Lp> : IAttributeProvider<L>
{
	val keys: Collection<K>;
	val properties: Collection<IProperty<T, *, Lp>>;
	
	fun hasProperty(key: K): Boolean;
	fun <V> getProperty(key: K): IProperty<T, V, Lp>;
	
	operator fun contains(key: K) = hasProperty(key);
	
	operator fun <V> get(target: T, key: K)
		= getProperty<V>(key).getValue(target);
	operator fun <V> set(target: T, key: K, value: V)
		= getProperty<V>(key).setValue(target, value);
}