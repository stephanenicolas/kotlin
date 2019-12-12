// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION
import kotlin.reflect.KFunction0

class A {
    class Nested
    
    fun main() {
        val x = ::Nested
        val y = A::Nested

        checkSubtype<KFunction0<Nested>>(x)
        <!INAPPLICABLE_CANDIDATE!>checkSubtype<!><KFunction0<Nested>>(y)
    }
    
    companion object {
        fun main() {
            ::Nested
            val y = A::Nested

            <!INAPPLICABLE_CANDIDATE!>checkSubtype<!><KFunction0<A.Nested>>(y)
        }
    }
}

class B {
    fun main() {
        ::Nested
        val y = A::Nested

        <!INAPPLICABLE_CANDIDATE!>checkSubtype<!><KFunction0<A.Nested>>(y)
    }
}