package com.test.smartpot

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_disease_chk.*
import kotlinx.android.synthetic.main.activity_pot1.*
import org.eclipse.paho.client.mqttv3.MqttMessage

class diseaseChkActivity : AppCompatActivity() {
    var sub_topic = ""
    val sub_topic2 = "sensor1/growmode"
    val server_uri = "tcp://3.96.178.99:1883" //broker의 ip와 port
    var mymqtt: MyMqtt? = null
    var plantname = ""
    var diseaseToggleFlag1 = 0 // 첫번째 칸 접혀있는 상태
    var diseaseToggleFlag2 = 0 // 두번째 칸 접혀있는 상태
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_chk)
        val intent = intent
        diseasepotchkname.text = intent.getStringExtra("potname").toString()
        plantname = intent.getStringExtra("plantname").toString()
        sub_topic = "iot/awslevelDisease/$plantname" // 해당하는 식물이름에 따른 주제를 구독

        mymqtt = MyMqtt(this, server_uri)
        mymqtt?.mysetCallback(::onReceived)
        mymqtt?.connect(arrayOf<String>(sub_topic, sub_topic2))

        //식물에 따라서 질병 예방 설명이 달라짐
        when(plantname){
            "Strawberry" ->{
                methodBtn1.text = "잿빛곰팡이병"
                methodBtn2.text = "흰가루병"
                methodBtn1.setOnClickListener { // 버튼 터치시
                    when(diseaseToggleFlag1){
                        0 -> { // 접혀있는 상태였다면
                            methodText1.text = "이 병은 습도가 높으면 발병이 많으므로 시설 내부가 다습하지 않도록" +
                                    "\n 가능한 관수량을 줄이고 환기를 충분히 실시하여 재배하고," +
                                    "\n 저온기 재배시에는 온도조절만이 아니라 난방을 하여 습기를 제거한다. "
                            diseaseToggleFlag1 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText1.text = ""
                            diseaseToggleFlag1 = 0
                        }
                    }
                }
                methodBtn2.setOnClickListener {
                    when(diseaseToggleFlag2){
                        0 -> { // 접혀있는 상태였다면
                            methodText2.text = "설재배에서는 저녁때 관수나 약제살포를 피해 주야간의 온도차가 심하지 않도록 관리하며" +
                                    "\n 또한 천창이 열린 상태로 개방하여 습한 공기를 환기시킨 후 천창을 닫아 야간의 습도를 낮추어야 한다." +
                                    "\n 낮동안에는 최대한 실내의 습도를 낮추는 환경조절이 최선의 방법이다. "
                            diseaseToggleFlag2 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText2.text = ""
                            diseaseToggleFlag2 = 0
                        }
                    }
                }
            }
            "Lettuce" ->{
                methodBtn1.text = "균핵병"
                methodBtn2.text = "노균병"
                methodBtn1.setOnClickListener { // 버튼 터치시
                    when(diseaseToggleFlag1){
                        0 -> { // 접혀있는 상태였다면
                            methodText1.text = "방제법은 외떡잎작물과 4년 이상의 윤작, " +
                                    "\n특별한 작용 기작을 갖는 살균제 살포와 포장을 침지시켜 균핵을 파괴하는 방법등이 있다."
                            diseaseToggleFlag1 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText1.text = ""
                            diseaseToggleFlag1 = 0
                        }
                    }
                }
                methodBtn2.setOnClickListener {
                    when(diseaseToggleFlag2){
                        0 -> { // 접혀있는 상태였다면
                            methodText2.text = "밀식을 피하고 통풍과 채광을 좋게하며 시설내의 환기를 충분히 하고 온도를 낮추어 준다." +
                                    "\n 비료의 부족현상이 일어나지 않도록 충분한 시비를 하여야 하며" +
                                    "\n 하우스 내의 다습으로 인하여 잎에 이슬이 맺힐 때는 병이 만연하기 좋은 조건이 되므로 하우스 내의 습도를 낮추어 준다."
                            diseaseToggleFlag2 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText2.text = ""
                            diseaseToggleFlag2 = 0
                        }
                    }
                }
            }
            "Rosemary" ->{
                methodBtn1.text = "흰가루병"
                methodBtn2.text = "점무늬병"
                methodBtn1.setOnClickListener { // 버튼 터치시
                    when(diseaseToggleFlag1){
                        0 -> { // 접혀있는 상태였다면
                            methodText1.text = "설재배에서는 저녁때 관수나 약제살포를 피해 주야간의 온도차가 심하지 않도록 관리하며" +
                                    "\n 또한 천창이 열린 상태로 개방하여 습한 공기를 환기시킨 후 천창을 닫아 야간의 습도를 낮추어야 한다." +
                                    "\n 낮동안에는 최대한 실내의 습도를 낮추는 환경조절이 최선의 방법이다. "
                            diseaseToggleFlag1 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText1.text = ""
                            diseaseToggleFlag1 = 0
                        }
                    }
                }
                methodBtn2.setOnClickListener {
                    when(diseaseToggleFlag2){
                        0 -> { // 접혀있는 상태였다면
                            methodText2.text = "병원균은 종자 혹은 병든 부위에서 균사나 분생포자의 형태로 월동 후," +
                                    "\n 분생포자를 형성하여 공기전염 한다." +
                                    "\n 고온다습한 환경의 시설재배 포장에서 많이 발생하며" +
                                    "\n 병증상은 병원균이 감염된 후 5일 이내에 나타난다."
                            diseaseToggleFlag2 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText2.text = ""
                            diseaseToggleFlag2 = 0
                        }
                    }
                }
            }
            "Geranium" ->{
                methodBtn1.text = "갈색무늬병"
                methodBtn2.text = "잿빛곰팡이병"
                methodBtn1.setOnClickListener { // 버튼 터치시
                    when(diseaseToggleFlag1){
                        0 -> { // 접혀있는 상태였다면
                            methodText1.text = "관수 및 배수철저, 균형있는 시비, 전정을 통해 수관내 통풍과 통광을 원활히 한다." +
                                    "\n 병에 걸린 낙엽을 모아 태우거나 땅속깊이 묻어 월동전염원을 제거한다. "
                            diseaseToggleFlag1 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText1.text = ""
                            diseaseToggleFlag1 = 0
                        }
                    }
                }
                methodBtn2.setOnClickListener {
                    when(diseaseToggleFlag2){
                        0 -> { // 접혀있는 상태였다면
                            methodText2.text = "이 병은 습도가 높으면 발병이 많으므로 시설 내부가 다습하지 않도록" +
                                    "\n 가능한 관수량을 줄이고 환기를 충분히 실시하여 재배하고," +
                                    "\n 저온기 재배시에는 온도조절만이 아니라 난방을 하여 습기를 제거한다. "
                            diseaseToggleFlag2 = 1
                        }
                        1 -> { // 펴져있는 상태였다면
                            methodText2.text = ""
                            diseaseToggleFlag2 = 0
                        }
                    }
                }
            }
        }
        diseaseCameraBtn.setOnClickListener {
            mymqtt?.publish("iot/actDiseaseCamera", "cameraon")
        }
    }

    fun onReceived(topic:String,message: MqttMessage){
        //토픽 설정 필요
        when(topic){
            // 이미지를 받은 경우 mqttImg로 출력
            sub_topic -> {
                val msg = message.payload
                val image= BitmapFactory.decodeByteArray(msg, 0, msg.size);
                mqttImg.setImageBitmap(image)
            }
            sub_topic2 -> { // 질병값 받을 때 식물에 따라서 걸리는 질병이 다르므로 분류하여 적용
                var growpayload = String(message.payload).split(':')
                when(plantname){
                    "Strawberry" ->{
                        when(growpayload[1]){
                            "해충" -> {
                                "해충이 있으니 방제해주세요"
                            }
                            "잿빛곰팡이병"->{
                                copingMethod.text = "일단 발병이 시작되면 빠르게 전반되어 " +
                                        "\n방제가 매우 어렵고 또한 기온이 선선하고 습윤한 조건이 계속되면 " +
                                        "\n농약을 살포해도 방제가 되지 않기 때문에 무엇보다도 예방이 중요하다."
                            }
                            "흰가루병" -> {
                                copingMethod.text = "발병 초기에 등록약제를 살포하여 초기의 병원균 밀도를 줄여준다."
                            }
                            else -> {
                                copingMethod.text = "정상입니다"
                            }

                        }
                    }
                    "Lettuce" ->{
                        when(growpayload[1]){
                            "균핵병"->{
                                copingMethod.text = "병든 식물체는 그 주변의 흙과 함께 뽑아내어 땅속 깊이 파묻는다."
                            }
                            "노균병" -> {
                                copingMethod.text = "발병시 환기를 급속히 자주하면 병이 넓게" +
                                        "\n 빨리 번져 나가게 되므로 서서히 해야 된다. "
                            }
                            else -> {
                                copingMethod.text = "정상입니다"
                            }


                        }
                    }
                    "Rosemary" ->{
                        when(growpayload[1]){
                            "흰가루병"->{
                                copingMethod.text = "\"발병 초기에 등록약제를 살포하여 초기의 병원균 밀도를 줄여준다.\""
                            }
                            "점무늬병" -> {
                                copingMethod.text = "일단 발병이 시작되면 빠르게 전반되어 " +
                                        "\n방제가 매우 어렵고 또한 기온이 선선하고 습윤한 조건이 계속되면 " +
                                        "\n농약을 살포해도 방제가 되지 않기 때문에 무엇보다도 예방이 중요하다."
                            }
                            else -> {
                                copingMethod.text = "정상입니다"
                            }


                        }
                    }
                    "Geranium" ->{
                        when(growpayload[1]){
                            "갈색무늬병"->{
                                copingMethod.text = "과수원에서 초기병반이 보이는 즉시 약제를 살포한다.\n" +
                                        "이 병은 한번 발생하면 이후 방제하기가 매우 곤란한 병이므로 예방에 초점을 맞추어 방제한다"
                            }
                            "잿빛곰팡이병" -> {
                                copingMethod.text = "일단 발병이 시작되면 빠르게 전반되어 " +
                                        "\n방제가 매우 어렵고 또한 기온이 선선하고 습윤한 조건이 계속되면 " +
                                        "\n농약을 살포해도 방제가 되지 않기 때문에 무엇보다도 예방이 중요하다."
                            }
                            else -> {
                                copingMethod.text = "정상입니다"
                            }
                        }
                    }
                }

                diseaseResult.text = growpayload[1]
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mymqtt?.disconnect()
    }
}

