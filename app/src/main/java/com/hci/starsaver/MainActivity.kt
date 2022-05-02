package com.hci.starsaver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hci.starsaver.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser==null){
            startActivity(Intent(this, LoginActivity::class.java))
        }else{
            startActivity(Intent(this, BottomNavigationActivity::class.java))
            finish()
        }
    }
}