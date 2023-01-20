package com.dezzelshipc.image_redactor

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt


abstract class ValueNode : DraggableNode() {
    @FXML
    var valueField: TextField? = null

    init {
        init("ValueNode.fxml")
    }
}

class IntNode : ValueNode() {
    override val nodeType: NodeTypes = NodeTypes.INT

    override fun addInit() {
        valueField!!.text = "0"
        titleBar!!.text = "Int"

        valueField!!.textProperty().addListener { _, _, _ ->
            outputLink?.kickAction()
        }
    }

    override fun getValue(): Int {
        return valueField!!.text.toIntOrNull() ?: return 0
    }

    override fun setImageView() {}
}

class FloatNode : ValueNode() {

    override val nodeType: NodeTypes = NodeTypes.FLOAT

    override fun addInit() {
        valueField!!.text = "0.0"
        titleBar!!.text = "Float"

        valueField!!.textProperty().addListener { _, _, _ ->
            outputLink?.kickAction()
        }
    }

    override fun getValue(): Float {
        return valueField!!.text.toFloatOrNull() ?: return 0.0f
    }

    override fun setImageView() {}
}

class StringNode : ValueNode() {
    override val nodeType: NodeTypes = NodeTypes.STRING
    override fun addInit() {
        valueField!!.text = ""
        titleBar!!.text = "String"

        valueField!!.textProperty().addListener { _, _, _ ->
            outputLink?.kickAction()
        }
    }

    override fun getValue(): String {
        return valueField!!.text
    }

    override fun setImageView() {}
}

abstract class ImageNode : DraggableNode() {
    override val nodeType: NodeTypes = NodeTypes.IMAGE

    @FXML
    var firstLink: AnchorPane? = null

    @FXML
    var imageView: ImageView? = null
    override fun setImageView() {
        val v = getValue() as Mat?
        if (v != null) {
            imageView!!.isVisible = true
            imageView!!.image = Utility.matToImage(v)
        }
    }
}

class SepiaNode : ImageNode() {

    override fun addInit() {
        titleBar!!.text = "Sepia"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null

        val colMat = Mat(3, 3, CvType.CV_64FC1)
        val row = 0
        val col = 0
        colMat.put(
            row, col, 0.272, 0.534, 0.131, 0.349, 0.686, 0.168, 0.393, 0.769, 0.189
        )

        val mat2 = Mat()
        mat.copyTo(mat2)
        Core.transform(mat, mat2, colMat)

        return mat2
    }

    init {
        init("OneNodeIV.fxml")
    }

}

class InvertNode : ImageNode() {
    override fun addInit() {
        titleBar!!.text = "Invert"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)
        Core.bitwise_not(mat, mat2)

        return mat2
    }

    init {
        init("OneNodeIV.fxml")
    }

}

class GreyNode : ImageNode() {
    override fun addInit() {
        titleBar!!.text = "Grey"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGR2GRAY)

        val mat3 = Mat()

        Core.merge(List(3) { mat2 }, mat3)

        return mat3
    }

    init {
        init("OneNodeIV.fxml")
    }

}

class BrightnessNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Bright"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "float"
    }


    override fun getValue(): Mat? {
        fun saturate(`val`: Double): Byte {
            var iVal = `val`.roundToInt()
            iVal = if (iVal > 255) 255 else if (iVal < 0) 0 else iVal
            return iVal.toByte()
        }

        val image = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val beta = nodes["secondLink"]!!.second?.getValue() as Float? ?: return null
        val alpha = 1.0

        val newImage = Mat()
        image.copyTo(newImage)

        val imageData = ByteArray(((image.total() * image.channels()).toInt()))
        image.get(0, 0, imageData)
        val newImageData = ByteArray((newImage.total() * newImage.channels()).toInt())
        for (y in 0 until image.rows()) {
            for (x in 0 until image.cols()) {
                for (c in 0 until image.channels()) {
                    var pixelValue = imageData[(y * image.cols() + x) * image.channels() + c].toDouble()
                    pixelValue = if (pixelValue < 0) pixelValue + 256 else pixelValue
                    newImageData[(y * image.cols() + x) * image.channels() + c] = saturate(alpha * pixelValue + beta)
                }
            }
        }
        newImage.put(0, 0, newImageData)

        return newImage
    }

    init {
        init("TwoNodesIV.fxml")
    }

}

class GaussNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Gauss"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "int"
    }


    override fun getValue(): Mat? {
        val image = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        var kernelSize = nodes["secondLink"]!!.second?.getValue() as Int? ?: return null

        kernelSize = kernelSize * 2 + 1
        if (kernelSize <= 0 || kernelSize > 100)
            return null


        val newImage = Mat()
        image.copyTo(newImage)

        Imgproc.GaussianBlur(image, newImage, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)

        return newImage
    }

    init {
        init("TwoNodesIV.fxml")
    }

}

class TScalePixelNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "ScalePix"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.INT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "i_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "i_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val x = nodes["secondLink"]!!.second?.getValue() as Int? ?: return null
        val y = nodes["thirdLink"]!!.second?.getValue() as Int? ?: return null

        if (x <= 0 || y <= 0)
            return null

        val mat2 = Mat()
        mat.copyTo(mat2)
        Imgproc.resize(mat, mat2, Size(x.toDouble(), y.toDouble()))

        return mat2
    }

    init {
        init("ThreeNodesIV.fxml")
    }

}

class TScalePercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "Scale%"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "f_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val px = nodes["secondLink"]!!.second?.getValue() as Float? ?: return null
        val py = nodes["thirdLink"]!!.second?.getValue() as Float? ?: return null

        val x = mat.cols() * px / 100
        val y = mat.rows() * py / 100

        if (x <= 0 || y <= 0)
            return null

        val mat2 = Mat()
        mat.copyTo(mat2)
        Imgproc.resize(mat, mat2, Size(x.toDouble(), y.toDouble()))

        return mat2
    }

    init {
        init("ThreeNodesIV.fxml")
    }

}

class TMovePixelsNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "Move"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.INT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "i_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "i_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val x = nodes["secondLink"]!!.second?.getValue() as Int? ?: return null
        val y = nodes["thirdLink"]!!.second?.getValue() as Int? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)

        val moveMat = Mat(2, 3, CvType.CV_64FC1)
        val row = 0
        val col = 0
        moveMat.put(
            row, col, 1.0, 0.0, x.toDouble(), 0.0, 1.0, y.toDouble()
        )

        Imgproc.warpAffine(mat, mat2, moveMat, Size(mat.cols().toDouble(), mat.rows().toDouble()))

        return mat2
    }

    init {
        init("ThreeNodesIV.fxml")
    }

}

class TMovePercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null
    override fun addInit() {
        titleBar!!.text = "Move"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "f_y"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val px = nodes["secondLink"]!!.second?.getValue() as Float? ?: return null
        val py = nodes["thirdLink"]!!.second?.getValue() as Float? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)

        val moveMat = Mat(2, 3, CvType.CV_64FC1)
        val row = 0
        val col = 0
        moveMat.put(
            row, col, 1.0, 0.0, (mat.cols() * px / 100.0), 0.0, 1.0, (mat.rows() * py / 100.0)
        )

        Imgproc.warpAffine(mat, mat2, moveMat, Size(mat.cols().toDouble(), mat.rows().toDouble()))

        return mat2
    }

    init {
        init("ThreeNodesIV.fxml")
    }

}

class TRotateNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Rotate"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "f_deg"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val deg = nodes["secondLink"]!!.second?.getValue() as Float? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)

        val rotMat = Imgproc.getRotationMatrix2D(Point(mat.cols() / 2.0, mat.rows() / 2.0), deg.toDouble(), 1.0)

        Imgproc.warpAffine(mat, mat2, rotMat, Size(mat.cols().toDouble(), mat.rows().toDouble()))

        return mat2
    }

    init {
        init("TwoNodesIV.fxml")
    }
}

class AddTextPixelNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null

    @FXML
    var fourthLink: AnchorPane? = null

    @FXML
    var fifthLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "AddText"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.INT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.INT)
        nodes["fourthLink"] = Triple(fourthLink!!, null, NodeTypes.STRING)
        nodes["fifthLink"] = Triple(fifthLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "intx"
        (thirdLink!!.children.find { it is Label } as Label).text = "inty"
        (fourthLink!!.children.find { it is Label } as Label).text = "str"
        (fourthLink!!.children.find { it is Label } as Label).text = "float"
        (fifthLink!!.children.find { it is Label } as Label).text = "fl_scale"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val x = nodes["secondLink"]!!.second?.getValue() as Int? ?: return null
        val y = nodes["thirdLink"]!!.second?.getValue() as Int? ?: return null
        val str = nodes["fourthLink"]!!.second?.getValue() as String? ?: return null
        val scale = nodes["fifthLink"]!!.second?.getValue() as Float? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)

        Imgproc.putText(
            mat2,
            str,
            Point(x.toDouble(), y.toDouble()),
            0,
            scale.toDouble(),
            Scalar(255.0, 255.0, 255.0),
            scale.toInt()
        )

        return mat2
    }

    init {
        init("FiveNodesIV.fxml")
    }
}

class AddTextPercentNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null

    @FXML
    var fourthLink: AnchorPane? = null

    @FXML
    var fifthLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "AddText"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.FLOAT)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.FLOAT)
        nodes["fourthLink"] = Triple(fourthLink!!, null, NodeTypes.STRING)
        nodes["fifthLink"] = Triple(fifthLink!!, null, NodeTypes.FLOAT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "fl_x"
        (thirdLink!!.children.find { it is Label } as Label).text = "fl_y"
        (fourthLink!!.children.find { it is Label } as Label).text = "str"
        (fifthLink!!.children.find { it is Label } as Label).text = "fl_scale"
    }

    override fun getValue(): Mat? {
        val mat = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val px = nodes["secondLink"]!!.second?.getValue() as Float? ?: return null
        val py = nodes["thirdLink"]!!.second?.getValue() as Float? ?: return null
        val str = nodes["fourthLink"]!!.second?.getValue() as String? ?: return null
        val scale = nodes["fifthLink"]!!.second?.getValue() as Float? ?: return null

        val mat2 = Mat()
        mat.copyTo(mat2)

        Imgproc.putText(
            mat2,
            str,
            Point(mat.cols() * px / 100.0, mat.rows() * py / 100.0),
            0,
            scale.toDouble(),
            Scalar(255.0, 255.0, 255.0),
            2
        )

        return mat2
    }

    init {
        init("FiveNodesIV.fxml")
    }
}

class MergeNode : ImageNode() {
    @FXML
    var secondLink: AnchorPane? = null

    @FXML
    var thirdLink: AnchorPane? = null

    @FXML
    var fourthLink: AnchorPane? = null

    override fun addInit() {
        titleBar!!.text = "Merge"

        nodes["firstLink"] = Triple(firstLink!!, null, NodeTypes.IMAGE)
        nodes["secondLink"] = Triple(secondLink!!, null, NodeTypes.IMAGE)
        nodes["thirdLink"] = Triple(thirdLink!!, null, NodeTypes.INT)
        nodes["fourthLink"] = Triple(fourthLink!!, null, NodeTypes.INT)

        (firstLink!!.children.find { it is Label } as Label).text = "img"
        (secondLink!!.children.find { it is Label } as Label).text = "img2"
        (thirdLink!!.children.find { it is Label } as Label).text = "i_x"
        (fourthLink!!.children.find { it is Label } as Label).text = "i_y"
    }

    override fun getValue(): Mat? {
        val image = nodes["firstLink"]!!.second?.getValue() as Mat? ?: return null
        val image2 = nodes["secondLink"]!!.second?.getValue() as Mat? ?: return null
        val sx = nodes["thirdLink"]!!.second?.getValue() as Int? ?: return null
        val sy = nodes["fourthLink"]!!.second?.getValue() as Int? ?: return null

        val newImage = Mat()
        image.copyTo(newImage)

        val imageData = ByteArray(((image.total() * image.channels()).toInt()))
        image.get(0, 0, imageData)
        val imageData2 = ByteArray(((image.total() * image.channels()).toInt()))
        image2.get(0, 0, imageData2)

        for (y in 0 until image2.rows()) {
            for (x in 0 until image2.cols()) {
                for (c in 0 until image2.channels()) {
                    if (0 <= sy + y && sy + y < image.rows() && 0 <= sx + x && sx + x < image.cols()) {
                        imageData[((sy + y) * image.cols() + sx + x) * image.channels() + c] =
                            imageData2[(y * image2.cols() + x) * image2.channels() + c]
                    }
                }
            }
        }
        newImage.put(0, 0, imageData)

        return newImage
    }

    init {
        init("FourNodesIV.fxml")
    }
}