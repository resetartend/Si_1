package com.example.si_1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class MatchActivity : AppCompatActivity() {
    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var selectedCourse: String? = null
    private lateinit var dateContainer: LinearLayout
    private lateinit var selectedInfo: TextView
    private var selectedDate: String? = null
    private var selectedTime: String? = null

    companion object {
        val applications = mutableListOf<ApplicationInfo>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        dateContainer = findViewById(R.id.dateContainer)
        selectedInfo = findViewById(R.id.tvSelectedInfo)

        val btnApply = findViewById<Button>(R.id.btnApply)
        val btnViewMatches = findViewById<Button>(R.id.btnViewMatches)

        setupDateButtons()
        setupTimeButtons()

        val allCourses = listOf(
            Course("코스1.거북섬 시화호뷰/3.5km", R.drawable.course1),
            Course("코스2.어린왕자 포토존/4km", R.drawable.course2),
            Course("코스3.한울공원/3.6km", R.drawable.course3),
            Course("코스4.옥구공원/3.1km", R.drawable.course4),
            Course("코스5.정왕동 포레스트뷰/4km", R.drawable.course5),
            Course("코스6.정왕동 중앙공원/3km", R.drawable.course6)
        )

        val recyclerCourses = findViewById<RecyclerView>(R.id.recyclerCourses)
        recyclerCourses.layoutManager = GridLayoutManager(this, 1)
        recyclerCourses.adapter = CourseAdapter(allCourses) { selectedCourseObj ->
            selectedCourse = selectedCourseObj.name
            updateSelectedInfo()
        }

        btnApply.setOnClickListener {
            if (selectedCourse == null || selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "코스, 날짜, 시간을 모두 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = auth.currentUser?.email ?: "guest@unknown.com"
            val appInfo = ApplicationInfo(
                userEmail = userEmail,
                course = selectedCourse!!,
                date = selectedDate!!,
                time = selectedTime!!
            )

            // companion object 리스트에 추가
            applications.add(appInfo)

            // Realtime Database 저장
            val dbRef = FirebaseDatabase
                .getInstance("https://mobileprograming-18e4c-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .reference
            val key = dbRef.child("applications").push().key ?: return@setOnClickListener

            dbRef.child("applications").child(key).setValue(appInfo)
                .addOnSuccessListener {
                    Toast.makeText(this, "신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "신청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnViewMatches.setOnClickListener {
            if (selectedCourse == null || selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "코스, 날짜, 시간을 모두 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, MatchListActivity::class.java).apply {
                putExtra("course", selectedCourse)
                putExtra("date", selectedDate)
                putExtra("time", selectedTime)
            }
            startActivity(intent)
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_match -> {
                    Toast.makeText(this, "매칭 서비스 화면입니다.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_my -> {
                    startActivity(Intent(this, MyActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_match
    }

    private fun setupDateButtons() {
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN) // <-- 여기 수정

        for (i in 0 until 7) {
            val date = today.time
            val formattedDate = dateFormat.format(date)

            val btn = Button(this).apply {
                text = formattedDate
                setOnClickListener {
                    selectedDate = text.toString() // 저장되는 selectedDate도 동일 포맷
                    updateSelectedInfo()
                }
            }

            dateContainer.addView(btn)
            today.add(Calendar.DATE, 1)
        }
    }

    private fun setupTimeButtons() {
        val btnMorning = findViewById<Button>(R.id.btnMorning)
        val btnNoon = findViewById<Button>(R.id.btnNoon)
        val btnEvening = findViewById<Button>(R.id.btnEvening)

        btnMorning.setOnClickListener {
            selectedTime = "오전 7시"
            updateSelectedInfo()
        }
        btnNoon.setOnClickListener {
            selectedTime = "오후 12시"
            updateSelectedInfo()
        }
        btnEvening.setOnClickListener {
            selectedTime = "오후 8시"
            updateSelectedInfo()
        }
    }

    private fun updateSelectedInfo() {
        val course = selectedCourse ?: "(코스 선택 안됨)"
        val date = selectedDate ?: "(날짜 선택 안됨)"
        val time = selectedTime ?: "(시간 선택 안됨)"
        selectedInfo.text = "선택한 예약: $course / $date / $time"
    }
}
