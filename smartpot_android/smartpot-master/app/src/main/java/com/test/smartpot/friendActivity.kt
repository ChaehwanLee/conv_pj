package com.test.smartpot

import android.os.Bundle
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_friend.*


class friendActivity : AppCompatActivity() {

    val UserList = arrayListOf<User>(
        User(R.drawable.plant_pot, "친구1", "30", "어서오세요"),
        User(R.drawable.plant_pot, "친구2", "20", "어서오세요"),
        User(R.drawable.plant_pot, "친구3", "40", "어서오세요"),
        User(R.drawable.plant_pot, "친구4", "10", "어서오세요"),
        User(R.drawable.plant_pot, "친구5", "60", "어서오세요"),
        User(R.drawable.plant_pot, "친구6", "70", "어서오세요"),
        User(R.drawable.plant_pot, "친구7", "80", "어서오세요"),

        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)


        // list view 리스트뷰
        val Adapter = UserAdapter(this, UserList)
        listView.adapter = Adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectItem = parent.getItemAtPosition(position) as User
                Toast.makeText(this, selectItem.name, Toast.LENGTH_SHORT).show()
            }
    }
}