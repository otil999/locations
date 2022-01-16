package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.manager.GameManager
import java.util.ResourceBundle
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import tornadofx.Controller
import tornadofx.View
import tornadofx.get
import tornadofx.imageview


class EditGameDialog : View() {
    init {
        messages = ResourceBundle.getBundle("edit-game-dialog")
    }

    override val root : VBox by fxml("/edit-game-dialog.fxml")
    private val buttonSave: Button by fxid("buttonSave")
    val textName: TextField by fxid("textName")

    private var saved = false
    private val updateMode: Boolean by param()
    private val databaseName: String by param()
    private val gameManager: GameManager by param()

    private val controller = EditGameController(gameManager)

    override fun onBeforeShow() {
        super.onBeforeShow()
        title = if (updateMode)
            messages["DIALOG.TITLE_RENAME_GAME"]
        else
            messages["DIALOG.TITLE_NEW_GAME"]
        saved = false
        buttonSave.imageview(Image("images/save-mini.png"))
        textName.text = databaseName
    }

    fun save() {
        if (updateMode) {
            controller.rename(databaseName, textName.text)
        } else {
            controller.create(textName.text)
        }
        saved = true
        close()
    }

    fun isCanceled() = !saved
}


internal class EditGameController(private val gameManager: GameManager) : Controller() {
    fun rename(oldName: String, newName: String) = gameManager.renameDatabase(oldName, newName)
    fun create(name: String) = gameManager.createDatabase(name)
}
