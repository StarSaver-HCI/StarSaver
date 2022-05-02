package com.hci.starsaver.ui.login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hci.starsaver.databinding.ActivityLoginBinding
import com.hci.starsaver.ui.home.PathName.Companion.HOME
import com.hci.starsaver.ui.home.PathName.Companion.IS_FOLDER
import com.hci.starsaver.ui.home.PathName.Companion.TITLE
import com.hci.starsaver.ui.home.PathName.Companion.USERS
import com.hci.starsaver.ui.home.PathName.Companion.USER_ID
import com.hci.starsaver.ui.home.PathName.Companion.USER_NAME

class LoginActivity:AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAuth()
        initGuestLoginButton()
        initGoogleLoginButton()
    }

    private fun initAuth() {
        auth = Firebase.auth
    }

    private fun initGoogleLoginButton() {

    }

    private fun initGuestLoginButton() {
        binding.loginGuestButton.setOnClickListener { auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleSuccessLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "로그인 실패",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleSuccessLogin(){
        if(auth.currentUser==null){
            Toast.makeText(this, "로그인 실패",Toast.LENGTH_SHORT).show()
            return
        }
//        val userId = auth.currentUser?.uid.orEmpty()
//        // Firebase.database.reference = 루트
//        val currentUserDB = Firebase.database.reference.child(USERS).child(userId)
//        val user = mutableMapOf<String, Any>()
//        user[USER_ID] = userId
//        user[USER_NAME] = "Guest"
//        currentUserDB.updateChildren(user)
//        currentUserDB.child(HOME).child(IS_FOLDER).setValue(true)
//        currentUserDB.child(HOME).child(TITLE).setValue(HOME)
        finish()
    }
}