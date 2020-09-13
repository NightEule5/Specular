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
package strixpyrr.specular.models.building

import strixpyrr.specular.models.DelegatedProperty
import strixpyrr.specular.models.IAttributeContainer
import strixpyrr.specular.models.IDelegatedProperty
import uy.klutter.core.common.with
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * @since 0.5
 */
open class DelegatedPropertyBuilder<T, V, L> : IPropertyBuilder<T, V, L>, AttributeProviderBuilder<L>()
{
	lateinit var getter: T.() -> V;
	lateinit var setter: T.(V) -> Unit;
	lateinit var isInitialized: T.() -> Boolean;
	lateinit var valueType: KType;
	
	@PublishedApi
	internal val hasType by ::valueType::isInitialized;
	
	open fun setFromProperty(property: KMutableProperty1<T, V>)
		= setAccessors(property::get, property::set).apply {
			if (!property.isLateinit && !::isInitialized.isInitialized)
				setAsAlwaysInitialized()
		}
	
	open fun setAccessors(get: T.() -> V, set: T.(V) -> Unit)
		= with {
			getter = get;
			setter = set;
		}
	
	open fun setInitialization(initialization: T.() -> Boolean)
		= with {
			isInitialized = initialization;
		}
	
	open fun setAsAlwaysInitialized() = setInitialization { true };
	
	override fun setAttributeContainer(builder: IAttributeContainerBuilder<L>)
		= with { super.setAttributeContainer(builder) };
	
	override fun setAttributeContainer(container: IAttributeContainer<L>)
		= with { super.setAttributeContainer(container) };
	
	@ExperimentalStdlibApi
	inline fun <reified V> setValueType()
		= with { valueType = typeOf<V>(); }
	
	override fun build(): IDelegatedProperty<T, V, L>
		= DelegatedProperty(
			getter,
			setter,
			isInitialized,
			valueType,
			attributeContainer
		);
}