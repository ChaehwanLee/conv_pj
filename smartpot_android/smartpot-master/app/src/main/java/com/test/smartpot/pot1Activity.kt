package com.test.smartpot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat

import kotlinx.android.synthetic.main.activity_pot1.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.roundToInt

class pot1Activity : AppCompatActivity() {
    private var soilWaterFlag = 0 // 정상상태 - 0
    private var tempModeFlag = 0 // 정상상태 - 0
    private var waterWarnFlag = 0 // 물 양 정상상태 - 0
    private var solarAlertFlag = 0 // 빛 양 정상상태 - 0

    private var solarAlertValue = 500 // 조도를 판단하는 기준
    private var lightValue = 0 // 들어오는 조도의 값
    private var soilWaterValue = 0.0 // 들어오는 토양 수분 값
    private var soilWaterAlertValue = 20.0 // 경고
    private var tempAlertValue = 27
    private var plantMode: String = "" // 식물 이름
    private var growmode = 7 // 생육상태

    private val sub_topic = "sensor1/#" // 구독할 토픽
    private val server_uri = "tcp://3.96.178.99:1883" //broker의 ip와 port
    private var mymqtt: MyMqtt? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pot1)
        //mqtt 기본 설정
        mymqtt = MyMqtt(this, server_uri)
        mymqtt?.mysetCallback(::onReceived)
        mymqtt?.connect(arrayOf<String>(sub_topic))
        val intent = intent
        //인텐트에서 가져오기
        pot1name.text = intent.getStringExtra("potname").toString()
        plantName.text = intent.getStringExtra("plantname").toString()
        plantMode = intent.getStringExtra("plantname").toString()
        //리스트에 추가

        diseaseChk.setOnClickListener {
            val disintent = Intent(this, diseaseChkActivity::class.java).apply {
                putExtra("potname","${pot1name.text}")
                putExtra("plantname","${plantName.text}")
            }
            startActivity(disintent)
        }
        levelChk.setOnClickListener {
            val levelIntent = Intent(this,potLevelActivity::class.java).apply{
                putExtra("potname",pot1name.text.toString())
                putExtra("plantname",plantName.text.toString())
            }
            startActivity(levelIntent)
        }
    }
    //mqtt 값을 받는 경우
    fun onReceived(topic:String,message: MqttMessage){
        val myTopic = topic.split('/')
        when(myTopic[1]){
            // 센서값 받는 경우
            "every" -> {
                Thread{
                    // 알림의 설정
                    val myPayload = String(message.payload).split(':')
                    var bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_flowerpot_320)
                    var builder1 = getNotificationBuilder("channel1","첫번째 채널")
                    var builder2 = getNotificationBuilder("channel2", "두번째 채널")
                    var builder4 = getNotificationBuilder("channel4", "네번째 채널")
                    soilWaterValue = myPayload[1].toDouble()
                    lightValue = myPayload[3].toDouble().roundToInt()
                    runOnUiThread {
                        waterLevel.text = (myPayload[0].toDouble() * 5.0).roundToInt().toString()

                        airhumid.text = myPayload[4].toDouble().roundToInt().toString()

                    }



                    if(solarAlertValue > lightValue){
                        if(solarAlertFlag == 0){
                            solarAlertFlag = 1 // 경고값보다 낮음
                            // 알림
                            val bigText = "LED의 높이를 조도가 " +
                                    solarAlertValue + "lux 가 넘도록 올려주세요"
                            val style = NotificationCompat.BigTextStyle()   // 1
                            style.bigText(bigText)    // 2
                            builder4.setTicker("조도 경고")
                            builder4.setSmallIcon(R.drawable.ic_flowerpot_320)
                            builder4.setLargeIcon(bitmap)
                            builder4.setNumber(100)
                            builder4.setStyle(style)   // 3
                            builder4.setAutoCancel(true)
                            builder4.setContentTitle("현재 조도 : " + myPayload[3] + "lux")
                            builder4.setContentText("")
                            var notication4 = builder4.build()
                            var mng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mng.notify(10, notication4)
                            runOnUiThread{
                                ledLux.setTextColor(Color.RED)
                                ledLux.text = lightValue.toString()
                            }
                        }
                    }else{
                        solarAlertFlag = 0
                        runOnUiThread{
                            ledLux.setTextColor(Color.BLUE)
                            ledLux.text = lightValue.toString()
                        }
                    }

                    // 토양 수분이 일정 값 보다 작으면 알림을 보내는 코드
                    if (soilWaterValue < soilWaterAlertValue){
                        if(soilWaterFlag == 0){
                            soilWaterFlag = 1 // 경고값보다 낮음
                            // 알림

                            builder1.setTicker("토양 수분 값")
                            builder1.setSmallIcon(R.drawable.ic_flowerpot_320)
                            builder1.setLargeIcon(bitmap)
                            builder1.setNumber(100)
                            builder1.setAutoCancel(true)
                            builder1.setContentTitle("토양 수분 경고")
                            builder1.setContentText(myPayload[1])
                            var notication1 = builder1.build()
                            var mng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mng.notify(10, notication1)

                        }
                        runOnUiThread{
                            soilWater.setTextColor(Color.RED)
                            soilWater.text = myPayload[1]

                        }
                    }else{
                        soilWaterFlag = 0
                        runOnUiThread{
                            soilWater.setTextColor(Color.BLUE)
                            soilWater.text = myPayload[1]
                            
                        }
                    }
                    // 온도가 일정 값보다 작으면 경고를 보내는 코드
                    if(myPayload[2].toDouble() < tempAlertValue){
                        if(tempModeFlag == 0){
                            tempModeFlag = 1 // 경고값보다 낮음
                            // 알림
                            builder2.setTicker("온도값")
                            builder2.setSmallIcon(R.drawable.ic_flowerpot_320)
                            builder2.setLargeIcon(bitmap)
                            builder2.setNumber(100)
                            builder2.setAutoCancel(true)
                            builder2.setContentTitle("온도 경고")
                            builder2.setContentText(myPayload[2])
                            var notication2 = builder2.build()
                            var mng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mng.notify(10, notication2)
                        }
                        runOnUiThread {
                            airtemp.text = myPayload[2]
                        }
                    }else{
                        tempModeFlag = 0
                        runOnUiThread {
                            airtemp.text = myPayload[2]
                        }
                    }
                }.start()
            }
            "growmode" -> { // 질병 + 생육단계 를 받음
                var growpayload = String(message.payload).split(':')
                when(growpayload[0]){ // 생육단계를 출력하기 위함
                    "1" -> {
                        potStatus.text = "1단계"
                        growmode = 1
                    }
                    "2" -> {
                        potStatus.text = "2단계"
                        growmode = 2
                    }
                    "3" -> {
                        potStatus.text = "3단계"
                        growmode = 3
                    }
                    "4" -> {
                        potStatus.text = "4단계"
                        growmode = 4
                    }
                    "5" -> {
                        potStatus.text = "5단계"
                        growmode = 5
                    }

                    "7" -> {
                        potStatus.text = "정상"
                        growmode = 7
                    }
                    "8" -> {
                        potStatus.text = "1단계"
                        growmode = 8
                    }
                    "9" -> {
                        potStatus.text = "2단계"
                        growmode = 9
                    }
                    else -> {
                        potStatus.text = "정상"
                    }
                }
                //potStatus.text = "5단계"
                // 모드 설정
                setMode(plantMode)

                pot1Disease.text = growpayload[1]
            }
            "waterWarn" -> {
                var builder3 = getNotificationBuilder("channel3", "세번째 채널")
                var bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.ic_flowerpot_320)
                builder3.setTicker("물 부족 경고")
                builder3.setSmallIcon(R.drawable.ic_flowerpot_320)
                builder3.setLargeIcon(bitmap3)
                builder3.setNumber(100)
                builder3.setAutoCancel(true)
                builder3.setContentTitle("물탱크 물 부족")
                builder3.setContentText("물탱크의 물이 부족합니다다")
                var notication3 = builder3.build()
                var mng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mng.notify(10, notication3)
            }
        }
    }


    fun getNotificationBuilder(id: String, name: String): NotificationCompat.Builder {
        var builder: NotificationCompat.Builder? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // 채널 정보를 시스템에 등록할 객체 생성
            var channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            manager.createNotificationChannel(channel) //채널 정보를 시스템에 등록
            builder = NotificationCompat.Builder(this, id) // 빌더를 생성만 해 놓음
        } else {
            builder = NotificationCompat.Builder(this)
        }
        return builder
    }

    // 식물에 따른 모드 설정
    fun setMode(mode: String){
        when(mode){// 식물, 생육상태에 따라서 토양습도, 온도, 조도 경고 기준값 설정
            "Strawberry" -> {
                when(growmode){
                    1 -> {
                        soilWaterAlertValue = 20.0 // 토양습도경고
                        tempAlertValue = 28 // 한계온도
                        solarAlertValue = 500 // 조도를 판단하는 기준
                    }
                    2 -> {
                        soilWaterAlertValue = 20.0 // 경고
                        tempAlertValue = 25 // 한계온도
                        solarAlertValue = 500 // 조도를 판단하는 기준
                    }
                    3 -> {
                        soilWaterAlertValue = 20.0 // 경고
                        tempAlertValue = 23 // 한계온도
                        solarAlertValue = 500 // 조도를 판단하는 기준
                    }
                    4 -> {
                        soilWaterAlertValue = 20.0 // 경고
                        tempAlertValue = 20 // 한계온도
                        solarAlertValue = 500 // 조도를 판단하는 기준
                    }
                    5 -> {
                        soilWaterAlertValue = 20.0 // 경고
                        tempAlertValue = 20 // 한계온도
                        solarAlertValue = 500 // 조도를 판단하는 기준
                    }
                }

            }
            "Lettuce" -> {
                soilWaterAlertValue = 20.0 // 경고
                tempAlertValue = 27
                solarAlertValue = 500 // 조도를 판단하는 기준
            }
            "Rosemary" -> {
                soilWaterAlertValue = 20.0 // 경고
                tempAlertValue = 27
                solarAlertValue = 500 // 조도를 판단하는 기준
            }
            "Geranium" -> {
                soilWaterAlertValue = 20.0 // 경고
                tempAlertValue = 27
                solarAlertValue = 500 // 조도를 판단하는 기준
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mymqtt?.disconnect()
    }
}