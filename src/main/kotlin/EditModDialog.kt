package hu.otilsoft.locations

import java.util.ResourceBundle
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import tornadofx.View
import tornadofx.get
import tornadofx.imageview


class EditModDialog : View() {
    init {
        messages = ResourceBundle.getBundle("edit-mod-dialog")
        title = messages["DIALOG.TITLE"]
    }

    private var saved = false
    private val updateMode: Boolean by param()
    private val mod: Mod by param()
    private val controller: MainController by inject()

    override val root : VBox by fxml("/edit-mod-dialog.fxml")
    private val buttonSave: Button by fxid("buttonSave")
    private val textName: TextField by fxid("textName")

    override fun onBeforeShow() {
        super.onBeforeShow()
        buttonSave.imageview(Image("images/save-mini.png"))
        textName.text = mod.name
    }

    fun save() {
        if (updateMode) {
            controller.renameMod(Mod(mod.id, textName.text))
        } else {
            controller.addMod(Mod(name = textName.text))
        }
        saved = true
        close()
    }

    fun isCanceled() = !saved
}
