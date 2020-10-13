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
package strixpyrr.specular.models.factory

import strixpyrr.specular.models.FactoryParameter
import strixpyrr.specular.models.IFactoryParameter
import strixpyrr.specular.models.IProperty
import strixpyrr.specular.models.annotations.ExcludedFactory
import strixpyrr.specular.models.annotations.Factory
import strixpyrr.specular.models.annotations.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

// Todo: This needs a more appropriate name.
open class FactoryResolver
{
	open fun <T> isIncluded(factory: KFunction<T>) =
		!factory.hasAnnotation<ExcludedFactory>()
	
	@Suppress("UNCHECKED_CAST")
	open fun <T : Any> findFunctions(type: KClass<T>): Collection<KFunction<T>> =
		type.staticFunctions.run()
		{
			// The stupid filter function doesn't use the collection size for the
			// list's capacity, instead using the default. We'll have to do that
			// manually. >:|
			filterTo(ArrayList(size))
			{
				it.extensionReceiverParameter == null &&
				it.returnType.jvmErasure.isSubclassOf(type) &&
				it.hasAnnotation<Factory>()
			} as List<KFunction<T>>
		}
	
	open fun createParameter(parameter: KParameter, properties: Iterable<IProperty<*, *, *>>): IFactoryParameter
	{
		if (parameter.kind == KParameter.Kind.EXTENSION_RECEIVER)
			throw IllegalArgumentException(
				"The specified parameter is an extension receiver: $parameter. " +
				"Extensions are not valid as factory functions."
			)
		
		val name = parameter.findAnnotation<Parameter>()?.propertyName ?:
		           parameter.name as String
		
		val property = properties.find { it.name == name }
		return FactoryParameter(name, parameter, property)
	}
}