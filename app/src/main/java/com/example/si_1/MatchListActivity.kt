package com.example.si_1

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MatchListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvTitle: TextView
    private lateinit var btnGoBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        val selectedCourse = intent.getStringExtra("course") ?: return finish()
        val selectedDate = intent.getStringExtra("date") ?: return finish()
        val selectedTime = intent.getStringExtra("time") ?: return finish()

        tvTitle = findViewById(R.id.tvTitle)
        listView = findViewById(R.id.listViewMatches)
        btnGoBack = findViewById(R.id.btnGoBack)

        tvTitle.text = "매칭된 런너 목록 - $selectedCourse / $selectedDate / $selectedTime"

        val dbRef = FirebaseDatabase
            .getInstance("https://mobileprograming-18e4c-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("applications")


        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchedEmails = mutableListOf<String>()

                for (child in snapshot.children) {
                    val course = child.child("course").getValue(String::class.java)
                    val date = child.child("date").getValue(String::class.java)
                    val time = child.child("time").getValue(String::class.java)
                    val userEmail = child.child("userEmail").getValue(String::class.java)

                    Log.d("MatchDebug", "읽은 신청 정보: course=$course, date=$date, time=$time, userEmail=$userEmail")

                    if (course == selectedCourse && date == selectedDate && time == selectedTime) {
                        matchedEmails.add(userEmail ?: "Unknown")
                    }
                }

                val displayList = if (matchedEmails.isEmpty())
                    listOf("신청자가 없습니다.")
                else
                    matchedEmails

                listView.adapter = ArrayAdapter(
                    this@MatchListActivity,
                    android.R.layout.simple_list_item_1,
                    displayList
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MatchListActivity, "조회 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        btnGoBack.setOnClickListener {
            finish()
        }
    }
}
