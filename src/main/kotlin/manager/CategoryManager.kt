package hu.otilsoft.locations.manager

import hu.otilsoft.locations.Category
import java.sql.Connection


class CategoryManager {
    private val sqlInsert = "INSERT INTO category (name) VALUES (?)"
    private val sqlUpdate = "UPDATE category SET name = ? WHERE id = ?"
    private val sqlDeleteCategoryLocations = "DELETE FROM location WHERE category_id = ?"
    private val sqlDelete = "DELETE FROM category WHERE id = ?"
    private val sqlSelect = "SELECT * FROM category ORDER BY name"

    var connection: Connection? = null

    fun add(category: Category) {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsert).use {
                it.setString(1, category.name)
                it.executeUpdate()
            }
        }
    }

    fun rename(category: Category) {
        connection?.let { connection ->
            connection.prepareStatement(sqlUpdate).use {
                it.setString(1, category.name)
                it.setInt(2, category.id)
                it.executeUpdate()
            }
        }
    }

    fun delete(category: Category) {
        connection?.let { connection ->
            connection.prepareStatement(sqlDeleteCategoryLocations).use {
                it.setInt(1, category.id)
                it.executeUpdate()
            }
            connection.prepareStatement(sqlDelete).use {
                it.setInt(1, category.id)
                it.executeUpdate()
            }
        }
    }

    fun findAll(): List<Category> {
        val categoryList: MutableList<Category> = mutableListOf()
        connection?.let { connection ->
            connection.prepareStatement(sqlSelect).use {
                val result = it.executeQuery()
                while (result.next()) {
                    val category = Category(result.getInt("id"), result.getString("name"))
                    categoryList.add(category)
                }
            }
        }
        return categoryList
    }
}
