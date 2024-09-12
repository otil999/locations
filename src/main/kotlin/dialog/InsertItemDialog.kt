package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.Item
import hu.otilsoft.locations.NoButtonBarDialogPane
import hu.otilsoft.locations.imageView
import hu.otilsoft.locations.manager.ItemManager
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Window
import java.util.*


class InsertLocationDialog(owner: Window, private val category: Category, itemManager: ItemManager) : Dialog<ButtonType>() {
    init {
        val resources = ResourceBundle.getBundle("insert-item-dialog")
        val fxmlLoader = FXMLLoader(this::class.java.getResource("/insert-item-dialog.fxml"), resources)
        fxmlLoader.setController(this)

        initOwner(owner)
        initModality(Modality.APPLICATION_MODAL)

        dialogPane = NoButtonBarDialogPane()
        dialogPane.content = fxmlLoader.load()
        dialogPane.scene.window.setOnCloseRequest { _ -> run { onCloseRequest() } }

        title = resources.getString("TITLE")
        buttonSave.graphic = imageView(resources, "BUTTON.SAVE.ICON")

        println("CAT:${category.id}")
    }

    @FXML private lateinit var buttonSave: Button
    @FXML private lateinit var textName: TextField
    @FXML private lateinit var textDescription: TextField
    @FXML private lateinit var textType: TextField
    @FXML private lateinit var checkBoxPassed: CheckBox

    private val controller = AddItemController(itemManager)

    private fun onCloseRequest() {
        result = ButtonType.CANCEL
        close()
    }

    fun save() {
        controller.add(
            Item(0, textName.text, textDescription.text, checkBoxPassed.isSelected, textType.text, category.id)
        )
        result = ButtonType.OK
        close()
    }
}


class AddItemController(private val itemManager: ItemManager) {
    internal fun add(item: Item) = itemManager.add(item)
}
