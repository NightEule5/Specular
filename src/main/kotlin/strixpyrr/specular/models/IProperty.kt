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

import kotlin.reflect.KProperty
import kotlin.reflect.KType

/**
 * @since 0.5
 */
interface IProperty<T, V, L> : IAttributeProvider<L>
{
	override val attributeContainer get() = getEmptyAttributeContainer<L>();
	
	val name: String;
	val type: KType;
	
	fun hasValue(target: T): Boolean;
	fun getValue(target: T): V;
	fun setValue(target: T, value: V);
	
	// Allows this to be used as a delegate.
	
	operator fun getValue(thisRef: T, property: KProperty<*>): V = getValue(thisRef);
	operator fun setValue(thisRef: T, property: KProperty<*>, value: V) = setValue(thisRef, value);
}