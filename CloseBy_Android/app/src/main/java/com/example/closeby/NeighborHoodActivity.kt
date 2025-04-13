package com.example.closeby.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.closeby.CreateNeighborhoodActivity
import com.example.closeby.LoginActivity
import com.example.closeby.R
import com.google.firebase.auth.FirebaseAuth

class NeighborhoodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighborhood)

        val joinButton: Button = findViewById(R.id.joinButton)
        val createButton: Button = findViewById(R.id.createButton)

        // check if buttons are being clicked
        joinButton.setOnClickListener {
            val intent = Intent(this, JoinNeighborhoodActivity::class.java)
            startActivity(intent)
        }

        createButton.setOnClickListener {
            val intent = Intent(this, CreateNeighborhoodActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}