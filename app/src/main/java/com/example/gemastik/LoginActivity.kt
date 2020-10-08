package com.example.gemastik

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity: AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        mAuth = FirebaseAuth.getInstance()
        initEt()
    }

    override fun onStart() {
        super.onStart()

        loadMainActivity()
    }

    private fun loadMainActivity(){
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun btnLogin(view: View) {
        if (etEmail!!.text.isNotBlank() && etPassword!!.text.isNotBlank()){
            loginFirebase(etEmail!!.text.toString(), etPassword!!.text.toString())
        }else{
            Toast.makeText(this, "Tolong lengkapi email dan password", Toast.LENGTH_SHORT).show()
        }
    }

    fun btnRegister(view: View) {
        if(etEmail!!.text.isNotBlank() && etPassword!!.text.isNotBlank()){
//            btn.startAnimation()
            registerFirebase(etEmail!!.text.toString(), etPassword!!.text.toString())
        }else{
            Toast.makeText(this, "Tolong lengkapi email dan password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initEt(){
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
    }

    private fun loginFirebase(email: String, password: String){
        // signInWithEmailAndPassword itu fungsi dari firebase, buat daftarin
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    loadMainActivity()
                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
//                    btn.revertAnimation()
                }
            }
    }

    private fun registerFirebase(email: String , password: String) {
        // createUserWithEmailAndPassword itu fungsi yg udh ada dari Firebase
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    loadMainActivity()
                } else {
                    Toast.makeText(applicationContext, "Register gagal", Toast.LENGTH_LONG).show()
//                    btn.revertAnimation()
                }
            }
    }
}