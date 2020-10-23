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
package strixpyrr.specular.models.generation.internal

import com.google.devtools.ksp.symbol.*
import strixpyrr.specular.models.generation.api.*
import kotlin.reflect.KClass

internal class ModelData(
	override val declaration: KSClassDeclaration
) : IModelData
{
	override lateinit var name: String
	
	override val attributes = ModelAttributeData()
	override val properties = PropertyDataContainer()
	override val factories = ArrayList<FactoryData>()
	
	internal lateinit var generator: KClass<out IModelGenerator>
}

internal class ModelAttributeData : IModelAttributeData
{
	override lateinit var labelType: KClass<*>
	override val attributes: MutableMap<Any?, Any?> = LinkedHashMap()
}

internal class PropertyDataContainer : IPropertyDataContainer
{
	override lateinit var labelType: KClass<*>
	override lateinit var keyType: KClass<*>
	override val properties = LinkedHashMap<Any?, PropertyData>()
}

internal class PropertyData(
	override val declaration: KSPropertyDeclaration,
	override val name: String,
	override val type: KSType,
	override val immutable: Boolean
) : IPropertyData
{
	override val attributes: MutableMap<Any?, Any?> = LinkedHashMap()
	override var isAlwaysInitialized = false
	override var initializationProperty: KSPropertyDeclaration? = null
}

internal class FactoryData(
	override val declaration: KSFunctionDeclaration,
	override val index: Int,
	override val isPrimary: Boolean,
	override val isConstructor: Boolean,
	override val parameters: List<FactoryParameterData>
) : IFactoryData

internal class FactoryParameterData(
	override val declaration: KSValueParameter,
	override val linkedProperty: KSPropertyDeclaration? = null
) : IFactoryParameterData