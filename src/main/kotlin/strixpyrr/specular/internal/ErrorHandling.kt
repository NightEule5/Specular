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
package strixpyrr.specular.internal

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <reified E : Exception> catch(factory: (E) -> Throwable, tryBlock: () -> Unit)
{
	contract {
		callsInPlace(factory, InvocationKind.AT_MOST_ONCE)
		callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
	}
	
	return catch<E, Unit>(factory, tryBlock)
}

@JvmName("catchRethrow")
internal inline fun <reified E : Exception, R> catch(factory: (E) -> Throwable, tryBlock: () -> R): R
{
	contract {
		callsInPlace(factory, InvocationKind.AT_MOST_ONCE)
		callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
	}
	
	return catch(factory, { throw it }, tryBlock)
}

@JvmName("catchThrowHandle")
internal inline fun <reified E : Exception, R> catch(factory: (E) -> Throwable, catchBlock: (Throwable) -> R, tryBlock: () -> R): R
{
	contract {
		callsInPlace(factory, InvocationKind.AT_MOST_ONCE)
		callsInPlace(catchBlock, InvocationKind.AT_MOST_ONCE)
		callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
	}
	
	return catchReturn(
		{
			if (it is E)
				throw factory(it)
			
			return catchBlock(it)
		},
		tryBlock
	)
}

internal inline fun <R> catchReturn(catchBlock: (Throwable) -> R, tryBlock: () -> R): R
{
	contract {
		callsInPlace(catchBlock, InvocationKind.AT_MOST_ONCE)
		callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
	}
	
	return try
	{
		tryBlock()
	}
	catch (e: Throwable)
	{
		catchBlock(e)
	}
}