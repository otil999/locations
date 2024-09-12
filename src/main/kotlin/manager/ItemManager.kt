package hu.otilsoft.locations.manager

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.Item
import java.sql.Connection
import java.sql.PreparedStatement


class ItemManager {
    private val sqlInsert = "INSERT INTO item (name, description, type, passed, category_id) VALUES (?, ?, ?, ?, ?)"
    private val sqlDelete = "DELETE FROM item WHERE id = ?"
    private val sqlUpdate = "UPDATE item SET name = ?, description = ?, type = ?, passed = ? WHERE id = ?"
    private val sqlSelectByCategory = "SELECT * FROM item WHERE category_id = ? ORDER BY id"
    private val sqlResetAll = "UPDATE item SET visited = FALSE WHERE category_id = ?"

    var connection: Connection? = null

    fun add(item: Item) {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsert).use {
                fillCommonParamsInStatement(it, item)
                it.setInt(5, item.modId)
                it.executeUpdate()
            }
        }
    }
    
    private fun fillCommonParamsInStatement(statement: PreparedStatement, item: Item) {
        statement.setString(1, item.name)
        statement.setString(2, item.description)
        statement.setString(3, item.type)
        statement.setBoolean(4, item.visited)
    }

    fun update(location: Item) {
        connection?.let { connection ->
            connection.prepareStatement(sqlUpdate).use {
                fillCommonParamsInStatement(it, location)
                it.setInt(5, location.id)
                it.executeUpdate()
            }
        }
    }

    fun delete(id: Int) {
        connection?.let { connection ->
            connection.prepareStatement(sqlDelete).use {
                it.setInt(1, id)
                it.executeUpdate()
            }
        }
    }

    fun findByCategory(category: Category) : List<Item> {
        val locationList: MutableList<Item> = mutableListOf()
        connection?.let { connection ->
            connection.prepareStatement(sqlSelectByCategory).use {
                it.setInt(1, category.id)
                val result = it.executeQuery()
                while (result.next()) {
                    locationList.add(
                            Item(
                                    result.getInt("id"),
                                    result.getString("name"),
                                    result.getString("description"),
                                    result.getBoolean("passed"),
                                    result.getString("type"),
                                    result.getInt("category_id")
                            )
                    )
                }
            }
        }
        return locationList
    }

    fun resetAll(category: Category) {
        connection?.let { connection ->
            connection.prepareStatement(sqlResetAll).use {
                it.setInt(1, category.id)
                it.executeUpdate()
            }
        }
    }
}
