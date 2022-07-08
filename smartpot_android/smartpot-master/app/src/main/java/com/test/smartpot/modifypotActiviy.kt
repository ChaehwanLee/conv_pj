package com.test.smartpot

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_modifypot_activiy.*

class modifypotActiviy : AppCompatActivity() {
    private var chkedPlant: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modifypot_activiy)
        val intent = intent
        modifypotname.hint = intent.getStringExtra("potname")
        modifyPotOk.setOnClickListener {
            if(modifypotname.text.isEmpty() || !ModifyStrawberry.isChecked && !ModifyLettuce.isChecked && !ModifyRosemary.isChecked && !ModifyGeranium.isChecked){
                Toast.makeText(this,"정보를 모두 입력해주세요",Toast.LENGTH_LONG).show()
            }else {
                intent.putExtra("potname", modifypotname.text.toString())
                intent.putExtra("plantname", chkedPlant.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        plantModifySelect.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.ModifyStrawberry -> {
                    chkedPlant = "Strawberry"
                }
                R.id.ModifyLettuce -> {
                    chkedPlant = "Lettuce"
                }
                R.id.ModifyRosemary -> {
                    chkedPlant = "Rosemary"
                }
                R.id.ModifyGeranium ->{
                    chkedPlant = "Geranium"
                }
            }
        }
        modifyPotCancel.setOnClickListener{
            intent.putExtra("cancel","화분 추가를 취소")
            setResult(Activity.RESULT_CANCELED,intent)
            finish()
        }
    }
}