package com.example.si_1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri

class MyActivity : AppCompatActivity() {

    private lateinit var userNameEditText: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var runCountTextView: TextView
    private lateinit var totalDistanceTextView: TextView
    private lateinit var todayDistanceTextView: TextView
    private lateinit var monthDistanceTextView: TextView
    private lateinit var bestRecordTextView: TextView
    private lateinit var profileImageView: ImageView
    private val IMAGE_PICK_CODE = 1001
    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my)

        // UI 요소 초기화
        userNameEditText = findViewById(R.id.userNameEditText)
        saveProfileButton = findViewById(R.id.saveProfileButton)
        runCountTextView = findViewById(R.id.runCountTextView)
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView)
        todayDistanceTextView = findViewById(R.id.todayDistanceTextView)
        monthDistanceTextView = findViewById(R.id.monthDistanceTextView)
        bestRecordTextView = findViewById(R.id.bestRecordTextView)
        profileImageView = findViewById(R.id.profileImageView)

        // SharedPreferences에서 이름과 이미지 URI 불러오기
        loadProfileData()

        // 프로필 이미지 선택 이벤트
        profileImageView.setOnClickListener {
            if (isStoragePermissionGranted()) {
                openImagePicker()
            } else {
                showPermissionExplanationDialog()
            }
        }

        // 프로필 저장 버튼 클릭 시
        saveProfileButton.setOnClickListener {
            val newName = userNameEditText.text.toString().trim()
            val imageUriString = profileImageView.tag as? String

            if (newName.isNotEmpty() || imageUriString != null) {
                val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                // 이름과 이미지 URI 모두 저장
                prefs.edit {
                    if (newName.isNotEmpty()) putString("userName", newName)
                    if (imageUriString != null) putString("profileImageUri", imageUriString)
                }

                // 로그캣에 URI 값 출력
                if (imageUriString != null) {
                    Log.d("ProfileImage", "Saved Image URI: $imageUriString")
                }

                Toast.makeText(this, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "변경된 내용이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 러닝 통계 불러오기
        val stats = RunningStatsManager.getStats(this)
        val runCount = stats["runCount"] as Int
        val totalDistance = stats["totalDistance"] as Float
        val todayDistance = stats["todayDistance"] as Float
        val monthDistance = stats["monthDistance"] as Float
        val bestRecord = stats["bestRecord"] as Float

        runCountTextView.text = "🏃‍♂️ 달린 횟수: $runCount 회"
        totalDistanceTextView.text = "📏 총 거리: %.2f km".format(totalDistance)
        todayDistanceTextView.text = "오늘 뛴 거리: %.2f km".format(todayDistance)
        monthDistanceTextView.text = "이번 달 뛴 거리: %.2f km".format(monthDistance)
        bestRecordTextView.text = "🏆 한달 최고 기록  %.2f KM".format(bestRecord)

        // 하단 네비게이션 바 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_match -> {
                    startActivity(Intent(this, MatchActivity::class.java))
                    true
                }
                R.id.nav_my -> {
                    Toast.makeText(this, "내 정보 화면입니다.", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_my
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Toast.makeText(this, "권한을 거부하셨습니다. 앱 설정에서 권한을 수동으로 허용해주세요.", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "권한을 허용해야 프로필 이미지를 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 권한 요청 전에 설명을 띄워주는 Dialog
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("프로필 이미지 권한")
            .setMessage("프로필 이미지를 선택하려면 저장소 권한을 허용해야 합니다. 권한을 허용해 주세요.")
            .setPositiveButton("허용") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // MANAGE_EXTERNAL_STORAGE 권한을 확인하는 함수
    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 이미지 선택 인텐트 실행
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"  // 이미지 선택만 가능하도록 설정
        }
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // 선택된 이미지 처리 및 URI 저장
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null && isValidUri(imageUri)) {
                profileImageView.setImageURI(imageUri) // 선택한 이미지를 ImageView에 설정
                profileImageView.tag = imageUri.toString() // 이미지 URI를 tag로 저장

                // 선택된 이미지 URI를 로그에 출력
                Log.d("ProfileImage", "Selected Image URI: $imageUri")
            } else {
                profileImageView.setImageResource(R.drawable.ic_person)
                profileImageView.tag = null
            }
        }
    }

    // URI 유효성 확인 함수
    private fun isValidUri(uri: Uri): Boolean {
        try {
            if (uri.scheme == "file") {
                val file = File(uri.path)
                return file.exists() && file.isFile
            } else if (uri.scheme == "content") {
                val contentResolver = contentResolver
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        val filePath = it.getString(columnIndex)
                        val file = File(filePath)
                        return file.exists() && file.isFile
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // 저장된 프로필 데이터 불러오는 함수
    private fun loadProfileData() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("userName", "이름을 입력하세요.")
        userNameEditText.setText(savedName)

        val imageUriString = prefs.getString("profileImageUri", null)
        if (imageUriString != null) {
            try {
                val imageUri = imageUriString.toUri()
                if (isValidUri(imageUri)) {
                    profileImageView.setImageURI(imageUri)
                    profileImageView.tag = imageUriString // 이미지 URI를 tag로 저장

                    // 저장된 URI를 로그에 출력
                    Log.d("ProfileImage", "Loaded Image URI: $imageUriString")
                } else {
                    profileImageView.setImageResource(R.drawable.ic_person)
                }
            } catch (_: Exception) {
                this.profileImageView.setImageResource(R.drawable.ic_person)
            }
        } else profileImageView.setImageResource(R.drawable.ic_person)
    }
}
