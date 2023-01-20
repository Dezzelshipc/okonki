package com.dezzelshipc.image_redactor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import java.io.IOException
import java.util.*


lateinit var nodeDragStart: DraggableNode
lateinit var anchorDragStart: AnchorPane

var stateAddLink = DataFormat("linkAdd")
var stateAddNode = DataFormat("nodeAdd")

abstract class DraggableNode : AnchorPane() {
    @FXML
    var rootPane: AnchorPane? = null

    @FXML
    var outputLinkHandle: AnchorPane? = null

    @FXML
    var titleBar: Label? = null

    @FXML
    var deleteButton: Button? = null

    lateinit var contextDragOver: EventHandler<DragEvent>
    lateinit var contextDragDropped: EventHandler<DragEvent>

    lateinit var linkDragDetected: EventHandler<MouseEvent>
    lateinit var linkDragDropped: EventHandler<DragEvent>
    lateinit var contextLinkDragOver: EventHandler<DragEvent>
    lateinit var contextLinkDagDropped: EventHandler<DragEvent>

    private var myLink = NodeLink()
    private var offset = Point2D(0.0, 0.0)

    var connectedLinks = mutableListOf<NodeLink>()
    var outputLink: NodeLink? = null

    var nodes = mutableMapOf<String, Triple<AnchorPane, DraggableNode?, NodeTypes>>()
    open val nodeType: NodeTypes = NodeTypes.NONE

    var superParent: AnchorPane? = null

    @FXML
    private fun initialize() {
        nodeHandlers()
        linkHandlers()

        initHandles()
        addInit()

        myLink.isVisible = false

        deleteButton!!.onAction = EventHandler {
            superParent!!.children.remove(this)
            superParent!!.children.removeIf { ch -> connectedLinks.count { it.id == ch.id } > 0 }
        }

        nodes.forEach { it.value.first.onDragDropped = linkDragDropped }

        parentProperty().addListener { _, _, _ -> if (parent != null) superParent = parent as AnchorPane }
    }

    open fun initHandles() {
        if (this.outputLinkHandle != null)
            this.outputLinkHandle!!.onDragDetected = linkDragDetected
    }

    open fun addInit() {}

    fun updatePoint(p: Point2D) {
        val local = parent.sceneToLocal(p)
        relocate(
            (local.x - offset.x),
            (local.y - offset.y)
        )
    }

    fun nodeHandlers() {

        contextDragOver = EventHandler { event ->
            updatePoint(Point2D(event.sceneX, event.sceneY))
            event.consume()
        }

        contextDragDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null
            event.isDropCompleted = true
            event.consume()
        }


        rootPane!!.onDragDetected = EventHandler { event ->
            parent.onDragOver = contextDragOver
            parent.onDragDropped = contextDragDropped

            offset = Point2D(event.x, event.y)
            updatePoint(Point2D(event.sceneX, event.sceneY))

            val content = ClipboardContent()
            content[stateAddNode] = "node"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()
        }
    }

    fun linkHandlers() {

        linkDragDetected = EventHandler { event ->
//            println("link started")

            if (outputLink != null) {
                superParent!!.children.remove(outputLink)
            }

            parent.onDragOver = null
            parent.onDragDropped = null

            parent.onDragOver = contextLinkDragOver
            parent.onDragDropped = contextLinkDagDropped

            superParent!!.children.add(0, myLink)
            myLink.isVisible = true

            val src = event.source as AnchorPane

            val p = Point2D(layoutX + src.layoutX + src.width / 2, layoutY + src.layoutY + src.height / 2)
            myLink.setStart(p)

            nodeDragStart = this
            anchorDragStart = event.source as AnchorPane

            val content = ClipboardContent()
            content[stateAddLink] = "link"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()
        }

        linkDragDropped = EventHandler { event ->
//            println("link connect")

            val src = event.source as AnchorPane
            if (nodeDragStart == this || !nodes.containsKey(src.id) || nodeDragStart.nodeType != nodes[src.id]!!.third || nodes[src.id]!!.second != null)
                return@EventHandler

            parent.onDragOver = null
            parent.onDragDropped = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            val link = NodeLink()

            superParent!!.children.add(0, link)
            nodeDragStart.outputLink = link
            nodeDragStart.outputLinkHandle = src

            nodes[src.id] = nodes[src.id]!!.copy(second = nodeDragStart)
            link.bindStartEnd(nodeDragStart, this, anchorDragStart, event.source as AnchorPane)

//            val content = ClipboardContent()
//            content[stateAddLink] = "link"
//            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.isDropCompleted = true
            event.consume()
        }


        contextLinkDragOver = EventHandler { event ->
            event.acceptTransferModes(*TransferMode.ANY)
            if (!myLink.isVisible) myLink.isVisible = true
            myLink.setEnd(Point2D(event.x, event.y))

            event.consume()
        }

        contextLinkDagDropped = EventHandler { event ->
//            println("link dropped")
            parent.onDragDropped = null
            parent.onDragOver = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            event.isDropCompleted = true
            event.consume()
        }
    }

    fun init(str: String) {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource(str)
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        try {
            fxmlLoader.load<Any>()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
        id = UUID.randomUUID().toString()
    }

    abstract fun getValue(): Any?

    abstract fun setImageView()
}