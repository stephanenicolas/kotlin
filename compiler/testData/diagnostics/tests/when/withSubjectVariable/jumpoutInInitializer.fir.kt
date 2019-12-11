// !LANGUAGE: +VariableDeclarationInWhenSubject
// !DIAGNOSTICS: -UNUSED_VARIABLE


fun testJumpOutInElvis(x: Int?) {
    loop@ while (true) {
        val y = when (val z = x ?: break@loop) {
            0 -> "0"
            else -> "not 0"
        }

        x.inc()
    }

    x.<!INAPPLICABLE_CANDIDATE!>inc<!>()
}

fun testJumpOutInElvisLikeIf(x: Int?) {
    loop@ while (true) {
        val y = when (val z = if (x == null) break@loop else x) {
            0 -> "0"
            else -> "not 0"
        }
        x.inc()
    }

    x.<!INAPPLICABLE_CANDIDATE!>inc<!>()
}


fun getBoolean() = true

fun testJumpOutInIf(x: Int?) {
    loop@ while (true) {
        val y = when (val z = if (getBoolean()) { x!!; break@loop } else x) {
            0 -> "0"
            else -> "not 0"
        }
        x.<!INAPPLICABLE_CANDIDATE!>inc<!>()
    }

    x.inc() // Actually, safe, but it's OK if it's error
}