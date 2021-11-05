package hu.otilsoft.locations

import java.util.ResourceBundle
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import tornadofx.View
import tornadofx.get
import tornadofx.imageview


class EditGameDialog : View() {
    init {
        messages = ResourceBundle.getBundle("edit-game-dialog")
        title = messages["DIALOG.TITLE"]
    }

    override val root : VBox by fxml("/edit-game-dialog.fxml")
    private val buttonSave: Button by fxid("buttonSave")
    private val textName: TextField by fxid("textName")

    private var saved = false
    private val updateMode: Boolean by param()
    private val databaseName: String by param()

    private val controller: MainController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
        saved = false
        buttonSave.imageview(Image("images/save-mini.png"))
        textName.text = databaseName
    }

    fun save() {
        if (updateMode) {
            controller.renameDatabase(databaseName, textName.text)
        } else {
            controller.createDatabase(textName.text)
        }
        saved = true
        close()
    }

    fun isCanceled() = !saved
}
