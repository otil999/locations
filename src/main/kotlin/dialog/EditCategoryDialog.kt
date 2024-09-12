package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.NoButtonBarDialogPane
import hu.otilsoft.locations.imageView
import hu.otilsoft.locations.manager.CategoryManager
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Window
import java.util.*


class EditCategoryDialog(
    owner: Window,
    private val update: Boolean,
    private val category: Category,
    categoryManager: CategoryManager) : Dialog<ButtonType>()
{
    private val controller = EditCategoryController(categoryManager)
    private var savedCategory: Category = Category()

    @FXML private lateinit var buttonSave: Button
    @FXML private lateinit var textName: TextField

    init {
        val resources = ResourceBundle.getBundle("edit-category-dialog")
        val fxmlLoader = FXMLLoader(this::class.java.getResource("/edit-category-dialog.fxml"), resources)
        fxmlLoader.setController(this)

        initOwner(owner)
        initModality(Modality.APPLICATION_MODAL)

        dialogPane = NoButtonBarDialogPane()
        dialogPane.content = fxmlLoader.load()
        dialogPane.scene.window.setOnCloseRequest { _ -> run { onCloseRequest() } }

        title = resources.getString(if (update) { "TITLE.RENAME_CATEGORY" } else { "TITLE.NEW_CATEGORY" })
        textName.text = category.name
        buttonSave.graphic = imageView(resources, "BUTTON.SAVE.ICON")
    }

    private fun onCloseRequest() {
        result = ButtonType.CANCEL
        close()
    }

    @FXML
    fun onSave() {
        val categoryName = textName.text
        if (update) {
            savedCategory = controller.rename(category, categoryName)
        } else {
            savedCategory = controller.add(Category(name = categoryName))
        }
        result = ButtonType.OK
        close()
    }

    fun savedCategory() = savedCategory
}


internal class EditCategoryController(private val categoryManager: CategoryManager) {
    fun rename(category: Category, newName: String) = categoryManager.rename(category, newName)
    fun add(category: Category) = categoryManager.add(category)
}
