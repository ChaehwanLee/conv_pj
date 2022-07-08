package com.test.smartpot

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_pot1.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    var potlist = 0
    var datalist = ArrayList<PotList>()
    var imglist = ArrayList<Int>()
    private val sub_topic = "111111/#" // 구독할 토픽
    private val server_uri = "tcp://3.96.178.99:1883" //broker의 ip와 port
    private var mymqtt: MyMqtt? = null

    private lateinit var resultLauncher1: ActivityResultLauncher<Intent>
    private lateinit var resultLauncher2: ActivityResultLauncher<Intent>
    private lateinit var resultLauncher3: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        potListInit()
        mymqtt = MyMqtt(this, server_uri)
        mymqtt?.mysetCallback(::onReceived)
        mymqtt?.connect(arrayOf<String>(sub_topic))
        //화분 데이터 추가를 위해 인텐트를 통해 이동 후 돌아왔을 때 화분을 리스트에 추가하기 위한 런처
        resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            when(result.resultCode){
                Activity.RESULT_OK -> {
                    Log.d("dataa",result.data?.getStringExtra("plantname")!!)
                    datalist.add(PotList(result.data?.getStringExtra("potname"),result.data?.getStringExtra("plantname")))
                    imglist.add(R.drawable.ic_flowerpot_320)
                    potlist = result.data?.getIntExtra("potlistcount",0)!!
                    addpot()
                    when(potlist){ // 화분 갯수에 따라서 추가하는 화분의 키가 달라짐
                        1 -> {
                            result.data?.getStringExtra("potname")?.let { myApp.prefs.setString("pot1name", it) }
                            result.data?.getStringExtra("plantname")?.let { myApp.prefs.setString("pot1plantname", it) }
                            mymqtt?.publish("mode/pot1PlantName", result.data?.getStringExtra("plantname")!!)
                            myApp.prefs.setString("potlist","1")
                        }
                        2 -> {
                            result.data?.getStringExtra("potname")?.let { myApp.prefs.setString("pot2name", it) }
                            result.data?.getStringExtra("plantname")?.let { myApp.prefs.setString("pot2plantname", it) }
                            myApp.prefs.setString("potlist","2")
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this,"화분 추가 취소",Toast.LENGTH_LONG).show()
                }
            }
        }
        resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            when(result.resultCode){
                Activity.RESULT_OK -> {
                    datalist[0].title = result.data?.getStringExtra("potname")
                    datalist[0].plant = result.data?.getStringExtra("plantname")
                    mymqtt?.publish("mode/pot1PlantModify", result.data?.getStringExtra("plantname")!!)
                    addpot()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this,"화분정보수정 취소",Toast.LENGTH_LONG).show()
                }
            }
        }
        resultLauncher3 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            when(result.resultCode){
                Activity.RESULT_OK -> {
                    datalist[1].title = result.data?.getStringExtra("potname")
                    datalist[1].plant = result.data?.getStringExtra("plantname")
                    addpot()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this,"화분정보수정 취소",Toast.LENGTH_LONG).show()
                }
            }
        }
        add_pot.setOnClickListener {
            if(potlist >= 2){
                Toast.makeText(this,"화분을 더 추가할 수 없습니다",Toast.LENGTH_LONG).show()
            }
            else{
                val intent = Intent(this, addpotactivity::class.java).apply{
                    putExtra("listcount",potlist)
                }
                resultLauncher1.launch(intent)
            }
        }

        pot1ModifyBtn.setOnClickListener {
            if(potlist == 0){
                Toast.makeText(this,"화분을 추가해 주세요",Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(this, modifypotActiviy::class.java).apply {
                    putExtra("potnumber","1")
                    putExtra("potname", datalist[0].title.toString())
                    putExtra("potplantname", datalist[0].plant.toString())
                }
                resultLauncher2.launch(intent)
            }
        }
        pot2ModifyBtn.setOnClickListener {
            if(potlist == 0 || potlist == 1){
                Toast.makeText(this,"화분을 추가해 주세요",Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(this, modifypotActiviy::class.java).apply {
                    putExtra("potnumber","2")
                    putExtra("potname", datalist[1].title.toString())
                    putExtra("potplantname", datalist[1].plant.toString())
                }
                resultLauncher3.launch(intent)
            }
        }
//        view_friend.setOnClickListener {
//            val intent = Intent(this, friendActivity::class.java)
//            startActivity(intent)
//        }
        addpot()
    }
    fun onReceived(topic:String,message: MqttMessage){

    }

    private fun addpot(){
        val adapter = PotListAdapter(this, R.layout.pot_list,datalist,imglist)
        val manager = GridLayoutManager(this,1)
        myrecycler1.layoutManager = manager
        myrecycler1.adapter = adapter
    }



    //앱을 처음 실행시 메인 액티비티에 화분 리스트를 보여주기 위한 메소드
    private fun potListInit(){
        when(myApp.prefs.getString("potlist","0").toInt()){
            1->{
                datalist.add(PotList(myApp.prefs.getString("pot1name","none"),myApp.prefs.getString("pot1plantname","none")))
                imglist.add(R.drawable.ic_flowerpot_320)
                potlist = 1

            }
            2-> {
                datalist.add(PotList(myApp.prefs.getString("pot1name","none"),myApp.prefs.getString("pot1plantname","none")))
                datalist.add(PotList(myApp.prefs.getString("pot2name","none"),myApp.prefs.getString("pot2plantname","none")))
                imglist.add(R.drawable.ic_flowerpot_320)
                imglist.add(R.drawable.ic_flowerpot_320)
                potlist = 2

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mymqtt?.disconnect()
    }
}