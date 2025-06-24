package com.example.si_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var weatherTextView: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var kakaoMapView: WebView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var endButton: Button
    private lateinit var courseSelector: Spinner
    private lateinit var timeTextView: TextView

    private var isRunning = false
    private var isPaused = false
    private var elapsedTime = 0 // 초 단위
    private val handler = Handler(Looper.getMainLooper())

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var selectedCourse: String = "코스1"

    private val courseDistances = mapOf(
        "코스1.거북섬 시화호뷰/3.5km" to 3.5,
        "코스2.어린왕자 포토존/4km" to 4.0,
        "코스3.한울공원/3.6km" to 3.6,
        "코스4.옥구공원/3.1km" to 3.1,
        "코스5.정왕동 포레스트뷰/4km" to 4.0,
        "코스6.정왕동 중앙공원/3km" to 3.0
    )

    private val mainViewModel: MainViewModel by viewModels()

    // 타이머 관련 변수
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning && !isPaused) {
                elapsedTime++
                val hours = elapsedTime / 3600
                val minutes = (elapsedTime % 3600) / 60
                val seconds = elapsedTime % 60
                timeTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds) // ⬅️ HH:MM:SS 형식
            }
            if (isRunning) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupCourseSelector()
        setupWebView()
        observeViewModel()
        requestLocationPermission()
        setupBottomNavigationView()
        setupRunButtons()

        // Intent로 전달된 코스 정보 받기
        val selectedCourse = intent.getStringExtra("selectedCourse")
        if (selectedCourse != null) {
            updateSelectedCourseInWebView(selectedCourse)
            courseSelector.setSelection(getCourseIndex(selectedCourse))
        }
    }

    private fun initViews() {
        weatherTextView = findViewById(R.id.weatherTextView)
        weatherIcon = findViewById(R.id.weatherIcon)
        kakaoMapView = findViewById(R.id.kakaoMapView)
        startButton = findViewById(R.id.startButton)
        pauseButton = findViewById(R.id.stopButton)
        endButton = findViewById(R.id.endButton)
        courseSelector = findViewById(R.id.courseSelector)
        timeTextView = findViewById(R.id.timeTextView)
    }

    private fun setupCourseSelector() {
        val courseNames = listOf("코스를 선택하세요", "코스1.거북섬 시화호뷰/3.5km", "코스2.어린왕자 포토존/4km", "코스3.한울공원/3.6km", "코스4.옥구공원/3.1km", "코스5.정왕동 포레스트뷰/4km", "코스6.정왕동 중앙공원/3km")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSelector.adapter = adapter
        courseSelector.setSelection(0)  // 기본값으로 "코스를 선택하세요"

        // SharedPreferences에서 선택된 코스를 불러오기
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedCourse = prefs.getString("selectedCourse", null)
        savedCourse?.let {
            courseSelector.setSelection(courseNames.indexOf(it))
        }

        courseSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCourse = parent.getItemAtPosition(position) as String
                updateSelectedCourseInWebView(selectedCourse)
                // 선택된 코스를 SharedPreferences에 저장
                val editor = prefs.edit()
                editor.putString("selectedCourse", selectedCourse)
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateSelectedCourseInWebView(course: String) {
        val jsCode = when (course) {
            "코스1.거북섬 시화호뷰/3.5km" -> "setCourse1()"
            "코스2.어린왕자 포토존/4km" -> "setCourse2()"
            "코스3.한울공원/3.6km" -> "setCourse3()"
            "코스4.옥구공원/3.1km" -> "setCourse4()"
            "코스5.정왕동 포레스트뷰/4km" -> "setCourse5()"
            "코스6.정왕동 중앙공원/3km" -> "setCourse6()"
            else -> ""
        }
        kakaoMapView.evaluateJavascript("javascript:$jsCode", null)
    }

    private fun getCourseIndex(course: String): Int {
        val courseNames = listOf("코스를 선택하세요", "코스1.거북섬 시화호뷰/3.5km", "코스2.어린왕자 포토존/4km", "코스3.한울공원/3.6km", "코스4.옥구공원/3.1km", "코스5.정왕동 포레스트뷰/4km", "코스6.정왕동 중앙공원/3km")
        return courseNames.indexOf(course)
    }

    private fun setupRunButtons() {
        startButton.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                isPaused = false
                elapsedTime = 0 // ⬅️ 타이머 0부터 시작
                timeTextView.text = "00:00:00" // ⬅️ 시간초 형식 세팅
                startTimer()
                Toast.makeText(this, "러닝 시작!", Toast.LENGTH_SHORT).show()
            }
        }

        pauseButton.setOnClickListener {
            if (isRunning) {
                isPaused = !isPaused
                val msg = if (isPaused) "일시정지!" else "재시작!"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        endButton.setOnClickListener {
            if (isRunning) {
                isRunning = false
                isPaused = false
                handler.removeCallbacks(timerRunnable)
                val distance = courseDistances[selectedCourse] ?: 0.0
                RunningStatsManager.saveRun(this, distance)
                Toast.makeText(this, "러닝 종료! %.1f km 저장됨".format(distance), Toast.LENGTH_SHORT).show()
                timeTextView.text = "00:00:00" // ⬅️ 종료 시 리셋
                elapsedTime = 0
            } else {
                Toast.makeText(this, "먼저 러닝을 시작하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startTimer() {
        handler.post(timerRunnable)
    }

    private fun setupWebView() {
        kakaoMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        kakaoMapView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("MainActivity", "Map HTML 로드 완료.")
            }
        }
        kakaoMapView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, true)
            }
        }

        with(kakaoMapView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
    }

    private fun observeViewModel() {
        mainViewModel.weatherData.observe(this, Observer { weatherText ->
            weatherTextView.text = weatherText
        })
        mainViewModel.fetchWeather()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            loadKakaoMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            loadKakaoMap()
        }
    }

    private fun loadKakaoMap() {
        kakaoMapView.loadUrl("file:///android_asset/kakaomap.html")
    }

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> {
                    Toast.makeText(this, "메인화면입니다.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_match -> {
                    startActivity(Intent(this, MatchActivity::class.java))
                    true
                }
                R.id.nav_my -> {
                    startActivity(Intent(this, MyActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
