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

import strixpyrr.abstrakt.collections.all
import strixpyrr.specular.models.internal.require
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * Exposes target creation behavior.
 * @since 0.6
 */
interface ITargetFactory<T>
{
	/**
	 * Whether a new target can be created.
	 */
	val canCreate get() = factoryVariants.isEmpty();
	
	val factoryVariants: Collection<IFactoryVariant<T>> get() = emptyList();
	
	/**
	 * @throws UnsupportedOperationException When [canCreate] returns `false`.
	 * @throws FactoryNotFoundException When no parameterless factory is found.
	 */
	fun create(): T
	{
		require(
			condition = canCreate,
			::UnsupportedOperationException,
			message = targetCreationNotSupported
		)
		
		return factoryVariants.find(IFactoryVariant<T>::isParameterless)?.create() ?:
		       throw FactoryNotFoundException(
			       "No parameterless factory was found."
		       )
	}
	
	fun create(vararg parameters: Any?): T
	{
		require(
			condition = canCreate,
			::UnsupportedOperationException,
			message = targetCreationNotSupported
		)
		
		val factory = factoryVariants.find()
		{
			if (it.parameters.size < parameters.size) return@find false
			
			val iterator = it.parameters.iterator()
			
			for (i in parameters.indices)
			{
				val type = iterator.next().type
				
				val requested = parameters[i]
				
				if (requested == null)
				{
					if (!type.isMarkedNullable)
						return@find false
				}
				else if (!type.jvmErasure.isSuperclassOf(requested::class))
						return@find false
			}
			
			iterator.all(IFactoryParameter::isOptional)
		}
		
		if (factory != null)
			return factory.create(parameters)
		
		throw FactoryNotFoundException(
			"No factory was found that's able to take the specified parameters:" +
			" $parameters."
		)
	}
}

private const val targetCreationNotSupported = "Target creation is not supported in this model."

internal val IFactoryVariant<*>.isParameterless get() =
	parameters.all(IFactoryParameter::isOptional);

open class FactoryNotFoundException : NoSuchElementException
{
	constructor() : super()
	constructor(message: String) : super(message)
}

/**
 * @since 0.6
 */
interface IFactoryVariant<T>
{
	val isPrimary: Boolean;
	val parameterCount get() = parameters.size;
	
	val parameters: Collection<IFactoryParameter>;
	
	fun create(vararg parameters: Any?): T
	fun create(parameters: Map<KParameter, Any?>): T
}

/**
 * @since 0.6
 */
interface IFactoryParameter
{
	val name: String;
	val index: Int;
	val type: KType;
	val isOptional: Boolean;
	
	val parameter: KParameter;
	val linkedProperty: IProperty<*, *, *>?
}

/**
 * @since 0.6
 */
@Suppress("LeakingThis")
open class FactoryParameter(
	override val name: String,
	override val parameter: KParameter,
	override val linkedProperty: IProperty<*, *, *>? = null
) : IFactoryParameter
{
	constructor(parameter: KParameter) : this(
		parameter.name ?: throw IllegalArgumentException("The parameter must have a name."),
		parameter
	)
	
	constructor(parameter: KParameter, linkedProperty: IProperty<*, *, *>) : this(
		parameter.name ?: throw IllegalArgumentException("The parameter must have a name."),
		parameter,
		linkedProperty
	)
	
	override val index      by parameter::index
	override val type       by parameter::type
	override val isOptional by parameter::isOptional
	
	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (other !is FactoryParameter) return false
		
		if (name != other.name) return false
		if (parameter != other.parameter) return false
		
		return true
	}
	
	override fun hashCode(): Int
	{
		var result = name.hashCode()
		result = 31 * result + parameter.hashCode()
		return result
	}
}
