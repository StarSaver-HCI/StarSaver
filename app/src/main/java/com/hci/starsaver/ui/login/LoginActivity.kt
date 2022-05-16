package com.hci.starsaver.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hci.starsaver.R
import com.hci.starsaver.databinding.ActivityLoginBinding


class LoginActivity:AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    lateinit var auth:FirebaseAuth
    lateinit var googleSignInClient:GoogleSignInClient
    private val RC_SIGN_IN = 9001

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
        getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginGoogleButton.setOnClickListener {
            googleSignIn()
        }
    }

    private fun initGuestLoginButton() {
        binding.loginGuestButton.setOnClickListener { auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, LoadingActivity::class.java)
                    startActivity(intent)
                    handleSuccessLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "로그인 실패",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleSuccessLogin(){
        if(auth.currentUser==null){
            Toast.makeText(this, "로그인 실패",Toast.LENGTH_SHORT).show()
            return
        }
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    } // onActivityResult End

    // firebaseAuthWithGoogle
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + acct.id!!)

        //Google SignInAccount 객체에서 ID 토큰을 가져와서 Firebase Auth로 교환하고 Firebase에 인증
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, LoadingActivity::class.java)
                    startActivity(intent)
                    handleSuccessLogin()
                } else {
                    Snackbar.make(binding.root, "로그인에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                }
            }
    }
}