package com.subhamgupta.roomiesapp.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.subhamgupta.roomiesapp.R
import com.subhamgupta.roomiesapp.fragments.LoginPage
import com.subhamgupta.roomiesapp.fragments.RoomCreation
import com.subhamgupta.roomiesapp.onItemClick
import com.subhamgupta.roomiesapp.utility.SettingsStorage

class AccountCreation : AppCompatActivity(), onItemClick {
    lateinit var login_pop: MaterialCardView
    lateinit var signin_pop: MaterialCardView
    private lateinit var mAuth: FirebaseAuth
    lateinit var linearLayout: LinearLayout
    var uuid: String?=null
    var user: FirebaseUser? = null
    lateinit var contextView: View
    lateinit var ref: DatabaseReference
    lateinit var settingsStorage: SettingsStorage
    lateinit var roomCreation: RoomCreation
    lateinit var loginPage: LoginPage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_creation)
        login_pop = findViewById(R.id.login_pop_btn)
        signin_pop = findViewById(R.id.signin_pop_btn)
        contextView = findViewById(android.R.id.content)
        linearLayout = findViewById(R.id.line1)
        settingsStorage = SettingsStorage(this)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser
        ref = FirebaseDatabase.getInstance().reference.child("ROOMIES")

        roomCreation = RoomCreation(mAuth, ref, settingsStorage, contextView, this)
        try {
            val code = intent.extras?.get("CODE").toString().toInt()
            Log.e("CODE","$code")
            if (code==0){
                setLogin()
            }
            if (code==1){
                setRoom()
            }
        }catch (e:Exception){

        }
        login_pop.setOnClickListener {
           setLogin()
        }
        signin_pop.setOnClickListener {
            setSignIn()
        }

        if (user != null)
            nextActivity(user)
    }
    override fun loginComplete(user: FirebaseUser) {
        nextActivity(user)
    }

    override fun roomCreated() {
        nextActivity(user)
    }

    override fun logout() {
        linearLayout.visibility = View.VISIBLE
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    override fun signInComplete() {
        setLogin()
    }

    private fun setRoom(){

        setUpFrag(roomCreation)
    }
    private fun setSignIn(){
        loginPage = LoginPage(mAuth, ref, settingsStorage,false, contextView, this)
        setUpFrag(loginPage)
    }
    private fun setLogin(){
        loginPage = LoginPage(mAuth, ref, settingsStorage,true, contextView, this)
        setUpFrag(loginPage)
    }
    private fun setUpFrag(fragment: Fragment){
        linearLayout.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.acview, fragment)
            .commit()
    }
    override fun onStop() {
        super.onStop()
        supportFinishAfterTransition()
    }
    private fun goToMain() {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ),
            bundle
        )
    }
    private fun goToRoom(){
        setRoom()
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
    private fun nextActivity(user: FirebaseUser?) {
        Log.e("USER", user!!.uid)
        hideKeyboard()
        try {
            ref.child(user.uid).child("IS_ROOM_JOINED").get()
                .addOnCompleteListener(this) { task: Task<DataSnapshot> ->
                    if (task.isSuccessful) {
                        try {
                            Log.e("USER", "11")
                            val b = task.result!!.value as Boolean
                            if (b) {
                                goToMain()
                                Log.e("USER", "22")
                            } else {
                                Log.e("USER", "33")
                                goToRoom()
                            }
                        } catch (e: Exception) {
                            Log.e("ERROR", e.message!!)
                            goToRoom()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
    }
}