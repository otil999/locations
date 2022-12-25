package hu.otilsoft.locations

import hu.otilsoft.locations.dialog.*
import hu.otilsoft.locations.manager.GameManager
import hu.otilsoft.locations.manager.LocationManager
import hu.otilsoft.locations.manager.CategoryManager
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import tornadofx.*
import kotlin.random.Random


fun main(args: Array<String>) {
    launch<LocationsApp>(args)
}


class LocationsApp : App(MainView::class)


class MainView : View() {
    data class LocationRow(
            val id: Label,
            val name: TextField,
            val description: TextField,
            val type: TextField,
            val visited: CheckBox,
            val actions: HBox
    )

    init {
        messages = ResourceBundle.getBundle("main")
        title = messages["WINDOW.TITLE"]
        primaryStage.icons.add(Image("images/locations.png"))
        primaryStage.onCloseRequest = EventHandler {
            controller.gameManager.closeConnection()
        }
    }

    private val controller: MainController by inject()
    override val root: VBox by fxml("/main.fxml")
    private val buttonNewGame: Button by fxid("buttonNewGame")
    private val buttonRenameGame: Button by fxid("buttonRenameGame")
    private val buttonCloneGame: Button by fxid("buttonCloneGame")
    private val buttonDeleteGame: Button by fxid("buttonDeleteGame")
    private val buttonNewMod: Button by fxid("buttonNewMod")
    private val buttonRenameMod: Button by fxid("buttonEditMod")
    private val buttonDeleteMod: Button by fxid("buttonDeleteMod")
    private val buttonInsertLocation: Button by fxid("buttonInsertLocation")
    private val buttonImportLocations: Button by fxid("buttonImportLocations")
    private val buttonRandomLocation: Button by fxid("buttonRandomLocation")
    private val comboGame: ComboBox<String> by fxid("comboGame")
    private val comboCategory: ComboBox<Category> by fxid("comboMod")
    private val tableLocation: TableView<LocationRow> by fxid("tableLocation")
    private val textLocation: TextField by fxid("textLocation")
    private val textFieldSearchLocation: TextField by fxid("textFieldSearchLocation")
    private val buttonSearchLocationNext: Button by fxid("buttonSearchLocationNext")
    private val buttonSearchLocationPrev: Button by fxid("buttonSearchLocationPrev")
    private val progressBarLocations: ProgressBar by fxid("progressBarLocations")
    private val labelProgress: Label by fxid("labelProgress")
    private val buttonResetLocations: Button by fxid("buttonResetLocations")

    override fun onBeforeShow() {
        super.onBeforeShow()
        buttonNewGame.imageview(Image("images/new-game.png"))
        buttonRenameGame.imageview(Image("images/rename-game.png"))
        buttonCloneGame.imageview(Image("images/clone-game.png"))
        buttonDeleteGame.imageview(Image("images/delete-game.png"))
        buttonNewMod.imageview(Image("images/new-mod.png"))
        buttonRenameMod.imageview(Image("images/rename-mod.png"))
        buttonDeleteMod.imageview(Image("images/delete-mod.png"))
        buttonInsertLocation.imageview(Image("images/add-location.png"))
        buttonImportLocations.imageview(Image("images/import-locations.png"))
        buttonRandomLocation.imageview(Image("images/random-location.png"))
        buttonSearchLocationNext.imageview(Image("images/search-next.png"))
        buttonSearchLocationPrev.imageview(Image("images/search-prev.png"))
        buttonResetLocations.imageview(Image("images/reset-locations.png"))

        initColumns()
        fillGameCombo()
    }

    private fun initColumns() {
        tableLocation.columns[0].cellValueFactory = PropertyValueFactory("id")
        tableLocation.columns[0].comparator = Comparator { o1, o2 ->
            ((o1 as Label).text.toInt().compareTo((o2 as Label).text.toInt()))
        }
        tableLocation.columns[1].cellValueFactory = PropertyValueFactory("visited")
        tableLocation.columns[1].comparator = Comparator { o1, o2 ->
            ((o1 as CheckBox).isSelected).compareTo((o2 as CheckBox).isSelected)
        }
        tableLocation.columns[2].cellValueFactory = PropertyValueFactory("name")
        tableLocation.columns[2].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableLocation.columns[3].cellValueFactory = PropertyValueFactory("description")
        tableLocation.columns[3].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableLocation.columns[4].cellValueFactory = PropertyValueFactory("type")
        tableLocation.columns[4].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableLocation.columns[5].cellValueFactory = PropertyValueFactory("actions")
        tableLocation.columns[5].isSortable = false
    }

    private fun fillGameCombo() {
        comboGame.items = FXCollections.observableArrayList(controller.listGames().map { it.toString() })
    }

    fun onNewGame() = openEditGameDialog(false)
    fun onEditGame() = openEditGameDialog(true, comboGame.selectedItem!!)

    private fun openEditGameDialog(update: Boolean, name: String = "") {
        val dialog = find<EditGameDialog>(
                "updateMode" to update,
                "databaseName" to name,
                "gameManager" to controller.gameManager
        )
        dialog.openModal(block = true, owner = primaryStage)
        if (!dialog.isCanceled()) {
            fillGameCombo()
            comboGame.selectionModel.select(dialog.textName.text)
        }
    }

    fun onCloneGame() {
        val clone = controller.cloneGame(comboGame.selectedItem!!)
        fillGameCombo()
        comboGame.selectionModel.select(clone)
    }

    fun onDeleteGame() {
        val alert = alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete game '${comboGame.selectedItem}'?",
                buttons = arrayOf(ButtonType.NO, ButtonType.YES)
        )
        if (alert.result == ButtonType.YES) {
            controller.deleteGame(comboGame.selectedItem!!)
            disableGame()
            fillGameCombo()
        }
    }

    private fun disableGame() {
        buttonRenameGame.isDisable = true
        buttonCloneGame.isDisable = true
        buttonDeleteGame.isDisable = true
        disableCategory()
        buttonNewMod.isDisable = true
        disableLocation()
    }

    private fun enableGame() {
        buttonRenameGame.isDisable = false
        buttonCloneGame.isDisable = false
        buttonDeleteGame.isDisable = false
        buttonNewMod.isDisable = false
    }

    private fun fillModCombo() {
        comboCategory.items.clear()
        comboCategory.items = FXCollections.observableArrayList(controller.listCategories())
    }

    fun onSelectGame() {
        if (comboGame.selectedItem?.isNotEmpty() == true) {
            controller.updateConnection(comboGame.selectedItem!!)
            enableGame()
            fillModCombo()
            comboCategory.selectionModel.selectFirst()
        }
    }

    private fun disableCategory() {
        buttonNewMod.isDisable = false
        buttonRenameMod.isDisable = true
        buttonDeleteMod.isDisable = true
        comboCategory.items.clear()
        disableLocation()
    }

    private fun enableCategory() {
        buttonNewMod.isDisable = false
        buttonRenameMod.isDisable = false
        buttonDeleteMod.isDisable = false
        enableLocation()
    }

    private fun enableLocation() {
        buttonInsertLocation.isDisable = false
        buttonImportLocations.isDisable = false
        buttonRandomLocation.isDisable = false
        buttonResetLocations.isDisable = false
        enableSearchButtonsWhenTextNotEmpty()
    }

    private fun disableLocation() {
        buttonRandomLocation.isDisable = true
        buttonInsertLocation.isDisable = true
        buttonImportLocations.isDisable = true
        buttonResetLocations.isDisable = true
        enableSearchButtonsWhenTextNotEmpty()
        tableLocation.items.clear()
    }

    fun onSelectCategory() {
        tableLocation.items.clear()
        if (comboCategory.items.isEmpty() || comboCategory.selectedItem == null) {
            disableCategory()
        } else {
            enableCategory()
            val locations = controller.selectLocationByCategory(comboCategory.selectedItem!!)
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
                            this.userData = location.id
                            add(Button("Save").apply {
                                imageview(Image("images/save-mini.png"))
                                this.maxWidth = Double.MAX_VALUE
                                this.userData = location.id
                                onAction = EventHandler {
                                    val sourceSaveButton = it.source as Button
                                    val rowData = tableLocation.items.first { locationRow ->
                                        locationRow.actions.userData == sourceSaveButton.userData
                                    }
                                    controller.updateLocation(
                                            Location(
                                                    (it.source as Button).userData as Int,
                                                    rowData.name.text,
                                                    rowData.description.text,
                                                    rowData.visited.isSelected,
                                                    rowData.type.text,
                                                    comboCategory.selectedItem?.id ?: 0
                                            )
                                    )
                                    refreshStatus()
                                }
                            })
                            add(Button("Delete").apply {
                                imageview(Image("images/delete-mini.png"))
                                userData = location.id
                                maxWidth = Double.MAX_VALUE
                                onAction = EventHandler {
                                    if (alert(
                                                    Alert.AlertType.CONFIRMATION,
                                                    "Do you really want to delete this location?",
                                                    buttons = arrayOf(ButtonType.NO, ButtonType.YES)
                                            ).result == ButtonType.YES) {
                                        deleteLocation((it.source as Button).userData as Int)
                                        onSelectCategory()
                                    }
                                }
                            })
                            maxWidth = Double.MAX_VALUE
                            alignment = Pos.CENTER
                            spacing = 5.0
                        }
                )
            }.forEach { tableLocation.items.add(it) }
            refreshStatus()
        }
    }

    private fun refreshStatus() {
        val numberOfLocations = tableLocation.items.count()
        val numberOfVisitedLocations = tableLocation.items.count { it.visited.isSelected }
        labelProgress.text = "$numberOfVisitedLocations/$numberOfLocations"
        progressBarLocations.progress = numberOfVisitedLocations.toDouble() / numberOfLocations
    }

    private fun showEditCategoryDialog(update: Boolean, category: Category) {
        val dialog = find<EditCategoryDialog>(
                "updateMode" to update,
                "category" to category,
                "categoryManager" to controller.categoryManager
        )
        dialog.openModal(block = true, owner = primaryStage)
        if (!dialog.isCanceled()) {
            fillModCombo()
            println(dialog.textName.text)
            comboCategory.selectionModel.select(comboCategory.items.first { m -> m.name == dialog.textName.text })
        }
    }

    fun onNewMod() = showEditCategoryDialog(false, Category())

    fun onEditMod() = showEditCategoryDialog(true, comboCategory.selectedItem!!)

    fun onDeleteMod() {
        val alert = alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete this mod with all of its locations?",
                buttons = arrayOf(ButtonType.NO, ButtonType.YES)
        )
        if (alert.result == ButtonType.YES) {
            controller.deleteCategory(comboCategory.selectedItem!!)
            fillModCombo()
            if (comboCategory.items.isEmpty()) {
                disableCategory()
            } else {
                comboCategory.selectionModel.selectFirst()
            }
        }
    }

    fun onInsertLocation() {
        val dialog = find<InsertLocationDialog>(
                "categoryId" to (comboCategory.selectedItem as Category).id,
                "locationManager" to controller.locationManager
        )
        dialog.openModal(block = true, owner = primaryStage)
        if (!dialog.isCanceled()) {
            onSelectCategory()
        }
    }

    fun onImportLocations() {
        FileChooser().apply {
            initialDirectory = controller.dataDir()
            extensionFilters.addAll(FileChooser.ExtensionFilter("PSV file", "*.psv"))
        }.showOpenDialog(currentWindow)?.let {
            val dialog = find<ImportLocationsDialog>(
                    "locationManager" to controller.locationManager,
                    "psv" to it,
                    "category" to comboCategory.selectedItem
            )
            dialog.openModal(owner = primaryStage, block = true)
            onSelectCategory()
        }
    }

    private fun saveLocation() =
            controller.addLocation(
                Location(0, textLocation.text, "",false, "", comboCategory.selectedItem!!.id)
            )

    private fun deleteLocation(id: Int) = controller.deleteLocation(id)

    fun chooseRandomLocation() {
        val notVisitedList = tableLocation.items.filter { locationRow -> !locationRow.visited.isSelected }.toList()
        val rnd = Random.nextInt(notVisitedList.size)
        val locationRow = notVisitedList[rnd]
        tableLocation.selectWhere(true) { l -> locationRow == l }
    }

    fun onSearchLocation(event: Event) {
        val textToSearch = textFieldSearchLocation.text
        val foundLocations = tableLocation.items.filter { locationRow ->
            (locationRow.name.text.indexOf(textToSearch, ignoreCase = true) >= 0 ||
            locationRow.description.text.indexOf(textToSearch, ignoreCase = true) >= 0)
        }.toList()
        if (event.source == buttonSearchLocationNext && controller.foundLocationIdx < foundLocations.lastIndex) {
            controller.foundLocationIdx++
        } else if (event.source == buttonSearchLocationPrev && controller.foundLocationIdx > 0) {
            controller.foundLocationIdx--
        }
        if (controller.foundLocationIdx >= 0) {
            tableLocation.selectWhere(true) { row -> row == foundLocations[controller.foundLocationIdx] }
            buttonSearchLocationPrev.isDisable = false
        }
    }

    fun onKeyReleased() {
        controller.foundLocationIdx = -1
        enableSearchButtonsWhenTextNotEmpty()
        buttonSearchLocationPrev.isDisable = true
    }

    private fun enableSearchButtonsWhenTextNotEmpty() {
        if (textFieldSearchLocation.text.isEmpty()) {
            buttonSearchLocationPrev.isDisable = true
            buttonSearchLocationNext.isDisable = true
        } else {
            buttonSearchLocationNext.isDisable = false
            buttonSearchLocationPrev.isDisable = false
        }
    }

    fun onResetLocations() {
        val alert = alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to reset all locations?",
                buttons = arrayOf(ButtonType.NO, ButtonType.YES)
        )
        if (alert.result == ButtonType.YES) {
            controller.resetAllLocations(comboCategory.selectedItem!!)
            onSelectCategory()
        }
    }
}


class MainController : Controller() {
    val gameManager = GameManager()
    val categoryManager = CategoryManager()
    val locationManager = LocationManager()
    var foundLocationIdx = -1

    internal fun dataDir() = gameManager.dataRoot.toAbsolutePath().toFile()

    internal fun updateConnection(game: String) {
        val connection = gameManager.openConnection(game)
        categoryManager.connection = connection
        locationManager.connection = connection
    }

    internal fun listGames() = gameManager.findGames()
    internal fun cloneGame(game: String) = gameManager.cloneDatabase(game)
    internal fun deleteGame(game: String) = gameManager.deleteDatabase(game)

    internal fun deleteCategory(category: Category) = categoryManager.delete(category)
    internal fun listCategories() = categoryManager.findAll()

    internal fun addLocation(location: Location) = locationManager.add(location)
    internal fun deleteLocation(id: Int) = locationManager.delete(id)
    internal fun selectLocationByCategory(category: Category) : List<Location> = locationManager.findByCategory(category)
    internal fun updateLocation(location: Location) = locationManager.update(location)
    internal fun resetAllLocations(category: Category) = locationManager.resetAll(category)
}
