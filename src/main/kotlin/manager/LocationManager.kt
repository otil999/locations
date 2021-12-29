package hu.otilsoft.locations.manager

import hu.otilsoft.locations.Location
import hu.otilsoft.locations.Mod
import java.sql.Connection
import java.sql.PreparedStatement


class LocationManager {
    private val sqlInsertLocation = "INSERT INTO location (name, description, type, visited, mod_id) VALUES (?, ?, ?, ?, ?)"
    private val sqlDeleteLocation = "DELETE FROM location WHERE id = ?"
    private val sqlUpdateLocation = "UPDATE location SET name = ?, description = ?, type = ?, visited = ? WHERE id = ?"
    private val sqlSelectLocationByMod = "SELECT * FROM location WHERE mod_id = ? ORDER BY id"

    var connection: Connection? = null

    fun addLocation(location: Location) {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsertLocation).use {
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

    fun updateLocation(location: Location) {
        connection?.let { connection ->
            connection.prepareStatement(sqlInsertLocation).use {
                fillCommonParamsInStatement(it, location)
                it.setInt(5, location.id)
                it.executeUpdate()
            }
        }
    }

    fun deleteLocation(id: Int) {
        connection?.let { connection ->
            connection.prepareStatement(sqlDeleteLocation).use {
                it.setInt(1, id)
                it.executeUpdate()
            }
        }
    }

    fun findLocationsByModId(mod: Mod) : List<Location> {
        val locationList: MutableList<Location> = mutableListOf()
        connection?.let { connection ->
            connection.prepareStatement(sqlSelectLocationByMod).use {
                it.setInt(1, mod.id)
                val result = it.executeQuery()
                while (result.next()) {
                    locationList.add(
                            Location(
                                    result.getInt("id"),
                                    result.getString("name"),
                                    result.getString("description"),
                                    result.getBoolean("visited"),
                                    result.getString("type"),
                                    result.getInt("mod_id")
                            )
                    )
                }
            }
        }
        return locationList
    }
}
