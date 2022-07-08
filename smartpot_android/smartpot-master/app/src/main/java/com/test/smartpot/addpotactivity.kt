package com.test.smartpot

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.addpot.*


class addpotactivity : AppCompatActivity() {
    var chkedPlant: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addpot)
        val intent = intent
        var potlist = intent.getIntExtra("listcount", 0)
        plantSelect.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.Strawberry -> {
                    chkedPlant = "Strawberry"
                }
                R.id.Lettuce -> {
                    chkedPlant = "Lettuce"
                }
                R.id.Rosemary -> {
                    chkedPlant = "Rosemary"
                }
                R.id.Geranium ->{
                    chkedPlant = "Geranium"
                }
            }
        }
        //request_code별로 구분해서 작업할 수 있다.
        addPotCancel.setOnClickListener{
            intent.putExtra("cancel","화분 추가를 취소")
            setResult(Activity.RESULT_CANCELED,intent)
            finish()
        }
        addPotOk.setOnClickListener {
            //화분이름이나 식물 종류가 비어있는 경우
            if(potname.text.toString().trim().isEmpty() || !Strawberry.isChecked && !Lettuce.isChecked && !Rosemary.isChecked && !Geranium.isChecked){
                Toast.makeText(this,"정보를 모두 입력해주세요",Toast.LENGTH_LONG).show()
            }else{
                potlist += 1
                intent.putExtra("potname",potname.text.toString())
                intent.putExtra("plantname",chkedPlant.toString())
                intent.putExtra("addok","추가됨 - 현재 화분 갯수 : $potlist")
                intent.putExtra("potlistcount",potlist)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        }
    }
}