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
package strixpyrr.specular.models.factory.internal

import java.lang.reflect.Field
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass

internal val KClass<out Any>.isEnum get() = java.isEnum;

@Suppress("UNCHECKED_CAST")
internal val KClass<out Any>.enumFields get() = (this as KClass<out Enum<*>>).fields;

// Todo: There seems to be no way to do this without using direct Java reflection.
//  I spent at least 20 minutes trying to figure out how to do this with Kotlin's
//  reflection library and only came up with one StackOverflow post. The answerer
//  said he didn't think it was possible, but more research needs to be done. I'm
//  frustrated with it though, so I'll just try using java. The post in question:
//  https://stackoverflow.com/q/61595208/10258324 (for future reference).
internal val KClass<out Enum<*>>.fields: List<KEnumConstant> get()
{
	val fields = java.fields;
	val list = ArrayList<KEnumConstant>(fields.size);
	
	for (field in fields)
		if (field.isEnumConstant)
			list.add(KEnumConstant(field));
	
	return list;
}

internal inline class KEnumConstant(val field: Field) : KAnnotatedElement
{
	override val annotations get() = field.annotations.toMutableList();
	
	val value get() = field.get(null);
}