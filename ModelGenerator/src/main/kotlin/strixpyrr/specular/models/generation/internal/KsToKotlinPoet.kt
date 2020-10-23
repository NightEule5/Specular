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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName

internal val KSClassDeclaration.typeName: TypeName get() =
	className.run()
	{
		if (typeParameters.isEmpty())
			this
		else parameterizedBy(
			*Array(typeParameters.size)
			{
				WildcardTypeName.STAR
			}
		)
	}

internal val KSType.typeName: TypeName get() =
	declaration.className.run()
	{
		if (arguments.isEmpty())
			this
		else parameterizedBy(
			*Array(arguments.size)
			{ i ->
				val argument = arguments[i]
				
				val name = argument.type?.resolve()?.typeName
				
				when (argument.variance)
				{
					Variance.STAR          -> WildcardTypeName.STAR
					Variance.INVARIANT     -> name!!
					Variance.COVARIANT     -> WildcardTypeName.subtypeOf(name!!)
					Variance.CONTRAVARIANT -> WildcardTypeName.supertypeOf(name!!)
				}
			}
		)
	}.handleNullability(nullability)

// Todo: What do we do with platform nullability?
@Suppress("NOTHING_TO_INLINE")
private inline fun TypeName.handleNullability(nullability: Nullability) =
	if (nullability == Nullability.NULLABLE && !nullable) asNullable() else this

internal val KSDeclaration.className: ClassName get()
{
	val firstName: String
	val otherNames: Array<String>
	
	simpleName.asString().split('.').run()
	{
		firstName = this[0]
		otherNames = subList(1, size).toTypedArray()
	}
	
	return ClassName(packageName.asString(), firstName, *otherNames)
}