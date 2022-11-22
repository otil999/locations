package hu.otilsoft.locations.manager

import hu.otilsoft.locations.Location
import hu.otilsoft.locations.Category
import java.sql.Connection
import java.sql.PreparedStatement


class LocationManager {
    private val sqlInsert = "INSERT INTO location (name, description, type, visited, category_id) VALUES (?, ?, ?, ?, ?)"
    private val sqlDelete = "DELETE FROM location WHERE id = ?"
    private val sqlUpdate = "UPDATE location SET name = ?, description = ?, type = ?, visited = ? WHERE id = ?"
    private val sqlSelectByCategory = "SELECT * FROM location WHERE category_id = ? ORDER BY id"
    private val sqlResetAll = "UPDATE location SET visited = FALSE WHERE category_id = ?"

    var connection: Connection? = null

    fun add(location: Location) {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsert).use {
                fillCommonParamsInStatement(it, location)
                it.setInt(5, location.modId)
                it.executeUpdate()
            }
        }
    }
    
    private fun fillCommonParamsInStatement(statement: PreparedStatement, location: Location) {
        statement.setString(1, location.name)
        statement.setString(2, location.description)
        statement.setString(3, location.type)
        statement.setBoolean(4, location.visited)
    }

    fun update(location: Location) {
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

    fun findByCategory(category: Category) : List<Location> {
        val locationList: MutableList<Location> = mutableListOf()
        connection?.let { connection ->
            connection.prepareStatement(sqlSelectByCategory).use {
                it.setInt(1, category.id)
                val result = it.executeQuery()
                while (result.next()) {
                    locationList.add(
                            Location(
                                    result.getInt("id"),
                                    result.getString("name"),
                                    result.getString("description"),
                                    result.getBoolean("visited"),
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
