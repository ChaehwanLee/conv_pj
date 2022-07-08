package com.test.smartpot

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pot_list.view.*

class PotListAdapter(var context: Context, var itemLayout:Int, var datalist:ArrayList<PotList>, var imagelist:ArrayList<Int>)
    :RecyclerView.Adapter<PotListAdapter.MyViewHolder>(){
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textview = itemView.potview_info
        val imageView = itemView.imageView1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(itemLayout,null)
        val myViewHolder = MyViewHolder(itemView)
        return myViewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var myTextView = holder.textview
        var myImageVIew = holder.imageView
        myTextView.text = datalist.get(position).title
        myImageVIew.setImageResource(imagelist[position])
        Log.d("test", "onBindViewHolder")
        myTextView.setOnClickListener {
            when (position) {
                0 -> {
                    val intent = Intent(context, pot1Activity::class.java).apply {
                        putExtra("potname","${myTextView.text}")
                        putExtra("plantname","${datalist.get(position).plant}")
                    }
                    ContextCompat.startActivity(context, intent, null)
                }
                1->{
                    val intent2 = Intent(context, pot2Activity::class.java)
                    ContextCompat.startActivity(context, intent2, null)
                }
            }
        }
        myImageVIew.setOnClickListener {
            when (position) {
                0 -> {
                    val intent = Intent(context, pot1Activity::class.java).apply {
                        putExtra("potname","${myTextView.text}")
                        putExtra("plantname","${datalist.get(position).plant}")
                    }
                    ContextCompat.startActivity(context, intent, null)
                }
                1->{
                    val intent2 = Intent(context, pot2Activity::class.java)
                    ContextCompat.startActivity(context, intent2, null)
                }
            }
        }
    }



    override fun getItemCount(): Int {
        return datalist.size
    }
}