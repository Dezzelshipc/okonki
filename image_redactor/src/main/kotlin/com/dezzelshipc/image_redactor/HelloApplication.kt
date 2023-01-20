package com.dezzelshipc.image_redactor

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.opencv.core.Core

class Window {
    private var root = AnchorPane()
    private val width = 1280.0
    private val height = 720.0

    fun start(): Scene {
        val vBox = VBox()
        fun createButton(str: String) {
            val button = Button(str)
            button.onAction = EventHandler {
                val node = getNode(str)
                node.layoutX += 100
                node.layoutY += 100
                root.children.add(node)
            }
            vBox.children.add(button)
        }

        createButton("int")
        createButton("float")
        createButton("string")
        createButton("image")
        createButton("sepia")
        createButton("grey")
        createButton("invert")
        createButton("bright")
        createButton("gauss")

        val end = EndNode()
        end.layoutX = width - end.rootPane!!.prefWidth - 10
        end.layoutY = height / 2
        root.children.add(end)

        root.children.add(vBox)

        return Scene(root, width, height);
    }

    private fun getNode(str: String): DraggableNode {
        return when (str) {
            "int" -> IntNode()
            "float" -> FloatNode()
            "string" -> StringNode()
            "image" -> ImgNode()
            "sepia" -> SepiaNode()
            "grey" -> GreyNode()
            "invert" -> InvertNode()
            "bright" -> BrightnessNode()
            "gauss" -> GaussNode()
            else -> IntNode()
        }
    }


}


class DnD2 : Application() {

    override fun start(primaryStage: Stage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        primaryStage.scene = Window().start()
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(DnD2::class.java)
        }
    }
}