package hu.otilsoft.locations.manager

import hu.otilsoft.locations.Category
import java.sql.Connection


class CategoryManager {
    private val sqlInsert = "INSERT INTO category (name) VALUES (?)"
    private val sqlUpdate = "UPDATE category SET name = ? WHERE id = ?"
    private val sqlDeleteCategoryLocations = "DELETE FROM location WHERE category_id = ?"
    private val sqlDelete = "DELETE FROM category WHERE id = ?"
    private val sqlSelect = "SELECT * FROM category ORDER BY name"
    private val sqlGetByName = "SELECT * FROM category WHERE name = ?"

    var connection: Connection? = null

    fun add(category: Category): Category {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsert).use {
                it.setString(1, category.name)
                it.executeUpdate()
            }
        }
        return category
    }

    fun rename(category: Category, newName: String): Category {
        connection?.let { connection ->
            connection.prepareStatement(sqlUpdate).use {
                it.setString(1, newName)
                it.setInt(2, category.id)
                it.executeUpdate()
            }
        }
        return Category(category.id, newName)
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

    fun findByName(name: String): Category {
        connection?.let { connection ->
            connection.prepareStatement(sqlGetByName).use {
                it.setString(1, name)
                val result = it.executeQuery()
                if (result.next()) {
                    return Category(result.getInt("id"), result.getString("name"))
                }
            }
        }
        return Category()
    }
}
