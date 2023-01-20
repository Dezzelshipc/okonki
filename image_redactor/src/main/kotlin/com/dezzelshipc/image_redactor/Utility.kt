package com.dezzelshipc.image_redactor

import javafx.scene.image.Image
import javafx.scene.image.WritablePixelFormat
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.IntBuffer


class Utility {
    companion object {
        fun matToImage(mat: Mat): Image {
            val buffer = MatOfByte()
            Imgcodecs.imencode(".png", mat, buffer)

            return Image(ByteArrayInputStream(buffer.toArray()))
        }

        fun imageToMat(image: Image): Mat? {
            val width = image.width.toInt()
            val height = image.height.toInt()
            val buffer = ByteArray(width * height * 4)
            val reader = image.pixelReader
            val format: WritablePixelFormat<ByteBuffer> = WritablePixelFormat.getByteBgraInstance()
            reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4)
            val mat = Mat(height, width, CvType.CV_8UC4)
            mat.put(0, 0, buffer)
            return mat
        }

        fun imageToBufferedImage(image: Image): BufferedImage? {
            val width = image.width.toInt()
            val height = image.height.toInt()
            val pixels = IntArray(width * height)

            // Load the image's data into an array
            // You need to MAKE SURE the image's pixel format is compatible with IntBuffer

            // Load the image's data into an array
            // You need to MAKE SURE the image's pixel format is compatible with IntBuffer
            image.pixelReader.getPixels(
                0, 0, width, height,
                image.pixelReader.pixelFormat as WritablePixelFormat<IntBuffer?>,
                pixels, 0, width
            )

            val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    // There may be better ways to do this
                    // You'll need to make sure your image's format is correct here
                    val pixel = pixels[y * width + x]
                    val r = pixel and 0xFF0000 shr 16
                    val g = pixel and 0xFF00 shr 8
                    val b = pixel and 0xFF shr 0
                    bufferedImage.raster.setPixel(x, y, intArrayOf(r, g, b))
                }
            }
            return bufferedImage
        }
    }


}