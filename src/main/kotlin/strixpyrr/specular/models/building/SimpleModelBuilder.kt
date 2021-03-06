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

import strixpyrr.specular.models.SimpleModel
import strixpyrr.specular.models.SimpleProperty
import uy.klutter.core.common.with
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @since 0.5
 */
class SimpleModelBuilder<T : Any> : NamedDelegatedModelBuilder<T, String, String>()
{
	inline fun <V> addProperty(
		key: String,
		block: SimplePropertyBuilder<T, V>.() -> Unit
	): SimpleModelBuilder<T>
	{
		contract {
			callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		}
		
		return addProperty(key, buildProperty(block));
	}
	
	fun <V> addProperty(key: String, propertyBuilder: SimplePropertyBuilder<T, V>)
		= addProperty(key, propertyBuilder.build());
	
	fun <V> addProperty(key: String, property: SimpleProperty<T, V>)
		= with { super.addProperty(key, property) };
	
	override fun build() = SimpleModel(finalizeStorage(), attributeContainer);
}