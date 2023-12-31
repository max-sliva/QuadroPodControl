//import androidx.compose.ui.draw.EmptyBuildDrawCacheParams.size
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import jssc.SerialPort
import jssc.SerialPortList


@Composable
@Preview
fun App(
    quadroPodBody: ImageBitmap,
    rotatePoints: Array<Pair<Int, Int>>,
    arms: Array<ImageBitmap>,
    portNames: Array<String>
) {
    //массив с мапом: начальные точки, оффсеты и точки поворота в виде пар значений
//    todo вставить в него все оффсеты, нач.точки и точки поворота
    var arrayForGettingAngles = arrayOf<HashMap<String, Pair<Float, Float>>>()
    var offsetXArray = remember { mutableStateListOf<Float>() }
    var offsetYArray = remember { mutableStateListOf<Float>() }
    repeat(4) {
        offsetXArray.add(0F)
        offsetYArray.add(0F)
    }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var degs by remember { mutableStateOf(0f) }
    var angleOnDragEnd by remember { mutableStateOf(0f) }
    var arm1RotatePointX by remember { mutableStateOf(0f) }
    var arm1RotatePointY by remember { mutableStateOf(0f) }
    var startPointXArray = remember { mutableStateListOf<Float>() }
    var startPointYArray = remember { mutableStateListOf<Float>() }
//    var legStartPointXArray = remember { mutableStateListOf<Float>() }
//    var legStartPointYArray = remember { mutableStateListOf<Float>() }
    var degsForArms = remember { mutableStateListOf<Int>() } //массив для хранения углов для каждой arm
    //вставляем углы для arms
    degsForArms.add(100)
    degsForArms.add(60)
    degsForArms.add(80)
    degsForArms.add(140)
    var degsForLegs = remember { mutableStateListOf<Float>() } //массив для хранения углов для каждой leg
    repeat(4) {
        startPointXArray.add(0F)
        startPointYArray.add(0F)
//        legStartPointXArray.add(0F)
//        legStartPointYArray.add(0F)
        degsForLegs.add(80F)
    }
    var startPointX by remember { mutableStateOf(0f) }
    var startPointY by remember { mutableStateOf(0f) }
    val openDialog = remember { mutableStateOf(false) }
    var curArm by remember { mutableStateOf(-1) }
    var curComPort by remember { mutableStateOf("") }
    var curSerialPort by remember { mutableStateOf(SerialPort("0")) }
//    print(" angle at start = $degs")
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(), //заполняем всё доступное пространство
            horizontalAlignment = Alignment.CenterHorizontally, //по центру горизонтально
//            verticalArrangement = Arrangement.Center //и вертикально
        ) {
            if (openDialog.value) MakeAlertDialog(
                curArm.toString(),
                openDialog,
                degsForLegs[curArm],
                curSerialPort
//                legStartPointXArray[curArm],
//                legStartPointYArray[curArm]
            ) { x -> //ф-ия обратного вызова для запоминания угла
                degsForLegs[curArm] = x
//                legStartPointYArray[curArm] = y
//                println("degsForLegs = $x ")
            } //для вызова окна с нужной leg
            DropdownDemo(portNames.toList()){x-> //лямбда для ф-ии обратного вызова
                curComPort=x
                if (curComPort!=""){
                    curSerialPort = SerialPort(curComPort)
                    curSerialPort.openPort()
                }
            }
            Canvas(modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touch ->
//                            println("\nStart of the interaction is x=${touch.x} y=${touch.y}")
                            startPointX = touch.x
                            startPointY = touch.y
                            offsetX = 0F //сбрасываем оффсеты, чтобы нормально двигать ногу
                            offsetY = 0F
                            var number = getArmNumber(startPointX, quadroPodBody, startPointY)
                            startPointXArray[number] = startPointX
                            startPointYArray[number] = startPointY
                            offsetXArray[number] = offsetX
                            offsetYArray[number] = offsetY
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
//                            println("in listener x    = ${dragAmount.x}  y = ${dragAmount.y}  ")
//                            println("arm1RotatePointX = $arm1RotatePointX arm1RotatePointY = $arm1RotatePointY" )
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            var number = 0
                            number = getArmNumber(startPointX, quadroPodBody, startPointY)    //для leg4
                            offsetXArray[number] += dragAmount.x
                            offsetYArray[number] += dragAmount.y
                        },
                        onDragEnd = {
//                            println("angle on drag end = $degs")
//                           angleOnDragEnd = degs
                            val number = getArmNumber(startPointX, quadroPodBody, startPointY)
                            if (curSerialPort.portName != "") writeArmAngleToComPort(curSerialPort, number, degsForArms[number])
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {//при клике на нужной arm
//                            println("x = ${it.x}  y = ${it.y}")
                            var number = 0
                            if (it.x < quadroPodBody.width / 2 && it.y < quadroPodBody.height / 2) number = 0 //для arm1
                            else if (it.x < quadroPodBody.width / 2 && it.y > quadroPodBody.height / 2) number =
                                1 //для arm2
                            else if (it.x > quadroPodBody.width / 2 && it.y < quadroPodBody.height / 2) number =
                                2   //для третьей лапы
                            else if (it.x > quadroPodBody.width / 2 && it.y > quadroPodBody.height / 2) number =
                                3    //для четвертой лапы
//                            println("leg = $number")
                            curArm = number
                            openDialog.value = true
                            println("curPort = $curComPort")
                        }
                    )
                }
            ) {
//                val canvasQuadrantSize = size / 2F
                try {
                    drawImage(
                        image = quadroPodBody,
                        topLeft = Offset(0F, 0F)
                    )
                    val arm1 = arms[0]
                    armRotate(
                        1,
                        0F,
                        0F,
                        arm1,
                        startPointXArray[0],
                        startPointYArray[0],
                        offsetXArray[0],
                        offsetYArray[0],
                        rotatePoints[0],
                        curSerialPort,
                        degsForArms[0]
                    ){x-> degsForArms[0]=x }
//                    armRotate(0F,0F,arm1, startPointX, startPointY, offsetX, offsetY, rotatePoints)
                    val arm2 = arms[1]
//                    val x0ForArm2 = rotatePoints[1].first
//                    val y0ForArm2 = rotatePoints[1].second-rotatePoints[0].second
                    val y0ForArm2 = rotatePoints[1].second - 80 //позиционируем вторую лапу
                    armRotate(
                        2,
                        0F,
                        y0ForArm2.toFloat(),
                        arm2,
                        startPointXArray[1],
                        startPointYArray[1],
                        offsetXArray[1],
                        offsetYArray[1],
                        rotatePoints[1],
                        curSerialPort,
                        degsForArms[1]
                    ) {x-> degsForArms[1]=x}
                    val arm3 = arms[2]
                    val x0ForArm3 = rotatePoints[2].first - 40 //позиционируем третью лапу
                    armRotate(
                        3,
                        x0ForArm3.toFloat(),
                        7F,
                        arm3,
                        startPointXArray[2],
                        startPointYArray[2],
                        offsetXArray[2],
                        offsetYArray[2],
                        rotatePoints[2],
                        curSerialPort,
                        degsForArms[2]
                    ) {x-> degsForArms[2]=x}
                    val arm4 = arms[3]
                    val x0ForArm4 = rotatePoints[3].first - 40
                    val y0ForArm4 = rotatePoints[3].second - 55 //позиционируем четвертую лапу
                    armRotate(
                        4,
                        x0ForArm4.toFloat(),
                        y0ForArm4.toFloat(),
                        arm4,
                        startPointXArray[3],
                        startPointYArray[3],
                        offsetXArray[3],
                        offsetYArray[3],
                        rotatePoints[3],
                        curSerialPort,
                        degsForArms[3]
                    ) {x-> degsForArms[3]=x}
                } catch (e: NullPointerException) {
//                    Toast.makeText(applicationContext,"No image", Toast.LENGTH_LONG).show()
                    println("No image")
                }
            }
        }
    }
}

private fun getArmNumber(
    startPointX: Float,
    quadroPodBody: ImageBitmap,
    startPointY: Float
): Int {
    var number = 0
    if (startPointX < quadroPodBody.width / 2 && startPointY < quadroPodBody.height / 2)
        number = 0 //для leg1
    else if (startPointX < quadroPodBody.width / 2 && startPointY > quadroPodBody.height / 2)
        number = 1 //для leg2
    else if (startPointX > quadroPodBody.width / 2 && startPointY < quadroPodBody.height / 2)
        number = 2   //для leg3
    else if (startPointX > quadroPodBody.width / 2 && startPointY > quadroPodBody.height / 2)
        number = 3
    return number
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MakeAlertDialog(
    curArm: String,
    openDialog: MutableState<Boolean>,
    degsInLeg: Float,
    curSerialPort: SerialPort,
//    startPointX: Float,
//    startPointY: Float,
    onUpdate: (x: Float) -> Unit
) { //показываем окно с нужным leg для его поворота
//    var degsInDialog = 0f
    var degs by remember { mutableStateOf(0f) }
    degs = degsInLeg

    AlertDialog(
        onDismissRequest = { //действия при закрытии окна
            openDialog.value = false
            onUpdate(degs)
            println("Exit")
        },
        modifier = Modifier.fillMaxSize(),
        title = { Text(text = curArm) }, //заголовок окна
        text = { //внутренняя часть окна
            val backImage = useResource("back.PNG") { loadImageBitmap(it) }
            val legBody = useResource("leg${curArm.toInt() + 1}_body_.PNG") { loadImageBitmap(it) }//содержимое окна
            val pixMap = legBody.toPixelMap()
            var rotatePoint: Pair<Int, Int>? = null
            for (x in 11 until legBody.width) { //в циклах ищем зеленые точки, чтоб их добавить к массиву точек поворота
                for (y in 11 until legBody.height) {
                    if (pixMap[x, y].green in (0.7..1.0) && pixMap[x, y].red < 0.5 && pixMap[x, y].blue < 0.5) {
//                        println("found green on leg${curArm.toInt() + 1} at $x $y")
                        rotatePoint = Pair(x, y)
                    }
                }
            }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var startPointX by remember { mutableStateOf(0f) }
            var startPointY by remember { mutableStateOf(0f) }

            Canvas(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { touch ->
//                            println("\nStart of the interaction is x=${touch.x} y=${touch.y}")
//                                onUpdate(touch.x, touch.y)
                                startPointX = touch.x
                                startPointY = touch.y

                                offsetX = 0F //сбрасываем оффсеты, чтобы нормально двигать ногу
                                offsetY = 0F
                                var number = 0
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
//                            println("in listener x    = ${dragAmount.x}  y = ${dragAmount.y}  ")
//                            println("arm1RotatePointX = $arm1RotatePointX arm1RotatePointY = $arm1RotatePointY" )
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                                degs = angle(rotatePoint!!.first.toFloat(), rotatePoint.second.toFloat(), startPointX, startPointY, offsetX, offsetY)
//                                println("angle = $degs")
                            },
                            onDragEnd = {
                                //тут передавать угол в ардуино

                                val curLeg = curArm.toInt()
                                println("degs for leg#${curLeg}  = $degs")
                                val angle = angleForServoLeg(degs, curLeg)
                                println("angle for leg#${curLeg} to arduino = $angle")
                                if (curSerialPort.portName != "") writeArmAngleToComPort(curSerialPort, curLeg, angle, false)
                            },
                        )
                    }
            ) {
                val leg = useResource("leg${curArm.toInt() + 1}.PNG") { loadImageBitmap(it) }//сама рука
//                println("leg image width = ${leg.width}")
                var rotatePointLeg: Pair<Int, Int>? = null
                val pixMapForLeg = leg.toPixelMap()
                for (x in 11 until leg.width) { //в циклах ищем зеленые точки, чтоб их добавить к массиву точек поворота
                    for (y in 11 until leg.height) {
                        if ((pixMapForLeg[x, y].green in (0.7..1.0) && curArm.toInt() != 0)
                            || (curArm.toInt() == 0 && pixMapForLeg[x, y].green in (0.7..1.0) && pixMapForLeg[x, y].red < 0.3 && pixMap[x, y].blue < 0.3)
                        ) {
//                        println("found green on leg${curArm.toInt() + 1} at $x $y")
                            rotatePointLeg = Pair(x, y)
                        }
                    }
                }
//                println("for leg pair.x = ${rotatePointLeg?.first}, pair.y = ${rotatePointLeg?.second}")
//                val degs = angle(rotatePoint!!.first.toFloat(), rotatePoint.second.toFloat(), startPointX, startPointY, offsetX, offsetY)
//                println("angle for leg = $degs")
                try {
                    drawImage(
                        image = backImage,
                        topLeft = Offset(0F, 0F)
                    )
//                    println("curArm = $curArm")
                    if (curArm.toInt() == 0 || curArm.toInt() == 2) {
                        legRotate(curArm.toInt(), degs, leg, rotatePointLeg!!, rotatePoint!!)
                        drawImage(
                            image = legBody,
                            topLeft = Offset(0F, 0F)
                        )
                    } else {
                        drawImage(
                            image = legBody,
                            topLeft = Offset(0F, 0F)
                        )
                        legRotate(curArm.toInt(), degs, leg, rotatePointLeg!!, rotatePoint!!)
                    }
                } catch (e: NullPointerException) {
//                    Toast.makeText(applicationContext,"No image", Toast.LENGTH_LONG).show()
                    println("No image")
                }
//                val degs = angle(armRotatePointX, armRotatePointY + y0, startPointX, startPointY, offsetX, offsetY)
            }
        },
        confirmButton = { //кнопка Ok, которая будет закрывать окно
            Button(onClick = {
                openDialog.value = false
                onUpdate(degs)
                println("Ok pressed")
            })
            { Text(text = "OK") }
        }
    )
}

fun main() = application {
    val quadroPodBody = useResource("quadroPodBody2.PNG") { loadImageBitmap(it) }
    val bodyWidth = quadroPodBody.width
    val bodyHeight = quadroPodBody.height

    val image = quadroPodBody
    val pixMap = image.toPixelMap()
    var rotatePoints = arrayOf<Pair<Int, Int>>()
    for (x in 11 until image.width) { //в циклах ищем зеленые точки, чтоб их добавить к массиву точек поворота
        for (y in 11 until image.height) {
            if (pixMap[x, y].green in (0.7..1.0)) {
//                println("found green at $x $y")
                rotatePoints = rotatePoints.plus(Pair(x, y))
            }
        }
    }
    println("points = ${rotatePoints.toList()}")
    println()
    println("image size on start= ${quadroPodBody.width} x ${quadroPodBody.height}")
//    val portNames = SerialPortList.getPortNames()

    Window(onCloseRequest = ::exitApplication, state = WindowState(size = DpSize(bodyWidth.dp, bodyHeight.dp))) {
//    Window(onCloseRequest = ::exitApplication ) {
        App(quadroPodBody, rotatePoints, loadArms(), SerialPortList.getPortNames())
    }
}

fun loadArms(): Array<ImageBitmap> { //для загрузки изображений ног робота (плечи)
    var armImagesArray = arrayOf<ImageBitmap>()
    for (i in 0..3) {
        val arm = useResource("arm${i + 1}.PNG") { loadImageBitmap(it) }
        armImagesArray = armImagesArray.plus(arm)
    }
//    armImagesArray.forEach {
//        println("arm = ${it.height}")
//    }
    return armImagesArray
}

@Composable
fun DropdownDemo(itemsInitial:  List<String>, onUpdate: (x: String) -> Unit) { //комбобокс для выбора компорта для подключения к Arduino
    var expanded by remember { mutableStateOf(false) }
//    val items = listOf("com1", "com2", "com3")
//    val disabledValue = "B"
    var items = remember { mutableStateListOf<String>() }
    itemsInitial.forEach {
        if (!items.contains(it))items.add(it)
    }
    var selectedIndex by remember { mutableStateOf(-1) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Text( //заголовок комбобокса
            if (selectedIndex<0) "Выберите порт: ▼" //если еще ничего не выбрано
            else items[selectedIndex], //если выбрано
            modifier = Modifier.clickable(onClick = { //при нажатии на текст раскрываем комбобокс
                val tempPortList = SerialPortList.getPortNames().toList() //получаем активные порты
                println("SerialPortList = $tempPortList")
                tempPortList.forEach {//добавляем новые порты к списку
                        if (!items.contains(it))items.add(it)
                }
                items.forEach{//убираем отключенные порты
                    if (!tempPortList.contains(it)) {
//                        println("$it not in SerialPortList")
                        items.remove(it)
                    }
                }
                expanded = true
            })
        )
        DropdownMenu( //сам выпадающий список для комбобокса
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            items.forEachIndexed { index, s -> //заполняем элементы выпадающего списка
                DropdownMenuItem(
                    onClick = { //обработка нажатия на порт
                        selectedIndex = index
                        expanded = false
                        onUpdate(s)
                        println("selected = $s")
                    }
                ) {
//                    val disabledText = if (s == disabledValue) {
//                        " (Disabled)"
//                    } else {
//                        ""
//                    }
                    Text(text = s )
                }
            }
        }
    }
}
