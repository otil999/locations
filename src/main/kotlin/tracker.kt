package hu.otilsoft.tracker

import hu.otilsoft.tracker.dialog.*
import hu.otilsoft.tracker.manager.ProjectManager
import hu.otilsoft.tracker.manager.ItemManager
import hu.otilsoft.tracker.manager.CategoryManager
import javafx.application.Application
import javafx.application.Application.launch
import javafx.beans.binding.Bindings
import java.util.ResourceBundle
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.net.URL
import kotlin.random.Random


fun main(args: Array<String>) {
    launch(ProgressTrackerApplication::class.java, *args)
}


fun imageView(resources: ResourceBundle, id: String): ImageView = ImageView(Image(resources.getString(id)))


class NoButtonBarDialogPane : DialogPane() {
    override fun createButtonBar(): Node? {
        return null
    }
}


class ProgressTrackerApplication : Application() {
    override fun start(primaryStage: Stage) {
        val fxmlLoader = FXMLLoader(this.javaClass.getResource("/main.fxml"), ResourceBundle.getBundle("main"))
        fxmlLoader.setController(MainController(primaryStage))

        primaryStage.scene = Scene(fxmlLoader.load())
        primaryStage.show()
    }
}


class MainController(private val stage: Stage) : Initializable {
    data class ItemRow(
            val id: Label,
            val name: TextField,
            val description: TextField,
            val type: TextField,
            val visited: CheckBox,
            val actions: HBox
    )

    private val resources = ResourceBundle.getBundle("main")
    private val itemResources = ResourceBundle.getBundle("item")

    @FXML private lateinit var comboProject: ComboBox<String>
    @FXML private lateinit var comboCategory: ComboBox<Category>
    @FXML private lateinit var buttonNewProject: Button
    @FXML private lateinit var buttonRenameProject: Button
    @FXML private lateinit var buttonCloneProject: Button
    @FXML private lateinit var buttonDeleteProject: Button
    @FXML private lateinit var buttonNewCategory: Button
    @FXML private lateinit var buttonRenameCategory: Button
    @FXML private lateinit var buttonCloneCategory: Button
    @FXML private lateinit var buttonDeleteCategory: Button
    @FXML private lateinit var buttonInsertItem: Button
    @FXML private lateinit var buttonImportItem: Button
    @FXML private lateinit var buttonRandomItem: Button
    @FXML private lateinit var buttonResetItem: Button
    @FXML private lateinit var buttonSearchItemNext: Button
    @FXML private lateinit var buttonSearchItemPrev: Button
    @FXML private lateinit var tableItems: TableView<ItemRow>
    @FXML private lateinit var labelProgress: Label
    @FXML private lateinit var progressBarItems: ProgressBar
    @FXML private lateinit var textFieldSearchItem: TextField
    @FXML private lateinit var textItem: TextField

    override fun initialize(url: URL, resources: ResourceBundle) {
        stage.icons.add(Image(resources.getString("WINDOW.ICON")))
        stage.title = resources.getString("WINDOW.TITLE")
        stage.width = 1024.0
        stage.height = 768.0

        buttonNewProject.graphic = imageView(resources,"BUTTON.NEW_PROJECT.ICON")
        buttonRenameProject.graphic = imageView(resources, "BUTTON.RENAME_PROJECT.ICON")
        buttonCloneProject.graphic = imageView(resources, "BUTTON.CLONE_PROJECT.ICON")
        buttonDeleteProject.graphic = imageView(resources, "BUTTON.DELETE_PROJECT.ICON")
        buttonNewCategory.graphic = imageView(resources, "BUTTON.NEW_CATEGORY.ICON")
        buttonRenameCategory.graphic = imageView(resources, "BUTTON.RENAME_CATEGORY.ICON")
        buttonCloneCategory.graphic = imageView(resources, "BUTTON.CLONE_CATEGORY.ICON")
        buttonCloneCategory.disableProperty().bind(comboCategory.valueProperty().isNull)
        buttonDeleteCategory.graphic = imageView(resources, "BUTTON.DELETE_CATEGORY.ICON")
        buttonInsertItem.graphic = imageView(resources, "BUTTON.INSERT_ITEM.ICON")
        buttonImportItem.graphic = imageView(resources, "BUTTON.IMPORT_ITEM.ICON")
        buttonRandomItem.graphic = imageView(resources, "BUTTON.RANDOM_ITEM.ICON")
        buttonResetItem.graphic = imageView(resources, "BUTTON.RESET_ITEM.ICON")
        buttonSearchItemNext.graphic = imageView(resources, "BUTTON.FIND_NEXT.ICON")
        buttonSearchItemPrev.graphic = imageView(resources,"BUTTON.FIND_PREV.ICON")

        tableItems.columns[0].cellValueFactory = PropertyValueFactory("id")
        tableItems.columns[0].comparator = Comparator { o1, o2 ->
            ((o1 as Label).text.toInt().compareTo((o2 as Label).text.toInt()))
        }
        tableItems.columns[1].cellValueFactory = PropertyValueFactory("visited")
        tableItems.columns[1].comparator = Comparator { o1, o2 ->
            ((o1 as CheckBox).isSelected).compareTo((o2 as CheckBox).isSelected)
        }
        tableItems.columns[2].cellValueFactory = PropertyValueFactory("name")
        tableItems.columns[2].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableItems.columns[3].cellValueFactory = PropertyValueFactory("description")
        tableItems.columns[3].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableItems.columns[4].cellValueFactory = PropertyValueFactory("type")
        tableItems.columns[4].comparator = Comparator { o1, o2 ->
            (o1 as TextField).text.compareTo((o2 as TextField).text)
        }
        tableItems.columns[5].cellValueFactory = PropertyValueFactory("actions")
        tableItems.columns[5].isSortable = false

        fillProjectCombo()
        fillCategoryCombo()

        stage.show()
    }

    @FXML
    fun onNewProject() = openEditProjectDialog(false)

    @FXML
    fun onEditProject() = openEditProjectDialog(true, comboProject.selectionModel.selectedItem)

    private fun openEditProjectDialog(update: Boolean, name: String = "") {
        val dialog = EditProjectDialog(this.stage, update, name, projectManager)
        if (dialog.showAndWait().get() == ButtonType.OK) {
            fillProjectCombo()
            comboProject.selectionModel.select(dialog.savedCategory())
        }
    }

    private fun fillProjectCombo() {
        comboProject.items = FXCollections.observableArrayList(listProjects())
    }

    @FXML
    fun onCloneProject() {
        val clone = cloneProject(comboProject.selectionModel.selectedItem)
        fillProjectCombo()
        comboProject.selectionModel.select(clone)
    }

    @FXML
    fun onDeleteProject() {
        val alert = Alert(
            Alert.AlertType.CONFIRMATION,
            "Do you really want to delete game '${comboProject.selectionModel.selectedItem}'?",
            ButtonType.NO, ButtonType.YES
        )
            .apply { headerText = resources.getString("CONFIRM") }
            .showAndWait()

        if (alert.isPresent && alert.get() == ButtonType.YES) {
            deleteProject(comboProject.selectionModel.selectedItem)
            disableProject()
            fillProjectCombo()
        }
    }

    private fun disableProject() {
        buttonRenameProject.isDisable = true
        buttonCloneProject.isDisable = true
        buttonDeleteProject.isDisable = true
        disableCategory()
        buttonNewCategory.isDisable = true
        disableItem()
    }

    private fun enableItem() {
        buttonInsertItem.isDisable = false
        buttonImportItem.isDisable = false
        buttonRandomItem.isDisable = false
        buttonResetItem.isDisable = false
        enableSearchButtonsWhenTextNotEmpty()
    }

    private fun disableItem() {
        buttonRandomItem.isDisable = true
        buttonInsertItem.isDisable = true
        buttonImportItem.isDisable = true
        buttonResetItem.isDisable = true
        enableSearchButtonsWhenTextNotEmpty()
        tableItems.items.clear()
    }

    private fun enableSearchButtonsWhenTextNotEmpty() {
        if (textFieldSearchItem.text.isEmpty()) {
            buttonSearchItemPrev.isDisable = true
            buttonSearchItemNext.isDisable = true
        } else {
            buttonSearchItemNext.isDisable = false
            buttonSearchItemPrev.isDisable = false
        }
    }

    @FXML fun onSelectProject() {
        val selectedProject = comboProject.selectionModel.selectedItem ?: ""
        if (selectedProject.isNotEmpty()) {
            updateConnection(selectedProject)
            enableProject()
            fillCategoryCombo()
            comboCategory.selectionModel.selectFirst()
        }
    }

    private fun enableProject() {
        buttonRenameProject.isDisable = false
        buttonCloneProject.isDisable = false
        buttonDeleteProject.isDisable = false
        buttonNewCategory.isDisable = false
    }

    private fun fillCategoryCombo() {
        comboCategory.items.clear()
        comboCategory.items = FXCollections.observableArrayList(listCategories())
    }

    @FXML
    fun onNewCategory() = showEditCategoryDialog(false, Category())

    @FXML
    fun onEditCategory() = showEditCategoryDialog(true, comboCategory.selectionModel.selectedItem)

    private fun showEditCategoryDialog(update: Boolean, category: Category) {
        val dialog = EditCategoryDialog(this.stage, update, category, categoryManager)
        if (dialog.showAndWait().get() == ButtonType.OK) {
            fillCategoryCombo()
            comboCategory.selectionModel.select(dialog.savedCategory())
        }
    }

    @FXML
    fun onCloneCategory() {
        val categoryToClone = comboCategory.selectionModel.selectedItem
        val clone = categoryManager.add(Category(name = "Clone of ${categoryToClone.name}"))
        itemManager
            .findByCategory(categoryToClone)
            .onEach { it.categoryId = clone.id }
            .forEach { itemManager.add(it) }
        comboCategory.items.addAll(clone)
        comboCategory.selectionModel.select(clone)
    }

    @FXML fun onDeleteCategory() {
        val alert = Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete this mod with all of its locations?",
                ButtonType.NO, ButtonType.YES
        ).showAndWait()
        if (alert.isPresent && alert.get() == ButtonType.YES) {
            deleteCategory(comboCategory.selectionModel.selectedItem)
            fillCategoryCombo()
            if (comboCategory.items.isEmpty()) {
                disableCategory()
            } else {
                comboCategory.selectionModel.selectFirst()
            }
        }
    }

    @FXML
    fun onSelectCategory() {
        tableItems.items.clear()
        if (comboCategory.items.isEmpty() || comboCategory.selectionModel.selectedItem == null) {
            disableCategory()
        } else {
            enableCategory()
            val items = selectItemByCategory(comboCategory.selectionModel.selectedItem)
            val itemRows = items.map { item ->
                ItemRow(
                        Label(item.id.toString()).apply {
                            this.alignment = Pos.CENTER_LEFT
                            this.maxHeight = Double.MAX_VALUE
                        },
                        TextField(item.name).apply {
                            this.maxWidth = Double.MAX_VALUE
                        },
                        TextField(item.description).apply {
                            this.maxWidth = Double.MAX_VALUE
                        },
                        TextField(item.type).apply {
                            this.maxWidth = Double.MAX_VALUE
                        },
                        CheckBox().apply {
                            this.isSelected = item.visited
                            this.alignment = Pos.CENTER
                            this.maxWidth = Double.MAX_VALUE
                            this.maxHeight = Double.MAX_VALUE
                        },
                        HBox().apply {
                            this.userData = item.id
                            children.add(Button("Save").apply {
                                graphic = imageView(itemResources, "BUTTON.SAVE_ITEM.ICON")
                                maxWidth = Double.MAX_VALUE
                                userData = item.id
                                onAction = EventHandler {
                                    val sourceSaveButton = it.source as Button
                                    val rowData = tableItems.items.first { locationRow ->
                                        locationRow.actions.userData == sourceSaveButton.userData
                                    }
                                    updateItem(
                                        Item(
                                            (it.source as Button).userData as Int,
                                            rowData.name.text,
                                            rowData.description.text,
                                            rowData.visited.isSelected,
                                            rowData.type.text,
                                            comboCategory.selectionModel.selectedItem?.id ?: 0
                                        )
                                    )
                                    refreshStatus()
                                }
                            })
                            children.add(Button("Delete").apply {
                                graphic = imageView(itemResources, "BUTTON.DELETE_ITEM.ICON")
                                userData = item.id
                                maxWidth = Double.MAX_VALUE
                                onAction = EventHandler {
                                    val dialogResult = Alert(
                                        Alert.AlertType.CONFIRMATION,
                                        "Do you really want to delete this location?",
                                        ButtonType.NO,
                                        ButtonType.YES
                                    ).showAndWait()
                                    if (dialogResult.isPresent && dialogResult.get() == ButtonType.YES) {
                                        deleteItem((it.source as Button).userData as Int)
                                        onSelectCategory()
                                    }
                                }
                            })
                            maxWidth = Double.MAX_VALUE
                            alignment = Pos.CENTER
                            spacing = 5.0
                        }
                )
            }
            tableItems.items.addAll(itemRows)
            refreshStatus()
        }
    }

    private fun refreshStatus() {
        val numberOfLocations = tableItems.items.count()
        val numberOfVisitedLocations = tableItems.items.count { it.visited.isSelected }
        labelProgress.text = "$numberOfVisitedLocations/$numberOfLocations"
        progressBarItems.progress = numberOfVisitedLocations.toDouble() / numberOfLocations
    }

    private fun disableCategory() {
        buttonNewCategory.isDisable = false
        buttonRenameCategory.isDisable = true
        buttonDeleteCategory.isDisable = true
        comboCategory.items.clear()
        disableItem()
    }

    private fun enableCategory() {
        buttonNewCategory.isDisable = false
        buttonRenameCategory.isDisable = false
        buttonDeleteCategory.isDisable = false
        enableItem()
    }

    @FXML fun onInsertItem() {
        val result = InsertLocationDialog(stage, comboCategory.selectionModel.selectedItem, itemManager).showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            onSelectCategory()
        }
    }

    @FXML fun onImportItem() {
        FileChooser().apply {
            initialDirectory = dataDir()
            extensionFilters.addAll(FileChooser.ExtensionFilter("PSV file", "*.psv"))
        }.showOpenDialog(stage.owner)?.let {
            ImportLocationsDialog(stage, comboCategory.selectionModel.selectedItem, itemManager, it).showAndWait()
            onSelectCategory()
        }
    }

    @FXML fun onRandomItem() {
        val notVisitedList = tableItems.items.filter { locationRow -> !locationRow.visited.isSelected }.toList()
        val rnd = Random.nextInt(notVisitedList.size)
        val itemRow = notVisitedList[rnd]
        tableItems.selectionModel.select(itemRow)
        tableItems.scrollTo(itemRow)
    }

    @FXML fun onResetItem() {
        val alert = Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to reset all locations?",
                ButtonType.NO, ButtonType.YES
        ).showAndWait()
        if (alert.isPresent && alert.get() == ButtonType.YES) {
            resetAllItem(comboCategory.selectionModel.selectedItem)
            onSelectCategory()
        }
    }

    @FXML fun onKeyReleased() {
        foundLocationIdx = -1
        enableSearchButtonsWhenTextNotEmpty()
        buttonSearchItemPrev.isDisable = true
    }

    @FXML fun onSearchLocation(event: Event) {
        val textToSearch = textFieldSearchItem.text
        val foundLocations = tableItems.items.filter { locationRow ->
            (locationRow.name.text.indexOf(textToSearch, ignoreCase = true) >= 0 ||
            locationRow.description.text.indexOf(textToSearch, ignoreCase = true) >= 0)
        }.toList()
        if (event.source == buttonSearchItemNext && foundLocationIdx < foundLocations.lastIndex) {
            foundLocationIdx++
        } else if (event.source == buttonSearchItemPrev && foundLocationIdx > 0) {
            foundLocationIdx--
        }
        if (foundLocationIdx >= 0) {
            tableItems.selectionModel.select(foundLocations[foundLocationIdx])
            tableItems.scrollTo(foundLocations[foundLocationIdx])
            buttonSearchItemPrev.isDisable = false
        }
    }

    val projectManager = ProjectManager()
    val categoryManager = CategoryManager()
    val itemManager = ItemManager()
    var foundLocationIdx = -1

    internal fun dataDir() = projectManager.dataRoot.toAbsolutePath().toFile()

    internal fun updateConnection(game: String) {
        val connection = projectManager.openConnection(game)
        categoryManager.connection = connection
        itemManager.connection = connection
    }

    internal fun listProjects() = projectManager.findProjects()
    internal fun cloneProject(project: String) = projectManager.cloneDatabase(project)
    internal fun deleteProject(project: String) = projectManager.deleteDatabase(project)

    internal fun deleteCategory(category: Category) = categoryManager.delete(category)
    internal fun listCategories() = categoryManager.findAll()

    internal fun deleteItem(id: Int) = itemManager.delete(id)
    internal fun selectItemByCategory(category: Category) : List<Item> = itemManager.findByCategory(category)
    internal fun updateItem(item: Item) = itemManager.update(item)
    internal fun resetAllItem(category: Category) = itemManager.resetAll(category)
}
