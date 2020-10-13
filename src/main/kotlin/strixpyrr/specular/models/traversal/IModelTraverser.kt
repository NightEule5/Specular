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
package strixpyrr.specular.models.traversal

import strixpyrr.specular.models.IAttributeProvider
import strixpyrr.specular.models.IModel
import strixpyrr.specular.models.IProperty

@Suppress("SpellCheckingInspection")
interface IModelTraverser<T, K, L, Lp, C>
{
	fun traverse(context: C, target: T, model: IModel<in T, K, L, Lp>)
	{
		// Model
		
		visit(context, target, model)
		
		// Model Attributes
		
		model.visitAttributes()
		{ label, attribute ->
			visit(context, target, model, label, attribute)
		}
		
		// Property
		
		model.visitProperties()
		{ key, property ->
			visit(context, target, model, key, property)
			
			// Attributes
			
			property.visitAttributes()
			{ label, attribute ->
				visit(context, target, model, key, property, label, attribute)
			}
		}
	}
	
	fun visit(
		context: C,
		target: T,
		model: IModel<in T, K, L, Lp>
	)
	
	fun visit(
		context: C,
		target: T,
		model: IModel<in T, K, L, Lp>,
		label: L,
		attribute: Any?
	)
	
	fun <V> visit(
		context: C,
		target: T,
		model: IModel<in T, K, L, Lp>,
		key: K,
		property: IProperty<in T, V, Lp>
	)
	fun <V> visit(
		context: C,
		target: T,
		model: IModel<in T, K, L, Lp>,
		key: K,
		property: IProperty<in T, V, Lp>,
		label: Lp,
		attribute: Any?
	)
}

private inline fun <L> IAttributeProvider<L>.visitAttributes(visit: (L, Any?) -> Unit) =
	attributeContainer.run { labels.forEach { visit(it, getAttribute(it)) } }

private inline fun <T, K, Lp> IModel<in T, K, *, Lp>.visitProperties(visit: (K, IProperty<in T, Any?, Lp>) -> Unit) =
	keys.forEach { visit(it, getProperty(it)) }