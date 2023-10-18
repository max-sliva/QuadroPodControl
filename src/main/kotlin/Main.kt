import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.draw.EmptyBuildDrawCacheParams.size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.lang.Math.abs
import java.lang.Math.toDegrees
import kotlin.math.atan

@Composable
@Preview
fun App(quadroPodBody: ImageBitmap, rotatePoints: Array<Pair<Int, Int>>, arms: Array<ImageBitmap>) {
//    var text by remember { mutableStateOf("Hello, World!") }
    //массив с мапом: начальные точки, оффсеты и точки поворота в виде пар значений
//    todo вставить в него все оффсеты, нач.точки и точки поворота
    var arrayForGettingAngles = arrayOf<HashMap<String, Pair<Float, Float>>>()
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var degs by remember { mutableStateOf(0f) }
    var angleOnDragEnd by remember { mutableStateOf(0f) }
    var currentArm = 0
//    var katet1 by remember { mutableStateOf(0f) }
//    var katet2 by remember { mutableStateOf(0f) }
    var arm1RotatePointX by remember { mutableStateOf(0f) }
    var arm1RotatePointY by remember { mutableStateOf(0f) }
    var startPointX by remember { mutableStateOf(0f) }
    var startPointY by remember { mutableStateOf(0f) }
//    print(" angle at start = $degs")
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(), //заполняем всё доступное пространство
            horizontalAlignment = Alignment.CenterHorizontally, //по центру горизонтально
//            verticalArrangement = Arrangement.Center //и вертикально
        ) {
            Canvas(modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
//                    detectTapGestures(
//                        onTap = {
//                            println("x = ${it.x}  y = ${it.y}")
//                        }
//                    )
//                   if (degs<=65)
                       detectDragGestures(
                        onDragStart = { touch ->
//                            println("\nStart of the interaction is x=${touch.x} y=${touch.y}")
                            startPointX = touch.x
                            startPointY = touch.y
//                            degs-=angleOnDragEnd
//                            angleOnDragEnd = degs
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
//                            println("in listener x    = ${dragAmount.x}  y = ${dragAmount.y}  ")
//                            println("arm1RotatePointX = $arm1RotatePointX arm1RotatePointY = $arm1RotatePointY" )
                            //todo сделать определение зоны касания, и увеличивать соотвествующую offset из массива офсетов
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        },
                       onDragEnd = {
                           println("angle on drag end = $degs")
//                           angleOnDragEnd = degs
                       }
                    )
//                    detectDragGestures { change, dragAmount ->
//                        change.consume()
////                        println("in listener x = ${dragAmount.x} y = ${dragAmount.y}")
//                        offsetX += dragAmount.x
//                        offsetY += dragAmount.y
//                    }
                }
                //todo сделать обработку клика на картинку
//                .clickable (
//                    onClick = {
//                        println("Clicked")
//                    }
//                )
            ) {
//                val canvasQuadrantSize = size / 2F
                try {
                    drawImage(
                        image = quadroPodBody,
                        topLeft = Offset(0F, 0F)
                    )
                    val arm1 = arms[0]
                    degs = angle(arm1RotatePointX, arm1RotatePointY, startPointX, startPointY, offsetX, offsetY) - angleOnDragEnd
//                    todo разобраться с началом поворота, чтобы не дергалась картинка, когда не с начала берем лапу
                        //скорее всего, нужно угол после поворота запоминать
                    println(" angle = $degs ")
                    arm1RotatePointX = arm1.width.toFloat()
                    arm1RotatePointY = (arm1.height / 2).toFloat()
//                    rotate(degrees = -degs, Offset(arm1RotatePointX, arm1RotatePointY)){
                    //ограничиваем поворот
//                    if (degs<=65 || degs>=-90)
//                    if (degs<=65)
                    if (degs<=65 && degs>-85 && startPointX+offsetX < arm1RotatePointX)
                        rotate(degrees = -degs, Offset(rotatePoints[0].first.toFloat(), rotatePoints[0].second.toFloat())){
                        drawImage(
                            image = arm1,
                            topLeft = Offset(0F, 0F)
    //                            topLeft = Offset(offsetX, offsetY)
                        )

                    } else
//                        if (degs >=60)
                        if (startPointY+offsetY > arm1RotatePointY)
                        rotate(degrees = -65F, Offset(rotatePoints[0].first.toFloat(), rotatePoints[0].second.toFloat())) {
                            drawImage(
                                image = arm1,
                                topLeft = Offset(0F, 0F)
                                //                            topLeft = Offset(offsetX, offsetY)
                            )
                        }
                    else
                        if (startPointY+offsetY < arm1RotatePointY)
                        rotate(degrees = 85F, Offset(rotatePoints[0].first.toFloat(), rotatePoints[0].second.toFloat())) {
                            drawImage(
                                image = arm1,
                                topLeft = Offset(0F, 0F)
                                //                            topLeft = Offset(offsetX, offsetY)
                            )
                        }
//                    else if (degs<=-90)
//                        rotate(degrees = 90F, Offset(rotatePoints[0].first.toFloat(), rotatePoints[0].second.toFloat())) {
//                            drawImage(
//                                image = arm1,
//                                topLeft = Offset(0F, 0F)
//                                //                            topLeft = Offset(offsetX, offsetY)
//                            )
//                        }
//                    println("image size = ${quadroPodBody.width} x ${quadroPodBody.height}")
                        } catch (e: NullPointerException) {
//                    Toast.makeText(applicationContext,"No image", Toast.LENGTH_LONG).show()
                    println("No image")
                }
            }
        }
    }
}

fun angle(
    arm1RotatePointX: Float,
    arm1RotatePointY: Float,
    startPointX: Float,
    startPointY: Float,
    offsetX: Float,
    offsetY: Float
): Float {
    var degs: Float
    //вычисляем катеты для угла поворота
    val katet1 = arm1RotatePointX - (startPointX + offsetX)
    val katet2 = startPointY + offsetY - arm1RotatePointY
    val tan = katet2 / abs(katet1) //тангенс угла поворота
//                    print(" offsetY = $offsetY   offsetX = $offsetX")
//                    print(" katet2 = $katet2   katet1 = $katet1")
    if (offsetY.toInt() != 0)
        degs = toDegrees(atan(tan).toDouble()).toFloat() //сам угол поворота
    else degs = 0F
    return degs
}

fun main() = application {
    val quadroPodBody = useResource("quadroPodBody2.PNG") { loadImageBitmap(it) }
    val bodyWidth = quadroPodBody.width
    val bodyHeight = quadroPodBody.height

    val image = quadroPodBody
    val pixMap = image.toPixelMap()
    var rotatePoints = arrayOf<Pair<Int, Int>>()
    for (x in 11 until image.width) { //в циклах ищем красные точки, чтоб их добавить к полигону
        for (y in 11 until image.height) {
            if (pixMap[x, y].green in (0.7..1.0)){
//                println("found green at $x $y")
                rotatePoints = rotatePoints.plus(Pair(x,y))
            }
        }
    }
    println("points = ${rotatePoints.toList()}")
    println()
    println("image size on start= ${quadroPodBody.width} x ${quadroPodBody.height}")
    Window(onCloseRequest = ::exitApplication, state = WindowState(size = DpSize(bodyWidth.dp, bodyHeight.dp))) {
//    Window(onCloseRequest = ::exitApplication ) {
        App(quadroPodBody, rotatePoints, loadArms())
    }
}

fun loadArms(): Array<ImageBitmap> { //для загрузки изображений ног робота (плечи)
    var armImagesArray = arrayOf<ImageBitmap>()
    for (i in 0..3){
        val arm = useResource("arm${i+1}.PNG") { loadImageBitmap(it) }
        armImagesArray = armImagesArray.plus(arm)
    }
//    armImagesArray.forEach {
//        println("arm = ${it.height}")
//    }
    return armImagesArray
}

