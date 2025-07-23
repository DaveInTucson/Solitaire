package s7n.scratch


fun main() {
   var values = ArrayList<Int>()
    (1..9).forEach{ values.add(it) }

    println("Values=$values")
    val v = values.removeAt(6)
    println("values=$values v=$v")
    values.add(6, v)
    println("values=$values")
}