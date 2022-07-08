package com.test.smartpot

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MyMqtt(context: Context, uri:String) {
    var mqttClient: MqttAndroidClient = MqttAndroidClient(context,uri, MqttClient.generateClientId())
    fun mysetCallback(callback: (topic:String,message: MqttMessage)->Unit){
        mqttClient.setCallback(object : MqttCallback {
            //연결 끊김
            override fun connectionLost(cause: Throwable?) {
                Log.d("mqtt","connectionLost")
            }
            //메시지 수신 시
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                //메시지가 전송되면 호출 - 액티비티의 메소드
                //액티비티 구성요소에 메시지 전달되는 내용을  출력하고 사용하기 위해 액티비티에 구현한 메소드를 호출할 예정
                Log.d("mqtt","messageArrived")
                callback(topic!!,message!!)
            }
            //메시지 전송 완료 시
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("mqtt","deliveryComplete")
            }
        })
    }
    //mqtt통신을 하기 위해 브로커서버와 연결, 연결이 끝난 후 콜백메소드 설정
    fun connect(topic:Array<String>){
        //연결하기 위해서 필요한 여러가지 정보를 담고 있는 객체
        val mqttConnectOptions = MqttConnectOptions()
        //mqttAndroidClient객체의 connect를 호출하여 브로커에 연결을 시도
        //안드로이드 내부에서 브로커에 연결을 성공한면 자동으로 이벤트가 발생하며 이를 처리하는 리스너가 IMqttActionListener
        mqttClient.connect(mqttConnectOptions,null,
            object: IMqttActionListener {//커넥트시 실행
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                //접속 성공
                Log.d("mymqtt","브로커 접속 성공....")
                //토픽을 여러 개 등록할 수 있으므로 배열처리
                //모든 전달된 토픽을 subscribe
                //for,if로 처리 가능 하지만 map함수를 이용하면 간편
                topic.map{
                    subscribeTopic(it) // 토픽 배열의 각각을 구독한다.
                }
            }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?
                ) {
                    //접속 실패
                    Log.d("mymqtt","브로커 접속 실패....")
                }
            })
    }

    //토픽을 subscribe로 등록하기 위해서 메소드 구현 --------1
    private fun subscribeTopic(topic: String,qos: Int=0){
        mqttClient.subscribe(topic, qos,null,object: IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("mqtt","구독 성공")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("mqtt","구독 실패")
            }

        })
    }
    fun publish(topic:String,payload:String,qos:Int=0){
        if(mqttClient.isConnected === false){
            mqttClient.connect()
        }
        //mqtt로 전송할 메시지객체를 생성
        val message = MqttMessage()
        //메시지 객체에 payload와 메시지전송품질 설정
        message.payload = payload.toByteArray()
        message.qos = qos
        //메시지 전송하기 (publish) - publish의 성공/실패하는 경우 이벤트가 발생하기 때문에 리스너등록
        mqttClient.publish(topic,message,null,object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("mymqtt","메시지 전송 성공....")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("mymqtt","메시지 전송 실패....")
            }

        })
    }
    fun disconnect(){
        Log.d("this","client disconnected");
        mqttClient.disconnect()
    }
}