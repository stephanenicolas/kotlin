// !DIAGNOSTICS: -UNUSED_PARAMETER -REDUNDANT_PROJECTION

class In<in T>() {
    fun f(t : T) {}
    fun f(t : Int) = t
}

fun test1(x: In<String>): Unit = x.f("1")
fun test2(x: In<in String>): Unit = x.f("1")
fun test3(x: In<out String>): Unit = x.f("1")
fun test4(x: In<*>): Unit = x.f("1")