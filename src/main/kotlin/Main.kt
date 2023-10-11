import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
fun App(quadroPodBody: ImageBitmap) {
    var text by remember { mutableStateOf("Hello, World!") }
    //todo сделать 2 массива для 4 офсетов по х и у
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var degs by remember { mutableStateOf(0f) }
    var katet1 by remember { mutableStateOf(0f) }
    var katet2 by remember { mutableStateOf(0f) }
    var startPointX by remember { mutableStateOf(0f) }
    var startPointY by remember { mutableStateOf(0f) }
    println("angle at start = $degs")
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(), //заполняем всё доступное пространство
            horizontalAlignment = Alignment.CenterHorizontally, //по центру горизонтально
//            verticalArrangement = Arrangement.Center //и вертикально
        ) {
//            var offsetX by remember { mutableStateOf(0f) }
//            var offsetY by remember { mutableStateOf(0f) }
//            var degs by remember { mutableStateOf(0f) }
//            println("angle at start = $degs")
            Canvas(modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
//                    detectTapGestures(
//                        onTap = {
//                            println("x = ${it.x}  y = ${it.y}")
//                        }
//                    )
                    detectDragGestures(
                        onDragStart = { touch ->
                            println("Start of the interaction is x=${touch.x} y=${touch.y}")
                            startPointX = touch.x
                            startPointY = touch.y
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
//                        println("in listener x = ${dragAmount.x} y = ${dragAmount.y}")
                            //todo сделать определение зоны касания, и увеличивать соотвествующую offset из массива офсетов
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
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
                val canvasQuadrantSize = size / 2F
                try {
                    drawImage(
                        image = quadroPodBody,
                        topLeft = Offset(0F, 0F)
                    )
                    val arm1 = useResource("arm1.PNG") { loadImageBitmap(it) }

                    val tan = offsetY / abs(offsetX)
                    println("offsetY = $offsetY   offsetX = $offsetX")
                    if (offsetX.toInt() != 0) degs = toDegrees(atan(tan).toDouble()).toFloat() else degs = 0F
                    println("angle = $degs")
//                    rotate(degrees = -degs, Offset(arm1.width.toFloat(), (arm1.height / 2).toFloat())){
                    drawImage(
                        image = arm1,
                        topLeft = Offset(0F, 0F)
//                            topLeft = Offset(offsetX, offsetY)
                    )
//                    }
//                    println("image size = ${quadroPodBody.width} x ${quadroPodBody.height}")
                } catch (e: NullPointerException) {
//                    Toast.makeText(applicationContext,"No image", Toast.LENGTH_LONG).show()
                    println("No image")
                }
            }
        }
    }
}

fun main() = application {
    val quadroPodBody = useResource("quadroPodBody2.PNG") { loadImageBitmap(it) }
    val bodyWidth = quadroPodBody.width
    val bodyHeight = quadroPodBody.height

    println("image size on start= ${quadroPodBody.width} x ${quadroPodBody.height}")
    Window(onCloseRequest = ::exitApplication, state = WindowState(size = DpSize(bodyWidth.dp, bodyHeight.dp))) {
//    Window(onCloseRequest = ::exitApplication ) {
        App(quadroPodBody)
    }
}
