package a

import a.A.*
import a.A.C
import a.A.C.*
import a.A.D.*
import a.A.C.*
import a.A.C.G
import a.A.E.J.*
import a.A.CO.*
import a.A.CO

import a.B.C.*
import a.B.C.A.*
import a.B.C.D.*

import a.E.*
import a.E.E1
import a.E.E2.*

class A {
    object C {
        object G
    }
    object D {

    }

    class E {
        object J
    }

    companion object CO {
        object H
    }
}

enum class E {
    E1, E2
}

object B {
    class C {
        object A
        object D
    }
}