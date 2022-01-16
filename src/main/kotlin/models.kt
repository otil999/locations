package hu.otilsoft.locations


data class Category (
    val id: Int = 0,
    val name: String = ""
) {
    override fun toString(): String = name
}


data class Location(
    val id: Int,
    val name: String,
    val description: String,
    val visited: Boolean,
    val type: String,
    val modId: Int
) {
    override fun toString(): String = name
}
