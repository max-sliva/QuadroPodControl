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
//import androidx.compose.ui.draw.EmptyBuildDrawCacheParams.size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

@Composable
@Preview
fun App(quadroPodBody: ImageBitmap) {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(), //заполняем всё доступное пространство
            horizontalAlignment = Alignment.CenterHorizontally, //по центру горизонтально
//            verticalArrangement = Arrangement.Center //и вертикально
        ) {
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }

            Canvas(modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
            ){
                val canvasQuadrantSize = size / 2F
                try {
//                    val mBitmapFromSdcard = BitmapFactory.decodeFile("/mnt/sdcard/face.png").asImageBitmap()
//                    val quadroPodBody = useResource("quadroPodBody2.PNG") { loadImageBitmap(it) }
                    drawImage(
                        image = quadroPodBody,
                        topLeft = Offset(0F, 0F)
                    )
                    val arm1 = useResource("arm1.PNG") { loadImageBitmap(it) }
                    drawImage(
                        image = arm1,
                        topLeft = Offset(offsetX,offsetY)
                    )
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
    Window(onCloseRequest = ::exitApplication, state = WindowState(size = DpSize(bodyWidth.dp, bodyHeight.dp)) ) {
//    Window(onCloseRequest = ::exitApplication ) {
        App(quadroPodBody)
    }
}
