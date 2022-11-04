package com.dezzelshipc.toast

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.text.TextAlignment


enum class ImageStyle {
    CIRCLE, RECTANGLE
}

enum class Animation {
    FADE, MOVE
}

enum class CornerSide {
    LEFT, RIGHT, UP, DOWN,
    LEFT_UP(LEFT, UP), RIGHT_UP(RIGHT, UP), LEFT_DOWN(LEFT, DOWN), RIGHT_DOWN(RIGHT, DOWN);

    var horizontal: CornerSide? = null
    var vertical: CornerSide? = null

    constructor()
    constructor(horizontal: CornerSide, vertical: CornerSide) {
        this.horizontal = horizontal
        this.vertical = vertical
    }
}

class Config {
    var width = 300.0
    var height = 1000.0
    var windowPadding = 10.0

    var alpha = 0.9
    var openTime = 7000.0
    var imageType = ImageStyle.CIRCLE
    var cornerSide = CornerSide.RIGHT_DOWN
    var animation = Animation.FADE
    var musicVolume = 1.0
    var textPadding = 10.0

    var appName = "APP NAME"
    var title = "TITLE"
    var message = "MESSAGE"
    var image = "https://avatars.mds.yandex.net/i?id=ee0a8cd0c69a411b7fee131fde2b4980-3732926-images-thumbs&n=13"
    var music = "https://zvukipro.com/uploads/files/2022-02/1643694985_6ad935de2f5e282.mp3"
    var buttonText = arrayListOf("This is a cool button that close toast", "")

    var titleStyle = "-fx-font-size: 30;"
    var messageStyle = "-fx-font-family: 'Times New Roman'"
    var appNameStyle = ""


}

class Toast {
    private var config = Config()
    private val windows = Stage()
    private var root = BorderPane()
    private var box = HBox()


    class Builder {
        private var config = Config()

        fun setAppName(text: String, style: String = config.appNameStyle): Builder {
            config.appName = text
            config.appNameStyle = style
            return this
        }
        fun setTitle(text: String, style: String = config.titleStyle): Builder {
            config.title = text
            config.titleStyle = style
            return this
        }
        fun setMessage(text: String, style: String = config.messageStyle): Builder {
            config.message = text
            config.messageStyle = style
            return this
        }
        fun setImageType(style: ImageStyle): Builder {
            config.imageType = style
            return this
        }
        fun setMusicLink(link: String): Builder {
            config.music = link
            return this
        }
        fun setMusicVolume(percent: Double): Builder {
            config.musicVolume = percent
            return this
        }
        fun setCornerSide(side: CornerSide): Builder {
            config.cornerSide = side
            return this
        }
        fun setAnimation(anim : Animation): Builder {
            config.animation = anim
            return this
        }
        fun setButtonText(buttonNum: Int, str: String): Builder {
            config.buttonText[buttonNum] = str
            return this
        }
        fun setWindowPadding(padding: Double): Builder {
            config.windowPadding = padding
            return this
        }
        fun setContentPadding(padding: Double): Builder {
            config.textPadding = padding
            return this
        }
        fun build(): Toast  {
            val toast = Toast()
            toast.config = config
            toast.build()

            return toast
        }
    }


    private fun build() {
        setImage()

        val vbox = VBox()

        val title = Label(config.title)
        val message = Label(config.message)
        val appName = Label(config.appName)

        title.style = config.titleStyle
        message.style = config.messageStyle
        appName.style = config.appNameStyle

        title.isWrapText = true
        message.isWrapText = true
        appName.isWrapText = true

        title.applyCss()
        message.applyCss()
        appName.applyCss()

        vbox.children.addAll(title, message, appName)

        if (config.buttonText[0].isNotEmpty()) {
            val button = Button(config.buttonText[0])
            button.onAction = EventHandler { closeAnimation() }
            button.isWrapText = true
            vbox.children.add(button)
        }

        if (config.buttonText[1].isNotEmpty()) {
            val button = Button(config.buttonText[1])
            button.onAction = EventHandler {  }
            button.isWrapText = true
            vbox.children.add(button)
        }

        vbox.padding = Insets(0.0,0.0,0.0,10.0)
        vbox.autosize()

        box.children.add(vbox)
        box.padding = Insets(config.textPadding)

        root.center = box

        windows.initStyle(StageStyle.TRANSPARENT)

        //config.height = box.height

        root.maxWidth = config.width
        root.minWidth = config.width

        windows.scene = Scene(root, config.width, 1000.0)
        windows.scene.fill = Color.TRANSPARENT

    }

    private fun setImage() {
        if (config.image.isEmpty())
            return

        val iconBorder = if (config.imageType == ImageStyle.RECTANGLE) {
            Rectangle(100.0, 100.0)
        }
        else {
            Circle(50.0, 50.0, 50.0)
        }
        iconBorder.fill = ImagePattern(Image(config.image))
        box.children.add(iconBorder)
    }

    private fun setCorner() {
        val resX = Screen.getPrimary().bounds.maxX
        val resY = Screen.getPrimary().bounds.maxY

        if (config.cornerSide.horizontal == CornerSide.LEFT) {
            windows.x = config.windowPadding
        }
        else {
            windows.x = resX - config.width - config.windowPadding
        }

        if (config.cornerSide.vertical == CornerSide.UP) {
            windows.y = config.windowPadding
        } else {
            windows.y = resY - config.height - config.windowPadding
        }

        when (config.animation) {
            Animation.MOVE -> {
                if (config.cornerSide.horizontal == CornerSide.LEFT) {
                    windows.x -= 2*config.windowPadding + config.width
                } else {
                    windows.x += 2*config.windowPadding + config.width
                }
            }
        }
    }

    private fun openAnimation() {
        val music = MediaPlayer(Media(config.music))
        music.volume = config.musicVolume;
        music.play()

        when(config.animation) {
            Animation.FADE -> {
                val anim = FadeTransition(Duration.millis(1500.0), root)
                anim.fromValue = 0.0
                anim.toValue = config.alpha
                anim.cycleCount = 1
                anim.play()
            }
            Animation.MOVE -> {
                val currentX = windows.x
                val x = SimpleDoubleProperty(currentX)
                x.addListener { obs: ObservableValue<out Number>?, oldX: Number?, newX: Number ->
                    windows.x = newX.toDouble()
                }
                val keyFrame = KeyFrame(Duration.millis(1000.0),
                    KeyValue(x, currentX + (2*config.windowPadding + config.width)*(
                            if (config.cornerSide.horizontal == CornerSide.LEFT) 1 else -1), Interpolator.EASE_OUT))
                val animation = Timeline(keyFrame)
                animation.play()
            }
        }
    }

    private var isClosing = false
    private fun closeAnimation() {
        if (isClosing)
            return
        isClosing = true

        when(config.animation) {
            Animation.FADE -> {
                val anim = FadeTransition(Duration.millis(1500.0), root)
                anim.fromValue = config.alpha
                anim.toValue = 0.0
                anim.cycleCount = 1
                anim.onFinished = EventHandler {
                    Platform.exit()
                    System.exit(0)
                }
                anim.play()
            }
            Animation.MOVE -> {
                val currentX = windows.x
                val x = SimpleDoubleProperty(currentX)
                x.addListener { obs: ObservableValue<out Number>?, oldX: Number?, newX: Number ->
                    windows.x = newX.toDouble()
                }
                val keyFrame = KeyFrame(Duration.millis(1000.0),
                    KeyValue(x, currentX + (2*config.windowPadding + config.width)*(
                            if (config.cornerSide.horizontal == CornerSide.LEFT) -1 else 1), Interpolator.EASE_IN))
                val animation = Timeline(keyFrame)
                animation.onFinished = EventHandler {
                    Platform.exit()
                    System.exit(0)
                }
                animation.play()
            }
        }
    }

    fun start() {
        windows.show()

        windows.scene.root.autosize()
        config.height = root.height
        windows.height = config.height
        setCorner()

        openAnimation()
        val thread = Thread {
            try {
                Thread.sleep(config.openTime.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            closeAnimation()
        }
        Thread(thread).start()
    }

}


class SomeClass: Application() {
    override fun start(p0: Stage?) {
        val toast = Toast.Builder()
            .setTitle("T.N.T")
            .setMessage("kasj fhkjashdflkjsad")
            .setAppName("acdc")
            .setCornerSide(CornerSide.RIGHT_DOWN)
            .setAnimation(Animation.MOVE)
            .setButtonText(1, "")
            .setMusicVolume(0.0)
            .build()
        toast.start()

    }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(SomeClass::class.java)
        }
    }
}