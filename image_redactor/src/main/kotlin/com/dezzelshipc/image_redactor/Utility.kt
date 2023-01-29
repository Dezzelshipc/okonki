package com.dezzelshipc.image_redactor

import javafx.scene.image.Image
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.io.ByteArrayInputStream

enum class NodeTypes {
    INT, FLOAT, STRING, IMAGE, NONE
}

class Colors {
    companion object {
        const val BLUE = "#1e90ff"
        const val RED = "#ff0000"
        const val BLACK = "#000000"
    }
}

class Link {
    companion object {
        const val FIRST = "firstLink"
        const val SECOND = "secondLink"
        const val THIRD = "thirdLink"
        const val FOURTH = "fourthLink"
        const val FIFTH = "fifthLink"
    }
}

class Utility {
    companion object {
        fun matToImage(mat: Mat): Image {
            val buffer = MatOfByte()
            Imgcodecs.imencode(".png", mat, buffer)

            return Image(ByteArrayInputStream(buffer.toArray()))
        }
    }
}