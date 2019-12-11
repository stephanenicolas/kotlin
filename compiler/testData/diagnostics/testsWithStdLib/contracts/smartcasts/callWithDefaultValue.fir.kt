// !LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect
// !USE_EXPERIMENTAL: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun myAssert(condition: Boolean, message: String = "") {
    contract {
        returns() implies (condition)
    }
    if (!condition) throw kotlin.<!UNRESOLVED_REFERENCE!>IllegalArgumentException<!>(message)
}

fun test(x: Any?) {
    myAssert(x is String)
    x.<!UNRESOLVED_REFERENCE!>length<!>
}