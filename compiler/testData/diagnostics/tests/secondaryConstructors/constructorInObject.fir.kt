object A {
    constructor()
    init {}
}

enum class B {
    X() {
        constructor()
    }
}

class C {
    companion object {
        constructor()
    }
}

val anonObject = object {
    <!CONSTRUCTOR_IN_OBJECT!>constructor()<!>
}