enum class E {
    E1 {
        override fun foo() = <!UNRESOLVED_REFERENCE!>outerFun<!>() + super.<!UNRESOLVED_REFERENCE!>outerFun<!>()
    },
    E2 {
        override fun foo() = E1.foo()
    };
    
    abstract fun foo(): Int
    
    fun outerFun() = 42
}
