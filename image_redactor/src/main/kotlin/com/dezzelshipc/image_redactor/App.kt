package com.dezzelshipc.image_redactor

import com.google.gson.Gson
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.opencv.core.Core
import java.io.File
import java.io.IOException

class Window {
    private var root = AnchorPane()
    private val width = 1280.0
    private val height = 720.0
    private var scene = Scene(root, width, height)

    fun start(): Scene {
        root.children.add(createButtons())
        root.children.add(saveButtons(100.0, 0.0))

        val end = EndNode()
        end.layoutX = width - end.rootPane!!.prefWidth - 10
        end.layoutY = height / 2
        root.children.add(end)

        return scene
    }

    private fun createButtons(): VBox {
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
        createButton("scalePix")
        createButton("scale%")
        createButton("movePix")
        createButton("move%")
        createButton("rotate")
        createButton("addTextPix")
        createButton("addText%")
        createButton("merge")

        return vBox
    }

    private fun saveButtons(x: Double, y: Double): HBox {
        val hBox = HBox()
        hBox.layoutX = x
        hBox.layoutY = y

//        val btn = Button("nodes")
//        btn.onAction = EventHandler {
//            val nodes = root.children.filterIsInstance<DraggableNode>()
//            val listNodes = MutableList(nodes.size) { nodes[it].toData() }
//            val links = root.children.filterIsInstance<NodeLink>()
//            val listLinks = MutableList(links.size) { links[it].toData() }
//
//            println("$listNodes \n $listLinks")
//        }
//        hBox.children.add(btn)

        val btn1 = Button("save")
        btn1.onAction = EventHandler {
            val gson = Gson()
            val nodes = root.children.filterIsInstance<DraggableNode>()
            val listNodes = MutableList(nodes.size) { nodes[it].toData() }
            val links = root.children.filterIsInstance<NodeLink>()
            val listLinks = MutableList(links.size) { links[it].toData() }

            println(gson.toJson(Saved(listNodes, listLinks)))

            val fileChooser = FileChooser()
            fileChooser.title = "Save Nodes"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Node Files", "*.ns"))
            val dir = fileChooser.showSaveDialog(scene.window)
            if (dir != null) {
                try {
                    val file = File(dir.toURI())
                    file.writeText(gson.toJson(Saved(listNodes, listLinks)))
                } catch (e: IOException) {
                    println(e)
                }
            }
        }
        hBox.children.add(btn1)

        val btn2 = Button("load")
        btn2.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.title = "Open Nodes"
            fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Node Files", "*.ns"))
            val dir = fileChooser.showOpenDialog(scene.window)
            if (dir != null) {
                try {
                    val file = File(dir.toURI())
                    if (!file.exists()) return@EventHandler

                    val data = Gson().fromJson(file.readText(), Saved::class.java)
                    if (data.links == null || data.nodes == null) return@EventHandler

                    //println(data)

                    root.children.removeIf { it is DraggableNode || it is NodeLink }

                    data.nodes.forEach {
                        val node = getNode(it.name)
                        node.fromData(it)
                        root.children.add(node)
                    }

                    data.links.forEach {
                        //println(it)

                        val inNode = root.lookup("#${it.inputNode}") as DraggableNode
                        val outNode = root.lookup("#${it.outputNode}") as DraggableNode
                        val inAnchor = root.lookup("#${it.inputAnchor}") as AnchorPane
                        val outAnchor = root.lookup("#${it.outputAnchor}") as AnchorPane

                        inAnchor.layoutX = it.inputAnchorSize.first
                        inAnchor.layoutY = it.inputAnchorSize.second

                        outAnchor.layoutX = it.outputAnchorSize.first
                        outAnchor.layoutY = it.outputAnchorSize.second

                        inNode.linkNodes(outNode, inNode, outAnchor, inAnchor, it.inputAnchor!!).id = it.id
                    }

                } catch (e: IOException) {
                    println(e)
                }
            }
        }
        hBox.children.add(btn2)

        return hBox
    }

    private fun getNode(str: String): DraggableNode {
        return when (str) {
            "int", IntNode::class.simpleName -> IntNode()
            "float", FloatNode::class.simpleName -> FloatNode()
            "string", StringNode::class.simpleName -> StringNode()
            "image", ImgNode::class.simpleName -> ImgNode()
            "sepia", SepiaNode::class.simpleName -> SepiaNode()
            "grey", GreyNode::class.simpleName -> GreyNode()
            "invert", InvertNode::class.simpleName -> InvertNode()
            "bright", BrightnessNode::class.simpleName -> BrightnessNode()
            "gauss", GaussNode::class.simpleName -> GaussNode()
            "scalePix", TScalePixelNode::class.simpleName -> TScalePixelNode()
            "scale%", TScalePercentNode::class.simpleName -> TScalePercentNode()
            "movePix", TMovePixelsNode::class.simpleName -> TMovePixelsNode()
            "move%", TMovePercentNode::class.simpleName -> TMovePercentNode()
            "rotate", TRotateNode::class.simpleName -> TRotateNode()
            "addTextPix", AddTextPixelNode::class.simpleName -> AddTextPixelNode()
            "addText%", AddTextPercentNode::class.simpleName -> AddTextPercentNode()
            "merge", MergeNode::class.simpleName -> MergeNode()
            EndNode::class.simpleName -> EndNode()
            else -> IntNode()
        }
    }
}

data class Saved(val nodes: MutableList<NodeData>?, val links: MutableList<LinkData>?)


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