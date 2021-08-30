interface Foo

fun <T> Foo.invoke(t: T) {}

fun test(f: Foo) {
    <expr>f("")</expr>
}
