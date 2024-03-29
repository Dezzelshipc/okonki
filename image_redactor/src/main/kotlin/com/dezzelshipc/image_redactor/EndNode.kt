package com.dezzelshipc.image_redactor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.IOException

class ImgNode : ImageNode() {
    override val nodeType: NodeTypes = NodeTypes.IMAGE

    @FXML
    var openButton: Button? = null

    private var imageMat: Mat? = null
    private var path: String? = null

    override fun getValue(): Mat? {
        return imageMat
    }

    fun getImage() {
        imageMat = Imgcodecs.imread(path)
        updateNode()
        imageView!!.isVisible = true
        outputLink?.kickAction()
    }

    override fun addInit() {
        openButton!!.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png"))
            fileChooser.title = "Open Image File"
            val file = fileChooser.showOpenDialog(scene.window)
            if (file != null) {
                path = file.absolutePath
                getImage()
            }
        }
    }

    override fun toData(): NodeData {
        val data = super.toData()
        data.data = path
        return data
    }

    override fun fromData(nodeData: NodeData) {
        super.fromData(nodeData)
        path = nodeData.data
        getImage()
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

    override fun updateNode() {
        goodNodes()
        val v = getValue()
        if (v != null) {
            imageView!!.isVisible = true
            imageView!!.image = Utility.matToImage(v)
            saveButton!!.textFill = Paint.valueOf(Colors.BLACK)
        } else {
            saveButton!!.textFill = Paint.valueOf(Colors.RED)
        }
    }

    init {
        init("EndNode.fxml")
    }
}