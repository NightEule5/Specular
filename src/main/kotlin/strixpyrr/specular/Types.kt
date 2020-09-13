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
package strixpyrr.specular

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.typeOf

inline fun <reified B : Any> KClass<*>.isSubclassOf() = isSubclassOf(B::class);
inline fun <reified D : Any> KClass<*>.isSuperclassOf() = isSuperclassOf(D::class);

inline fun <reified B : Any> KClass<*>.isDerivedFrom() = isSubclassOf<B>();
inline fun <reified D : Any> KClass<*>.isBaseOf() = isSuperclassOf<D>();

@ExperimentalStdlibApi
inline fun <reified B> KType.isSubtypeOf() = isSubtypeOf(typeOf<B>());
@ExperimentalStdlibApi
inline fun <reified D> KType.isSupertypeOf() = isSupertypeOf(typeOf<D>());