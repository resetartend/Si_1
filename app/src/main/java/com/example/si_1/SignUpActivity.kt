package com.example.si_1

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.si_1.model.User
import com.example.si_1.firestore.FirestoreManager
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val emailEdit = findViewById<EditText>(R.id.emailEditText)
        val pwEdit = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)


        registerButton.setOnClickListener {
            val email = emailEdit.text.toString()
            val pw = pwEdit.text.toString()


            if (email.isNotEmpty() && pw.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: ""
                        val user = User(uid, email, isAdmin = false)
                        FirestoreManager.saveUser(user) {
                            Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
