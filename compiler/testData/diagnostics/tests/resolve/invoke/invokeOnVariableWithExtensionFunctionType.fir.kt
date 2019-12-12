// !WITH_NEW_INFERENCE
// FILE: 1.kt
package fooIsExtension

class A
class B

val A.foo: B.() -> Unit get() = {}

fun test(a: A, b: B) {
    b.<!UNRESOLVED_REFERENCE!>(a.foo)()<!>
    (a.foo)(b)
    a.foo(b)

    with(a) {
        b.<!UNRESOLVED_REFERENCE!>foo<!>()

        b.<!UNRESOLVED_REFERENCE!>(foo)()<!>

        <!UNRESOLVED_REFERENCE!>(b.<!INAPPLICABLE_CANDIDATE!>foo<!>)()<!>

        foo(b)
        (foo)(b)
    }

    with(b) {
        a.<!INAPPLICABLE_CANDIDATE!>foo<!>()
        a.<!UNRESOLVED_REFERENCE!>(foo)()<!>

        <!INAPPLICABLE_CANDIDATE!>(a.foo)()<!>

        (a.foo)(this)
        a.foo(this)
    }

    with(a) {
        with(b) {
            <!INAPPLICABLE_CANDIDATE!>foo<!>()
            <!INAPPLICABLE_CANDIDATE!>(foo)()<!>
        }
    }
}

// FILE: 1.kt
package fooIsMember

class A {
    val foo: B.() -> Unit get() = {}
}
class B

fun test(a: A, b: B) {
    b.<!UNRESOLVED_REFERENCE!>(a.foo)()<!>
    (a.foo)(b)
    a.foo(b)

    with(a) {
        b.<!UNRESOLVED_REFERENCE!>foo<!>()

        b.<!UNRESOLVED_REFERENCE!>(foo)()<!>

        <!UNRESOLVED_REFERENCE!>(b.<!UNRESOLVED_REFERENCE!>foo<!>)()<!>

        foo(b)
        (foo)(b)
    }

    with(b) {
        a.<!INAPPLICABLE_CANDIDATE!>foo<!>()
        a.<!UNRESOLVED_REFERENCE!>(foo)()<!>

        <!INAPPLICABLE_CANDIDATE!>(a.foo)()<!>

        (a.foo)(this)
        a.foo(this)
    }

    with(a) {
        with(b) {
            <!INAPPLICABLE_CANDIDATE!>foo<!>()
            <!INAPPLICABLE_CANDIDATE!>(foo)()<!>
        }
    }
}
