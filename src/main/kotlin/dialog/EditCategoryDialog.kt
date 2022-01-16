package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.manager.CategoryManager
import java.util.ResourceBundle
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import tornadofx.Controller
import tornadofx.View
import tornadofx.get
import tornadofx.imageview


class EditModDialog : View() {
    init {
        messages = ResourceBundle.getBundle("edit-category-dialog")
        title = messages["DIALOG.TITLE"]
    }

    private var saved = false
    private val updateMode: Boolean by param()
    private val category: Category by param()
    private val categoryManager: CategoryManager by param()
    private val controller = EditModController(categoryManager)

    override val root : VBox by fxml("/edit-category-dialog.fxml")
    private val buttonSave: Button by fxid("buttonSave")
    val textName: TextField by fxid("textName")

    override fun onBeforeShow() {
        super.onBeforeShow()
        buttonSave.imageview(Image("images/save-mini.png"))
        textName.text = category.name
    }

    fun save() {
        if (updateMode) {
            controller.rename(Category(category.id, textName.text))
        } else {
            controller.add(Category(name = textName.text))
        }
        saved = true
        close()
    }

    fun isCanceled() = !saved
}


internal class EditModController(private val categoryManager: CategoryManager) : Controller() {
    fun rename(category: Category) = categoryManager.rename(category)
    fun add(category: Category) = categoryManager.add(category)
}
