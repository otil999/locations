package hu.otilsoft.locations

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.ResourceBundle
import java.util.function.BiPredicate
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kotlin.random.Random
import kotlin.streams.toList
import tornadofx.App
import tornadofx.Controller
import tornadofx.View
import tornadofx.get
import tornadofx.imageview
import tornadofx.launch
import tornadofx.selectWhere
import tornadofx.selectedItem
import tornadofx.text
import tornadofx.toObservable


fun main(args: Array<String>) {
    launch<LocationsApp>(args)
}


class LocationsApp : App(MainView::class)


data class LocationRow(
    val id: Label,
    val name: TextField,
    val description: TextField,
    val type: TextField,
    val visited: CheckBox,
    val actions: HBox
)

class MainView : View() {
    init {
        messages = ResourceBundle.getBundle("main")
        title = messages["WINDOW.TITLE"]
    }

    private val controller: MainController by inject()
    override val root: VBox by fxml("/main.fxml")
    private val buttonNewGame: Button by fxid("buttonNewGame")
    private val buttonEditGame: Button by fxid("buttonEditGame")
    private val buttonCloneGame: Button by fxid("buttonCloneGame")
    private val buttonDeleteGame: Button by fxid("buttonDeleteGame")
    private val buttonNewMod: Button by fxid("buttonNewMod")
    private val buttonEditMod: Button by fxid("buttonEditMod")
    private val buttonDeleteMod: Button by fxid("buttonDeleteMod")
    private val buttonImportLocations: Button by fxid("buttonImportLocations")
    private val buttonRandomLocation: Button by fxid("buttonRandomLocation")
    private val comboGame: ComboBox<String> by fxid("comboGame")
    private val comboMod: ComboBox<Mod> by fxid("comboMod")
    private val tableLocation: TableView<LocationRow> by fxid("tableLocation")
    private val textLocation: TextField by fxid("textLocation")
//    private val buttonSave: Button by fxid("buttonSave")
    private val vboxTable: VBox by fxid("vboxTable")
    private val splitPane: SplitPane by fxid("splitPane")
    private val scrollPaneLocation: ScrollPane by fxid("scrollPaneLocation")

    override fun onBeforeShow() {
        super.onBeforeShow()
        tableLocation.columns[0].cellValueFactory = PropertyValueFactory("id")
        tableLocation.columns[1].cellValueFactory = PropertyValueFactory("visited")
        tableLocation.columns[2].cellValueFactory = PropertyValueFactory("name")
        tableLocation.columns[3].cellValueFactory = PropertyValueFactory("description")
        tableLocation.columns[4].cellValueFactory = PropertyValueFactory("type")
        tableLocation.columns[5].cellValueFactory = PropertyValueFactory("actions")
        splitPane.dividers[0].positionProperty().addListener { observableValue, old, new -> dividerResized() }
        fillGameCombo()
    }

    fun onNewGame() {
        val dialog = find<EditGameDialog>(
            "updateMode" to false,
            "databaseName" to ""
        )
        dialog.openModal(block = true)
        if (!dialog.isCanceled()) {
            fillGameCombo()
        }
    }

    fun onEditGame() {
        val dialog = find<EditGameDialog>(
            "updateMode" to true,
            "databaseName" to comboGame.selectedItem
        )
        dialog.openModal(block = true)
        if (!dialog.isCanceled()) {
            tableLocation.items = emptyList<LocationRow>().toObservable()
            fillGameCombo()
            buttonEditGame.isDisable = true
            buttonCloneGame.isDisable = true
            buttonDeleteGame.isDisable = true
            comboMod.items.clear()
        }
    }

    fun onCloneGame() {
        controller.cloneDatabase(comboGame.selectedItem!!)
        fillGameCombo()
    }

    fun onDeleteGame() {
        controller.deleteDatabase(comboGame.selectedItem!!)
        tableLocation.items = emptyList<LocationRow>().toObservable()
        comboMod.items.clear()
        buttonEditGame.isDisable = true
        buttonCloneGame.isDisable = true
        buttonDeleteGame.isDisable = true
        buttonNewMod.isDisable = true
        buttonEditMod.isDisable = true
        buttonDeleteMod.isDisable = true
        buttonRandomLocation.isDisable = true
        buttonImportLocations.isDisable = true
        fillGameCombo()
    }

    private fun dividerResized() {
        println(scrollPaneLocation.width)
        tableLocation.columns[0].prefWidth = scrollPaneLocation.width * 0.1
        tableLocation.columns[1].prefWidth = scrollPaneLocation.width * 0.1
        tableLocation.columns[2].prefWidth = scrollPaneLocation.width * 0.2
        tableLocation.columns[3].prefWidth = scrollPaneLocation.width * 0.3
        tableLocation.columns[4].prefWidth = scrollPaneLocation.width * 0.1
        tableLocation.columns[5].prefWidth = scrollPaneLocation.width * 0.2
    }

    private fun fillGameCombo() {
        comboGame.items = FXCollections.observableArrayList(controller.loadGames().map { it.toString() })
    }

    private fun fillModCombo() {
        controller.createModList()
        comboMod.items = FXCollections.observableArrayList(controller.mods)
        comboMod.selectionModel.selectFirst()
    }

    fun onSelectGame() {
        if (comboGame.selectedItem?.isNotEmpty() == true) {
            controller.openGameConnection(comboGame.selectedItem!!)

            fillModCombo()

            buttonEditGame.isDisable = false
            buttonCloneGame.isDisable = false
            buttonDeleteGame.isDisable = false

            buttonNewMod.isDisable = false
        }
    }

    fun onSelectMod() {
        buttonEditMod.isDisable = false
        buttonDeleteMod.isDisable = false

        buttonImportLocations.isDisable = false
        buttonRandomLocation.isDisable = false

        tableLocation.items.clear()
        val locations = comboMod
            .selectedItem?.let {
                controller.selectLocationByMod(it)
            } ?: emptyList()
        var idx = 0
        locations.map { location ->
            LocationRow(
                Label(location.id.toString()).apply {
                    this.alignment = Pos.CENTER_LEFT
                    this.maxHeight = Double.MAX_VALUE
                },
                TextField(location.name).apply {
                    this.maxWidth = Double.MAX_VALUE
                },
                TextField(location.description).apply {
                    this.maxWidth = Double.MAX_VALUE
                },
                TextField(location.type).apply {
                    this.maxWidth = Double.MAX_VALUE
                },
                CheckBox().apply {
                    this.isSelected = location.visited
                    this.alignment = Pos.CENTER
                    this.maxWidth = Double.MAX_VALUE
                    this.maxHeight = Double.MAX_VALUE
                },
                HBox().apply {
                    add(Button("Save").apply {
                        imageview(Image("images/save-mini.png"))
                        this.userData = idx++
                        this.maxWidth = Double.MAX_VALUE
                        onAction = EventHandler {
                            val rowData = tableLocation.items[(it.source as Button).userData as Int]
                            controller.updateLocation(
                                Location(
                                    rowData.id.text.toInt(),
                                    rowData.name.text,
                                    rowData.description.text,
                                    rowData.visited.isSelected,
                                    rowData.type.text,
                                    comboMod.selectedItem?.id ?: 0
                                )
                            )
                        }
                    })
                    add(Button("Delete").apply {
                        this.maxWidth = Double.MAX_VALUE
                    })
                    maxWidth = Double.MAX_VALUE
                    alignment = Pos.CENTER
                    spacing = 5.0
                }
            )
        }.forEach { tableLocation.items.add(it) }
        tableLocation.items.add(
            LocationRow(Label(), TextField(), TextField(), TextField(), CheckBox(), HBox())
        )
    }

    fun onNewMod() {
        val dialog = find<EditModDialog>(
            "updateMode" to false,
            "mod" to Mod()
        )
        dialog.openModal(block = true)
        if (!dialog.isCanceled()) {
            fillModCombo()
        }
    }

    fun onEditMod() {
        val dialog = find<EditModDialog>(
            "updateMode" to true,
            "mod" to comboMod.selectedItem
        )
        dialog.openModal(block = true)
        if (!dialog.isCanceled()) {
            fillModCombo()
        }
    }

    fun onDeleteMod() {
        controller.deleteMod(comboMod.selectedItem!!)
        comboMod.items.remove(comboMod.selectedItem)
        if (comboMod.items.isEmpty()) {
            buttonEditMod.isDisable = true
            buttonDeleteMod.isDisable = true
            buttonImportLocations.isDisable = true
            buttonRandomLocation.isDisable = true
            tableLocation.items.clear()
        } else {
            fillModCombo()
        }
    }

    fun saveLocation() {
        controller.addLocation(Location(0, textLocation.text, "",false, "", comboMod.selectedItem?.id ?: 0))
    }

    fun importLocations() {
        controller.importLocationsFromCsv(
            FileChooser().apply {
                initialDirectory = controller.dataRoot.toAbsolutePath().toFile()
                extensionFilters.addAll(FileChooser.ExtensionFilter("CSV file", "*.csv"))
            }.showOpenDialog(this.currentWindow),
            comboMod.selectedItem?.id ?: 0
        )
        onSelectMod()
    }

    fun chooseRandomLocation() {
        val notVisitedList = tableLocation.items.filter { locationRow -> !locationRow.visited.isSelected }.toList()
        val rnd = Random.nextInt(notVisitedList.size)
        val locationRow = notVisitedList[rnd]
        tableLocation.selectWhere(true) { l -> locationRow == l }
    }
}


class MainController : Controller() {
    val dataRoot: Path = Paths.get("")
    var connection: Connection? = null
    var selectedGameIdx: Int = -1

    private val createModTable = """
        CREATE TABLE IF NOT EXISTS mod (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE
        );
    """.trimIndent()

    private val insertMod = "INSERT INTO mod (name) VALUES (?)"

    private val updateMod = "UPDATE mod SET name = ? WHERE id = ?"

    private val deleteMod = "DELETE FROM mod WHERE id = ?"

    private val selectMod = "SELECT * FROM mod ORDER BY name"

    private val selectLocationByMod = """
        SELECT *
        FROM location
        WHERE mod_id = ?
        ORDER BY name
    """.trimIndent()

    private val createLocationTable = """
        CREATE TABLE IF NOT EXISTS location (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE,
            description TEXT,
            visited BOOLEAN NOT NULL DEFAULT FALSE,
            mod_id INTEGER NOT NULL,
            type STRING NOT NULL,
            FOREIGN KEY (mod_id) REFERENCES mod(id) ON DELETE CASCADE
        )
    """.trimIndent()

    private val updateLocation = """
        UPDATE location
            SET name = ?, description = ?, type = ?, visited = ?
            WHERE id = ?
    """.trimIndent()

    private val insertLocation = "INSERT INTO location (name, mod_id) VALUES (?, ?)"

    private val deleteModLocations = "DELETE FROM location WHERE mod_id = ?"

    var mods: List<Mod> = emptyList()
        private set

    fun createDatabase(name: String) {
        DriverManager.getConnection("jdbc:sqlite:${name}.db").use { connection ->
            connection.createStatement().use { it.execute(createModTable) }
            connection.createStatement().use { it.execute(createLocationTable) }
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

    fun cloneDatabase(name: String) {
        val currentDatabase = dataRoot.resolve("${name}.db").toFile()
        val clonedDatabase = dataRoot.resolve("$name Clone.db").toFile()
        currentDatabase.copyTo(clonedDatabase, overwrite = true)
    }

    fun deleteDatabase(name: String) {
        if (connection != null && !connection!!.isClosed) {
            connection!!.close()
        }
        dataRoot.resolve("${name}.db").toFile().delete()
    }

    fun addMod(mod: Mod) {
        connection!!.prepareStatement(insertMod).use {
            it.setString(1, mod.name)
            it.executeUpdate()
        }
    }

    fun renameMod(newMod: Mod) {
        connection!!.prepareStatement(updateMod).use {
            it.setString(1, newMod.name)
            it.setInt(2, newMod.id)
            it.executeUpdate()
        }
    }

    fun deleteMod(mod: Mod) {
        connection!!.prepareStatement(deleteModLocations).use {
            it.setInt(1, mod.id)
            it.executeUpdate()
        }
        connection!!.prepareStatement(deleteMod).use {
            it.setInt(1, mod.id)
            it.executeUpdate()
        }
    }

    fun addLocation(location: Location) {
        connection!!.createStatement().use {
            it.execute(createLocationTable)
        }
        connection!!.prepareStatement(insertLocation).use {
            it.setString(1, location.name)
            it.setInt(2, location.modId)
            it.executeUpdate()
        }
    }

    fun openGameConnection(game: String) {
        if (connection != null && !connection!!.isClosed) {
            connection!!.close()
        }

        val gamePath = dataRoot.toAbsolutePath().resolve("${game}.db")
        connection = DriverManager.getConnection("jdbc:sqlite:${gamePath}")
        println(connection?.toString())
    }

    fun loadGames() =
        Files
            .find(dataRoot, 1, BiPredicate {
                    path, attrs -> attrs.isRegularFile && path.toString().endsWith(".db")
            })
            .map { it.toString().replace(".db", "") }
            .toList()

    fun createModList() {
        connection!!.prepareStatement(selectMod).use {
            val result = it.executeQuery()
            val modList: MutableList<Mod> = mutableListOf()
            while (result.next()) {
                val mod = Mod(result.getInt("id"), result.getString("name"))
                modList.add(mod)
            }
            mods = modList
        }
    }

    fun selectLocationByMod(mod: Mod) : List<Location> {
        val locationList: MutableList<Location> = mutableListOf()
        try {
            connection!!.prepareStatement(selectLocationByMod).use {
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
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return locationList
    }

    fun importLocationsFromCsv(csv: File, modId: Int) {
        csv.useLines { lines ->
            for (line in lines.iterator()) {
                val fields = line.split("|")
                val sql = "INSERT INTO location (name, description, type, mod_id) VALUES (?, ?, ?, ?)"
                connection!!.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, fields[0])
                    stmt.setString(2, fields[1])
                    stmt.setString(3, fields[2])
                    stmt.setInt(4, modId)
                    stmt.executeUpdate()
                }
            }
        }
    }

    fun updateLocation(location: Location) {
        connection!!.prepareStatement(updateLocation).use {
            it.setString(1, location.name)
            it.setString(2, location.description)
            it.setString(3, location.type)
            it.setBoolean(4, location.visited)
            it.setInt(5, location.id)
            it.executeUpdate()
        }
    }
}
