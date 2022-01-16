package hu.otilsoft.locations.manager

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.util.function.BiPredicate
import kotlin.streams.toList

class GameManager {
    val dataRoot: Path = Paths.get("")
    private var connection: Connection? = null

    private val sqlCreateCategoryTable = """
        CREATE TABLE IF NOT EXISTS category (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE
        )
    """.trimIndent()

    private val sqlCreateLocationTable = """
        CREATE TABLE IF NOT EXISTS location (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT,
            visited BOOLEAN NOT NULL DEFAULT FALSE,
            category_id INTEGER NOT NULL,
            type STRING NOT NULL,
            FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
            UNIQUE (category_id,name)
        )
    """.trimIndent()

    fun openConnection(game: String): Connection {
        closeConnection()

        val gamePath = dataRoot.toAbsolutePath().resolve("${game}.db")
        connection = DriverManager.getConnection("jdbc:sqlite:${gamePath}")

        return connection!!
    }

    fun closeConnection() {
        if (connection != null && !connection!!.isClosed) {
            connection!!.close()
        }
    }

    fun createDatabase(name: String) {
        openConnection(name).use { connection ->
            connection.createStatement().use { it.execute(sqlCreateCategoryTable) }
            connection.createStatement().use { it.execute(sqlCreateLocationTable) }
        }
    }

    fun renameDatabase(oldName: String, newName: String) {
        if (connection != null && !connection!!.isClosed) {
            connection!!.close()
        }

        if (oldName != newName) {
            val oldDatabase = dataRoot.resolve("${oldName}.db").toFile()
            val newDatabase = dataRoot.resolve("${newName}.db").toFile()
            oldDatabase.renameTo(newDatabase)
        }
    }

    fun cloneDatabase(name: String): String {
        val currentDatabase = dataRoot.resolve("${name}.db").toFile()
        val clonedName = "$name Clone"
        val clonedDatabase = dataRoot.resolve("${clonedName}.db").toFile()
        currentDatabase.copyTo(clonedDatabase, overwrite = true)
        return clonedName
    }

    fun deleteDatabase(name: String) {
        if (connection != null && !connection!!.isClosed) {
            connection!!.close()
        }
        dataRoot.resolve("${name}.db").toFile().delete()
    }

    fun findGames() =
            Files
                    .find(dataRoot, 1, BiPredicate {
                        path, attrs -> attrs.isRegularFile && path.toString().endsWith(".db")
                    })
                    .map { it.toString().replace(".db", "") }
                    .toList()
}
