package com.dezzelshipc.image_redactor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.FileChooser
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException

import javax.imageio.ImageIO





class ImgNode : ImageNode() {
    override val nodeType: NodeTypes = NodeTypes.IMAGE

    @FXML
    var openButton: Button? = null

    private var imageMat: Mat? = null

    override fun getValue(): Mat? {
        return imageMat
    }

    override fun addInit() {
        openButton!!.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png"))
            fileChooser.title = "Open Image File"
            val file = fileChooser.showOpenDialog(scene.window)
            if (file != null) {
                imageMat = Imgcodecs.imread(file.absolutePath)
                setImageView()
                imageView!!.isVisible = true
                outputLink?.kickAction()
            }

        }
    }

    init {
        init("ImgNode.fxml")
    }
}

class EndNode : ImageNode() {
    @FXML
    var saveButton: Button? = null

    override fun getValue(): Mat? {
        return nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
    }

    override fun addInit() {
        rootPane!!.onDragDetected = null

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"

        saveButton!!.onAction = EventHandler {
            val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return@EventHandler

            val fileChooser = FileChooser()
            fileChooser.title = "Save Picture"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Image Files", "*.png"))
            val dir = fileChooser.showSaveDialog(scene.window)
            if (dir != null) {
                try {
                    Imgcodecs.imwrite(dir.absolutePath, mat)
                } catch (e: IOException) {
                    println(e)
                }
            }
        }
    }

    init {
        init("EndNode.fxml")
    }
}