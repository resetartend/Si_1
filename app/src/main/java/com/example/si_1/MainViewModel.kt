package com.example.si_1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.graphics.Color

class MainViewModel : ViewModel() {

    // LiveData를 사용하여 UI에 날씨 데이터를 안전하게 전달
    private val _weatherData = MutableLiveData<CharSequence>()
    val weatherData: LiveData<CharSequence> = _weatherData

    fun fetchWeather() {
        val serviceKey = "yKNIYSaI78Tg99WBiJW6ntOTN%2BUwbURst0wCfTF8L6iRDAmzBAmUFR6mr%2FTZVN1t%2BFo90yhJ4%2BTSCSnM2SEAUQ%3D%3D"
        val baseDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date())
        val baseTime = "0500"
        val nx = 55 // 예시 좌표 (시흥 지역)
        val ny = 124 // 예시 좌표

        val weatherUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst" +
                "?serviceKey=$serviceKey" +
                "&numOfRows=100&pageNo=1&dataType=JSON&base_date=$baseDate&base_time=$baseTime&nx=$nx&ny=$ny"

        // viewModelScope를 사용하여 ViewModel의 생명주기에 맞춰 코루틴 관리
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val weatherRequest = Request.Builder().url(weatherUrl).build()
                val weatherResponse = client.newCall(weatherRequest).execute()
                val weatherBody = weatherResponse.body?.string()

                if (weatherBody.isNullOrEmpty()) {
                    _weatherData.postValue("날씨 정보를 받아오지 못했습니다.")
                    return@launch
                }

                val json = JSONObject(weatherBody)
                val items = json.getJSONObject("response").getJSONObject("body")
                    .getJSONObject("items").getJSONArray("item")

                var temp = ""
                var sky = ""
                var rainType = ""

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val category = item.getString("category")
                    val fcstValue = item.getString("fcstValue")

                    when (category) {
                        "TMP" -> temp = fcstValue
                        "SKY" -> sky = when (fcstValue) {
                            "1" -> "맑음"
                            "3" -> "구름 많음"
                            "4" -> "흐림"
                            else -> "알 수 없음"
                        }
                        "PTY" -> rainType = when (fcstValue) {
                            "0" -> "없음"
                            "1" -> "비"
                            "2" -> "비/눈"
                            "3" -> "눈"
                            "4" -> "소나기"
                            else -> "없음"
                        }
                    }
                }

                // 색상 지정 로직
                val tempInt = temp.toIntOrNull() ?: 0
                val tempColor = when {
                    tempInt <= 20 -> "#0000FF" // 파랑
                    tempInt <= 27 -> "#FFD700" // 노랑
                    else -> "#FF0000" // 빨강
                }

                val builder = SpannableStringBuilder()
                builder.append("📍 시흥 날씨\n")

                // 🌡 기온 색상 지정
                builder.append("🌡 기온: ")
                val start = builder.length
                builder.append("${tempInt}℃")
                val end = builder.length
                builder.setSpan(
                    ForegroundColorSpan(Color.parseColor(tempColor)),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // 선크림 경고
                if (tempInt >= 27) {
                    builder.append(" 🔥 선크림 필수!")
                    val warnStart = builder.length - "선크림 필수!".length
                    builder.setSpan(
                        ForegroundColorSpan(Color.RED),
                        warnStart,
                        builder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                builder.append("\n☁️ 상태: $sky, $rainType")

                _weatherData.postValue(builder)

            } catch (e: Exception) {
                Log.e("MainViewModel", "날씨 정보 불러오기 실패", e)
                _weatherData.postValue("날씨 정보를 불러오지 못했습니다.")
            }
        }
    }
}