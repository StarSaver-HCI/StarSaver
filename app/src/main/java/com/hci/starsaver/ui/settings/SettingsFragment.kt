package com.hci.starsaver.ui.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import com.hci.starsaver.MainActivity
import com.hci.starsaver.R
import com.hci.starsaver.databinding.DialogAddLinkBinding
import com.hci.starsaver.databinding.DialogNotificationBinding
import com.hci.starsaver.databinding.FragmentSettingsBinding
import com.hci.starsaver.ui.login.LoadingActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var account : GoogleSignInAccount? = null
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient:GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        initAuth()
        initBackButton()
        initLogOutButton()
        initAccountDeleteButton()
        initAccountButton()
        initNotiImageView()
        return binding.root
    }

    private fun initNotiImageView() {
        binding.notiImageView.setOnClickListener {
            showPopup()
        }
    }

    private fun initAccountButton() {
        getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        binding.emailConnectedTextView.setOnClickListener {
            auth.signOut()
            googleSignIn()
        }
        if(account !== null){
            binding.emailConnectedTextView.text = "이메일"
            binding.emailConnectedTextView.isEnabled = true
            binding.googleImageView.visibility = View.VISIBLE
            binding.emailTextView.visibility = View.VISIBLE
            binding.emailTextView.text = account!!.email
            binding.notiImageView.visibility = View.GONE
            binding.backUpButton.isEnabled = true
            binding.backUpButton.setTextColor(Color.BLACK)
            binding.dataUploadButton.isEnabled = true
            binding.dataUploadButton.setTextColor(Color.BLACK)

        }else{
            binding.emailConnectedTextView.text = "이메일 연동하기"
            binding.googleImageView.visibility = View.GONE
            binding.emailTextView.visibility = View.GONE
            binding.notiImageView.visibility = View.VISIBLE
            binding.backUpButton.isEnabled = false
            binding.backUpButton.setTextColor(Color.LTGRAY)
            binding.dataUploadButton.isEnabled = false
            binding.dataUploadButton.setTextColor(Color.LTGRAY)
            binding.emailButton.isEnabled=false
            binding.emailConnectedTextView.isEnabled = true
        }
    }

    private fun initAccountDeleteButton() {
        binding.accountDeleteButton.setOnClickListener {
            logOut()
        }
    }

    private fun initBackButton(){
        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }
    }

    private fun initAuth() {
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        account = GoogleSignIn.getLastSignedInAccount(requireActivity())
    }

    private fun initLogOutButton() {
        binding.logoutButton.setOnClickListener {
            logOut()
        }
    }

    private fun logOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {}
        val intent = Intent(this.context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showPopup() {
        var addLinkDialog = Dialog(requireContext())
        var dialogView = DialogNotificationBinding.inflate(layoutInflater).root
        addLinkDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addLinkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addLinkDialog.setContentView(dialogView)
        addLinkDialog.show()
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    initAuth()
                    initAccountButton()
                } else {
                    Snackbar.make(binding.root, "로그인에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                }
            }
    }
}