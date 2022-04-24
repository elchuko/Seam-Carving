package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import kotlin.math.*

var maxEnergy = 0.0

fun main(args: Array<String>) {
    val IN = "-in"
    val OUT = "-out"
    val WIDTH = "-width"
    val HEIGHT = "-height"


    val map = processArguments(args)
    val inputFileName = map.get(IN)
    val outputFileName = map.get(OUT)
    val width = map.get(WIDTH)?.toInt()
    val height = map.get(HEIGHT)?.toInt()
    val inputFile = File(inputFileName)

    var image = ImageIO.read(inputFile)
    image = removeSeams(width, image)
    image = rotateImage(image)
    image = removeSeams(height, image)
    image = rotateImage(image)
    val file = File(outputFileName)


    ImageIO.write(image, "png", file)
}

fun removeSeams(numberOfSeams: Int?, ogImage: BufferedImage): BufferedImage {
    var image = ogImage
    if (numberOfSeams != null) {
        for (times in 0 until numberOfSeams) {
            image = seams(image)
        }
    }
    return image
}

fun seams(image: BufferedImage): BufferedImage {
    val energyMatrix = calcEnergy(image)
    val seam = setSeam(image, energyMatrix)
    return removeSeams(image, seam)
}

fun removeSeams(image: BufferedImage, seam: IntArray): BufferedImage {
    val smallerImage = BufferedImage(image.width - 1, image.height, BufferedImage.TYPE_INT_RGB)

    for (y in 0 until image.height - 1) {
        var xx = 0
        for (x in 0 until image.width) {
            if (seam[y + 1] == x) continue
            smallerImage.setRGB(xx, y, image.getRGB(x, y))
            xx++
        }
    }
    return smallerImage
}

fun rotateImage(image: BufferedImage): BufferedImage {
    var rotatedImage = BufferedImage(image.height, image.width, BufferedImage.TYPE_INT_RGB)

    for(y in 0 until image.height) {
        for (x in 0 until image.width) {
            rotatedImage.setRGB(y, x, image.getRGB(x, y))
        }
    }
    return rotatedImage
}

fun setSeam(image: BufferedImage, energyMatrix: Array<Array<Double>>): IntArray {
    var sumEnergyMatrix = sumEnergy(image, energyMatrix)

    var x = findMinSumEnergy(image, sumEnergyMatrix)
    val seam = IntArray(image.height)

    for (y in image.height - 1 downTo 1) {
        image.setRGB(x, y, Color.red.rgb)
        val left = (x - 1).coerceIn(0, image.width - 1)
        val right = (x + 1).coerceIn(0, image.width - 1)
        val min = minOf(sumEnergyMatrix[y - 1][left], sumEnergyMatrix[y - 1][x], sumEnergyMatrix[y - 1][right])
        x = if (sumEnergyMatrix[y - 1][left] == min) left else if (sumEnergyMatrix[y - 1][right] == min) right else x
        seam[y] = x
    }

    return seam
}

private fun sumEnergy(image: BufferedImage, energyMatrix: Array<Array<Double>>): Array<Array<Double>> {
    val sumEnergyMatrix = Array(image.height) { Array(image.width) {0.0} }

    for (x in 0 until image.width) {
        sumEnergyMatrix[0][x] = energyMatrix[0][x]
    }

    for (y in 1 until image.height) {
        for (x in 0 until image.width) {
            val left = (x - 1).coerceIn(0, image.width - 1)
            val right = (x + 1).coerceIn(0, image.width - 1)
            val min = minOf(sumEnergyMatrix[y - 1][left], sumEnergyMatrix[y - 1][x], sumEnergyMatrix[y - 1][right])
            sumEnergyMatrix[y][x] = energyMatrix[y][x] + min
        }
    }
    return sumEnergyMatrix
}

private fun findMinSumEnergy(image: BufferedImage, sumEnergyMatrix: Array<Array<Double>>): Int {
    var min = Double.MAX_VALUE
    var xx = 0
    for (x in 0 until image.width) {
        if (sumEnergyMatrix[image.height - 1][x] < min) {
            min = sumEnergyMatrix[image.height - 1][x]
            xx = x
        }
    }
    return xx
}

fun processArguments(args: Array<String>): HashMap<String, String> {
    val argMap: HashMap<String, String> = hashMapOf()
    for (i in 0 until args.size step 2) {
        argMap.put(args[i], args[i + 1])
    }
    return argMap
}

fun calcEnergy(image: BufferedImage): Array<Array<Double>> {
    var xx: Int
    var yy: Int
    val energyMatrix = Array(image.height) { Array<Double>(image.width) { 0.0 } }
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            xx = if (x == 0) 1 else if (x == image.width - 1) x - 1 else x
            val colorXPrev = Color(image.getRGB(xx - 1, y))
            val colorXNext = Color(image.getRGB(xx + 1, y))
            val deltaX = (colorXPrev.red - colorXNext.red).toDouble().pow(2) +
                    (colorXPrev.green - colorXNext.green).toDouble().pow(2) +
                    (colorXPrev.blue - colorXNext.blue).toDouble().pow(2)

            yy = if (y == 0) 1 else if (y == image.height - 1) y - 1 else y
            val colorYPrev = Color(image.getRGB(x, yy - 1))
            val colorYNext = Color(image.getRGB(x, yy + 1))
            val deltaY = (colorYPrev.red - colorYNext.red).toDouble().pow(2) +
                    (colorYPrev.green - colorYNext.green).toDouble().pow(2) +
                    (colorYPrev.blue - colorYNext.blue).toDouble().pow(2)
            energyMatrix[y][x] = sqrt(deltaX + deltaY)
        }
    }
    return energyMatrix
}

fun NormalizeIntensity(intensity:Double): Int {
    return (255.0 * intensity / maxEnergy).toInt()
}