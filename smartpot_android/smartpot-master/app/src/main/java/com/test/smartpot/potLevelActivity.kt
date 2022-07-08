package com.test.smartpot


import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_pot1.*
import kotlinx.android.synthetic.main.activity_pot_level.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class potLevelActivity : AppCompatActivity() {
    var sub_topic = ""
    var sub_topic2 = "sensor1/growmode"
    val server_uri = "tcp://3.96.178.99:1883" //broker의 ip와 port
    var mymqtt: MyMqtt? = null
    lateinit var image1: Bitmap
    var msg1: ByteArray? = null
    lateinit var imageuri: Uri
    var imageFlag = 0
    var plantname = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pot_level)

        val intent = intent
        potlevelname.text = intent.getStringExtra("potname")
        plantname = intent.getStringExtra("plantname").toString()
        sub_topic = "iot/awslevelDisease/$plantname" // 해당하는 식물이름에 따른 주제를 구독

        mymqtt = MyMqtt(this, server_uri)
        mymqtt?.mysetCallback(::onReceived)
        mymqtt?.connect(arrayOf<String>(sub_topic, sub_topic2))

        //공유하기 클릭시 원하는 곳으로 공유
        snsShareBtn.setOnClickListener {
            if(imageFlag == 1){
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT,"지금 ${potlevelname.text}의 레벨은 ${potLevel.text}")
                    putExtra(Intent.EXTRA_STREAM, imageuri)
                    type = "image/png"
                }
                startActivity(Intent.createChooser(shareIntent, "Helllo"))
            }else{
                Toast.makeText(this,"'현재 상태' 버튼을 눌러 이미지를 받아 주세요",Toast.LENGTH_LONG).show()
            }
        }
        levelCameraBtn.setOnClickListener {
            mymqtt?.publish("iot/actLevelCamera", "cameraon")
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun onReceived(topic:String, message: MqttMessage){
        when(topic){
            sub_topic -> {
                val msg = message.payload
                val image= BitmapFactory.decodeByteArray(msg, 0, msg.size);
                image1 = image
                imageFlag = 1
                levelchkcamera.setImageBitmap(image)
                saveImageUpAndVerQ(image1)
            }
            sub_topic2 -> {
                var growpayload = String(message.payload).split(':')
                when(growpayload[0]){
                    "1" -> {
                        potLevel.text = "1단계"
                    }
                    "2" -> {
                        potLevel.text = "2단계"
                    }
                    "3" -> {
                        potLevel.text = "3단계"
                    }
                    "4" -> {
                        potLevel.text = "4단계"
                    }
                    "5" -> {
                        potLevel.text = "5단계"
                    }
                    "6" -> {
                        potLevel.text = "2단계"
                    }
                    "7" -> {
                        potLevel.text = "정상"
                    }
                }
            }
        }
    }
    //Android Q 이상
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageUpAndVerQ(bitmap: Bitmap) {
        val mqttfileName = System.currentTimeMillis().toString() + ".png" // 파일이름
        /*
            ContentValues() 객체 생성.
            ContentValues는 ContentResolver가 처리할 수 있는 값을 저장.
        */
        val contentInfomation = ContentValues()
        contentInfomation.apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ImageSave") // 경로
            put(MediaStore.Images.Media.DISPLAY_NAME, mqttfileName) // 파일이름
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1) // 저장하는 동안 현재 저장소 독점
        }
        // 이미지를 저장할 uri를 미리 설정.
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentInfomation)
        if (uri != null) {
            imageuri = uri
        }
        try {
            if(uri != null) {
                val image = contentResolver.openFileDescriptor(uri, "w", null)
                // write 모드로 file을 open한다.

                if(image != null) {
                    val outputstream = FileOutputStream(image.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputstream)
                    outputstream.close()

                    contentInfomation.clear()
                    contentInfomation.put(MediaStore.Images.Media.IS_PENDING, 0) // 저장소 독점을 해제한다.
                    contentResolver.update(uri, contentInfomation, null, null)
                }
            }
        } catch(e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mymqtt?.disconnect()
    }
}