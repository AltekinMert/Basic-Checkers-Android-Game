package com.example.checkers_skylb.main.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.checkers_skylb.R
import com.example.checkers_skylb.main.CheckersPlay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Button that starts the game(changes activity)
        val start_button : Button = findViewById(R.id.button_start)

        start_button.setOnClickListener{
            val intent = Intent(this, GameBoard::class.java)
            startActivity(intent)
            CheckersPlay.reset()
        }

    }
}