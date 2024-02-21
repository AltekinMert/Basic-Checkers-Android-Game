package com.example.checkers_skylb.main.ui

import android.annotation.SuppressLint
import android.app.GameManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.checkers_skylb.R
import com.example.checkers_skylb.main.BoardView
import com.example.checkers_skylb.main.BoardViewCallback
import com.example.checkers_skylb.main.data.CheckerPiece
import com.example.checkers_skylb.main.CheckersPlay
import com.example.checkers_skylb.main.CheckersPlay.reset
import com.example.checkers_skylb.main.PlayPiece
import com.example.checkers_skylb.main.data.Square
import java.io.PrintWriter
const val TAG = "GameBoard"
class GameBoard : AppCompatActivity(), PlayPiece,BoardViewCallback {


    private var printWriter: PrintWriter? = null
    private lateinit var boardView: BoardView
    private lateinit var scoreWhite: TextView
    private lateinit var scoreBlack: TextView
    private lateinit var turnImage: ImageView
    private var blackScoreCount = 0
    private var whiteScoreCount = 0
    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_board)

        val back_button : ImageView = findViewById(R.id.back_button)
        back_button.setOnClickListener{

            val intent = Intent(this,MainActivity::class.java)
            showWarningPopup(
                this,
                "Warning",
                "Are you sure about leaving the game\nGame will dismiss",
                "Continue",
                "Yes",
                {
                    // Code to execute when the "Continue" button is clicked
                    // Add your positive button action here
                },
                {
                    startActivity(intent)
                }
            )
        }


        boardView = findViewById(R.id.board_view)
        scoreWhite = findViewById(R.id.score_text_white)
        scoreBlack = findViewById(R.id.score_text_black)
        turnImage = findViewById(R.id.turn_view)

        scoreBlack.text = getString(R.string.black_points, blackScoreCount)
        scoreWhite.text = getString(R.string.white_points, whiteScoreCount)
        boardView.playPiece = this


        CheckersPlay.setBoardViewCallback(this)

    }

    override fun onBackPressed() {
        showWarningPopup(
            this,
            "Warning",
            "Are you sure about leaving the game\nGame will dismiss",
            "Continue",
            "Yes",
            {
                // Code to execute when the "Continue" button is clicked
                // Add your positive button action here
            },
            {
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        )
    }




    fun showWarningPopup(context: Context, title: String, message: String, positiveText: String, negativeText: String, onPositiveClick: () -> Unit, onNegativeClick: () -> Unit) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                onPositiveClick.invoke()
                dialog.dismiss()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                onNegativeClick.invoke()
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }


    override fun pieceAt(square: Square): CheckerPiece? = CheckersPlay.pieceAt(square)

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    override fun movePiece(from: Square, to: Square) {


        CheckersPlay.movePiece(from, to)

        printWriter?.let {
            val moveStr = "${from.col},${from.row},${to.col},${to.row}"
            Log.d(TAG,moveStr)

        }
        // update score if game is finished and reset
        if(CheckersPlay.checkForWinner() == -1){
            Log.d(TAG, "Black has won a game")
            blackScoreCount++
            scoreBlack.text = getString(R.string.black_points, blackScoreCount)
            reset()
        }
        if(CheckersPlay.checkForWinner() == 1){
            Log.d(TAG, "White has won a game")
            whiteScoreCount++
            scoreWhite.text = getString(R.string.white_points, whiteScoreCount)
            reset()

        }
        // update the color according to turn
        if(CheckersPlay.whiteTurn){
            val textView : TextView = findViewById(R.id.turnText)
            textView.text = "White Turn"
            turnImage.setImageResource(R.drawable.turn_red_bg)
        }else{
            val textView : TextView = findViewById(R.id.turnText)
            textView.text = "Black Turn"
            turnImage.setImageResource(R.drawable.turn_blue_bg)
        }
        boardView.invalidate()
    }

    override fun update() {
        boardView.invalidate()
    }

}