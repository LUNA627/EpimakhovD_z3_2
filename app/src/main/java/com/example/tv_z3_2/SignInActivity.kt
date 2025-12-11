package com.example.tv_z3_2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class SignInActivity : Activity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_activity)


        val rView = findViewById<View>(android.R.id.content)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login_enter)

        login.setOnClickListener{
            if (email.text.isNullOrEmpty() || password.text.isNullOrEmpty()){
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()

            }
            else{
                val intent = Intent(this@SignInActivity, SearchActivity::class.java)
                startActivity(intent)
            }
        }
    }

}