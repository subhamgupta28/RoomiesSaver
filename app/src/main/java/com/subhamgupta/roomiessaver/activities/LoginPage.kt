package com.subhamgupta.roomiessaver.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.subhamgupta.roomiessaver.R
import com.subhamgupta.roomiessaver.utility.SettingsStorage
import android.app.Activity
import android.app.ActivityOptions
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.tasks.OnCompleteListener


class LoginPage : AppCompatActivity() {
    lateinit var mdc: MaterialAlertDialogBuilder
    lateinit var create_account: Button
    lateinit var login_btn: Button
    lateinit var forgot: Button
    lateinit var login_pop: MaterialCardView
    lateinit var signin_pop: MaterialCardView
    lateinit var tname: TextInputEditText
    lateinit var tpass: TextInputEditText
    lateinit var temail: TextInputEditText
    lateinit var cnf_pass: TextInputEditText
    lateinit var text: TextView
    lateinit var linearProgressIndicator: LinearProgressIndicator
    private lateinit var mAuth: FirebaseAuth
    lateinit var email_layout: TextInputLayout
    lateinit var name_layout: TextInputLayout
    lateinit var cnf_layout: TextInputLayout
    lateinit var pass_layout: TextInputLayout
    var user: FirebaseUser? = null
    lateinit var contextView: View
    lateinit var contactPopupView: View
    lateinit var ref: DatabaseReference
    lateinit var settingsStorage: SettingsStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        login_pop = findViewById(R.id.login_pop_btn)
        signin_pop = findViewById(R.id.signin_pop_btn)
        contextView = findViewById(android.R.id.content)
        settingsStorage = SettingsStorage(this)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")
        mdc = MaterialAlertDialogBuilder(this)

        login_pop.setOnClickListener { login_popup(0) }
        signin_pop.setOnClickListener { login_popup(1) }
        if (user != null)
            nextActivity(user)
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun login_popup(t: Int) {
        contactPopupView = layoutInflater.inflate(R.layout.signup_popup, null)
        temail = contactPopupView.findViewById(R.id.email)
        tname = contactPopupView.findViewById(R.id.name)
        tpass = contactPopupView.findViewById(R.id.pass)
        cnf_pass = contactPopupView.findViewById(R.id.cnf_pass)
        email_layout = contactPopupView.findViewById(R.id.email_layout)
        create_account = contactPopupView.findViewById(R.id.create_acc)
        login_btn = contactPopupView.findViewById(R.id.login_btn)
        cnf_layout = contactPopupView.findViewById(R.id.cnf_layout)
        forgot = contactPopupView.findViewById(R.id.forgot)
        name_layout = contactPopupView.findViewById(R.id.name_layout)
        text = contactPopupView.findViewById(R.id.text)
        pass_layout = contactPopupView.findViewById(R.id.pass_layout)
        linearProgressIndicator = contactPopupView.findViewById(R.id.progress)
        if (settingsStorage.email != "null")
            temail.setText(settingsStorage.email)

        if (t != 0)
            text.text = "Create Acount"
        else
            text.text = "Login"

        name_layout.visibility = if (t == 0) View.GONE else View.VISIBLE
        cnf_layout.visibility = if (t == 0) View.GONE else View.VISIBLE
        create_account.visibility = if (t == 0) View.GONE else View.VISIBLE
        login_btn.visibility = if (t == 0) View.VISIBLE else View.GONE
        forgot.visibility = if (t == 0) View.VISIBLE else View.GONE
        create_account.setOnClickListener {
            val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            startActivity(
                Intent(
                    applicationContext,
                    RoomCreation::class.java
                ),
                bundle
            )
        }
        forgot.setOnClickListener {
            resetPassword(temail.text.toString())
        }
        temail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (isValidEmail(editable.toString())) email_layout.error =
                    null else email_layout.error =
                    "Enter Valid Email"
            }
        })
        create_account.setOnClickListener {
            linearProgressIndicator.isIndeterminate = true
            linearProgressIndicator.visibility = View.VISIBLE
            hideKeyboard()
            disableButton()
            signInEmail()
//            Log.e("signin", "create")
        }
        login_btn.setOnClickListener {
            linearProgressIndicator.isIndeterminate = true
            linearProgressIndicator.visibility = View.VISIBLE
            hideKeyboard()
            disableButton()
            emailLogIn()
//            Log.e("login", "login")
        }
        mdc.background = ColorDrawable(Color.TRANSPARENT)
        mdc.setView(contactPopupView).show()

    }
    fun resetPassword(email: String) {
        if (!email.isEmpty()) {
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
//                        Log.d("LOGIN", "Email sent.")
                        showSnackBar("Password Change Email is sent to $email")
                    }
                }
        }
    }
    private fun disableButton(){
        login_btn.isEnabled = false
        create_account.isEnabled = false
        temail.isEnabled = false
        tpass.isEnabled = false
        tname.isEnabled = false
        name_layout.isEnabled = false
        pass_layout.isEnabled = false
    }
    private fun enableButton(){
        login_btn.isEnabled = true
        create_account.isEnabled = true
        temail.isEnabled = true
        tpass.isEnabled = true
        tname.isEnabled = true
        name_layout.isEnabled = true
        pass_layout.isEnabled = true
    }
    private fun emailLogIn() {
        val email = temail.text.toString()
        val pass = tpass.text.toString()
        hideKeyboard()
        if (email == "" || pass == "") {
            showSnackBar("Enter Credentials")
            linearProgressIndicator.visibility = View.INVISIBLE
            create_account.isEnabled = true
        } else {
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        linearProgressIndicator.visibility = View.GONE
                        val user = mAuth.currentUser!!
                        hideKeyboard()
                        if (!user.isEmailVerified) {
                            user.sendEmailVerification()
                            create_account.isEnabled = true
                            hideKeyboard()
                            settingsStorage.email = email
                            enableButton()
                            Snackbar.make(
                                contextView,
                                "Verification link is sent to $email verify to login",
                                Snackbar.LENGTH_LONG
                            )
                                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                .setDuration(10000)
                                .show()
                        } else {
                            hideKeyboard()
                            settingsStorage.email = email
                            nextActivity(user)
                            showSnackBar("Login Successful")
                            enableButton()
//                        updateNmae(sharedSession.getUsername(), mAuth);

                        }
                    } else {
                        hideKeyboard()
                        showSnackBar("Wrong email or password")
                        enableButton()
                        create_account.isEnabled = true
                        linearProgressIndicator.visibility = View.GONE
                    }
                }.addOnFailureListener { e: Exception ->
                Log.e("onfail", e.message!!)
                hideKeyboard()
                linearProgressIndicator.visibility = View.INVISIBLE
                    enableButton()
                email_layout.error = "Wrong Password or Email"
            }
        }
    }

    private fun signInEmail() {
        hideKeyboard()
        val email = temail.text.toString()
        val pass = tpass.text.toString()
        val name = tname.text.toString()
        if (email == "" || pass == "" || name == "") {
            showSnackBar("Enter Credentials")
            linearProgressIndicator.visibility = View.INVISIBLE
           enableButton()
        } else {
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task: Task<AuthResult> ->
                    linearProgressIndicator.visibility = View.GONE
                    if (task.isComplete) {
                        hideKeyboard()
                        showSnackBar("Successfully Account Created")
                        settingsStorage.username = name
                        settingsStorage.email = email
                        settingsStorage.isRoom_joined = false
                        try {
                            saveUser(name, email, task.result!!.user)
                            emailLogIn()
                        } catch (e: Exception) {
                            hideKeyboard()
                            enableButton()
                            Log.e("ERROR_LOGIN_PAGE", e.message!!)
                        }
                    } else {
                        enableButton()
                        hideKeyboard()
                        showSnackBar("Something Went Wrong..")
                    }
                }
        }
    }

    private fun saveUser(name: String, email: String, u: FirebaseUser?) {
        ref.child(u!!.uid).child("USER_NAME").setValue(name)
        ref.child(u.uid).child("USER_EMAIL").setValue(email)
            .addOnSuccessListener { unused: Void? -> settingsStorage.username = name }
            .addOnFailureListener { e: Exception ->
                Log.e("ERROR", e.message!!)
                hideKeyboard()
            }
    }
    override fun onStop() {
        super.onStop()
        supportFinishAfterTransition()
    }
    private fun nextActivity(user: FirebaseUser?) {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        try {
            if (user != null) {
//              if (user.isEmailVerified)
                    ref.child(user.uid).child("IS_ROOM_JOINED").get()
                        .addOnCompleteListener { task: Task<DataSnapshot> ->
                            if (task.isSuccessful) {
                                try {
                                    val b = task.result!!.value as Boolean
                                    if (b) {
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                MainActivity::class.java
                                            ),
                                            bundle
                                        )
                                    } else {
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                RoomCreation::class.java
                                            ),
                                            bundle
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("ERROR", e.message!!)

                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            RoomCreation::class.java
                                        ),bundle
                                    )
                                }
                            }
                        }
//                } else {
//                    user.sendEmailVerification()
//                    hideKeyboard()
//                    enableButton()
//                    showSnackBar("Verification link is sent to your email, verify to login")
//                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
    }

    private fun hideKeyboard() {

        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(contextView, msg, Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

}