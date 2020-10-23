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

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <reified E : Throwable> catch(catchBlock: (E) -> Unit, tryBlock: () -> Unit)
{
	contract {
		callsInPlace(catchBlock, InvocationKind.AT_MOST_ONCE)
		callsInPlace(tryBlock, InvocationKind.EXACTLY_ONCE)
	}
	
	try
	{
		tryBlock()
	}
	catch (e: Throwable)
	{
		if (e is E)
			catchBlock(e)
		else throw e
	}
}