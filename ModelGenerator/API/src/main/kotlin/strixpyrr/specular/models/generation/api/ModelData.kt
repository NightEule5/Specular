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
package strixpyrr.specular.models.generation.api

import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass

interface IModelData
{
	val name: String
	val declaration: KSClassDeclaration
	val attributes: IModelAttributeData
	val properties: IPropertyDataContainer
	val factories: List<IFactoryData>
}

interface IModelAttributeData
{
	val labelType: KClass<*>
	val attributes: Map<Any?, Any?>
}

interface IPropertyDataContainer
{
	val labelType: KClass<*>
	val keyType: KClass<*>
	val properties: Map<Any?, IPropertyData>
}

interface IPropertyData
{
	val declaration: KSPropertyDeclaration
	val attributes: Map<Any?, Any?>
	val name: String
	val type: KSType
	val immutable: Boolean
	val isAlwaysInitialized: Boolean
	val initializationProperty: KSPropertyDeclaration?
}

interface IFactoryData
{
	val declaration: KSFunctionDeclaration
	
	val index: Int
	val isPrimary: Boolean
	val isConstructor: Boolean
	val parameters: List<IFactoryParameterData>
}

interface IFactoryParameterData
{
	val declaration: KSValueParameter
	val linkedProperty: KSPropertyDeclaration?
}