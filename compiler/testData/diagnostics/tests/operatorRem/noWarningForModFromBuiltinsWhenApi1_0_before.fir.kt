// !LANGUAGE: -ProhibitOperatorMod
// !WITH_NEW_INFERENCE
// !API_VERSION: 1.0
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -EXTENSION_SHADOWED_BY_MEMBER

class Foo {
    operator fun rem(x: Int): Foo = Foo()
}

class Bar {
    operator fun remAssign(x: Int) {}
}

class Baz {
    companion object {
        operator fun rem(x: Int) {}
        operator fun Int.rem(x: Int) {}
    }
}

operator fun Baz.rem(x: Int) {}

fun local() {
    operator fun Int.rem(x: Int) {}
    operator fun String.remAssign(x: Int) {}
}

class WithMod {
    operator fun mod(other: WithMod) = this

    fun test() {
        val a = this <!INAPPLICABLE_CANDIDATE!>%<!> this
        var b = this.mod(this)
        <!UNRESOLVED_REFERENCE!>b %= this<!>
    }
}

fun noOverflow() {
    (-1).mod(5)
}

fun builtIns(b: Byte, s: Short) {
    var a = 1 % 2
    a %= 3
    1.mod(2)
    b % s
    1.0 % 2.0
}