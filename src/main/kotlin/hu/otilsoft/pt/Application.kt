package hu.otilsoft.pt

import javafx.application.Application
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.ResourceBundle


fun imageView(resources: ResourceBundle, id: String): ImageView = ImageView(Image(resources.getString(id)))


fun useApi(client: HttpClient, request: HttpRequest, handler: (body: String) -> Unit) {
    client
        .sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply { response -> response.body() }
        .thenAccept { body -> Platform.runLater { handler(body) } }
}


fun main(args: Array<String>) {
    launch(ProgressTrackerApplication::class.java, *args)
}


class ProgressTrackerApplication : Application() {
    override fun start(primaryStage: Stage) {
        val fxmlLoader = FXMLLoader(
            this.javaClass.getResource("/fxml/main.fxml"),
            ResourceBundle.getBundle("main")
        ).apply { MainController(primaryStage) }

        primaryStage.scene = Scene(fxmlLoader.load())
        primaryStage.show()
    }
}


class MainController(private val stage: Stage) : Initializable {
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var comboCategory: ComboBox<String>

    @FXML
    private lateinit var textFieldSearchItem: TextField

    @FXML
    private lateinit var buttonNewProject: Button

    override fun initialize(location: URL, resources: ResourceBundle) {
        this.resources = resources
        buttonNewProject.graphic = imageView(resources, "BUTTON.NEW_PROJECT.ICON")
        textFieldSearchItem.disableProperty().bind(comboCategory.selectionModel.selectedItemProperty().isNull)
    }

    @FXML
    fun onNewProject() {}

    @FXML
    fun onEditProject() {}

    @FXML
    fun onCloneProject() {}

    @FXML
    fun onDeleteProject() {}

    @FXML
    fun onNewCategory() {}

    @FXML
    fun onEditCategory() {}

    @FXML
    fun onCloneCategory() {}

    @FXML
    fun onDeleteCategory() {}

    @FXML
    fun onInsertItem() {}

    @FXML
    fun onImportItem() {}

    @FXML
    fun onRandomItem() {}

    @FXML
    fun onResetItem() {}

    @FXML
    fun onSelectProject() {}

    @FXML
    fun onSelectCategory() {}

    @FXML
    fun onKeyReleased() {}

    @FXML
    fun onSearchLocation() {}
}
