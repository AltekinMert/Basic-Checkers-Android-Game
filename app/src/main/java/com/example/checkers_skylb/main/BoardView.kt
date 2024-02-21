package com.example.checkers_skylb.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.checkers_skylb.R
import com.example.checkers_skylb.main.data.CheckerPiece
import com.example.checkers_skylb.main.data.DarkGreenSquares
import com.example.checkers_skylb.main.data.Player
import com.example.checkers_skylb.main.data.Square
import kotlin.math.min

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var originX = 20f
    private var originY = 200f
    private var radius = 0f
    private var initialPlacement = 65f
    private var shift = (initialPlacement * 2)
    private val scaleFactor = 1.0f




    private var fromCol: Int = -1
    private var fromRow: Int = -1
    private var movingPieceX = -1f
    private var movingPieceY = -1f


    private val paint = Paint()
    private val bitmaps = mutableMapOf<Int,Bitmap>()
    private val darkSquares = HashSet<DarkGreenSquares>()


    var playPiece: PlayPiece? = null // placing a piece
    private var movingPiece: CheckerPiece? = null //moving a piece
    private var movingPieceBitmap: Bitmap? = null


    private val imageId = setOf(
        R.drawable.black,
        R.drawable.white,
        R.drawable.white_king,
        R.drawable.black_king,

        )


    init {
        populateMap()
    }
    private fun populateMap(){
        imageId.forEach{ imageId ->
            bitmaps[imageId] = BitmapFactory.decodeResource(resources,imageId)
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val smaller = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(smaller, smaller)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return//if canvas null exit function

        var boardSize = min(width, height) * scaleFactor
        shift = boardSize / 8f
        originX = (width - boardSize) / 2f
        originY = (height - boardSize) / 2f
        radius = min(width - (shift * 5), height - (shift * 5)) / 8f

        super.onDraw(canvas)
        drawCheckerboard(canvas)
        //drawStartPieces(canvas)
        drawWhilePlaying(canvas)

        CheckersPlay.checkToKing()


    }


    private fun drawCheckerboard(canvas: Canvas) {
        for (row in 0..7) {
            for (col in 0..7) {
                drawSquareAt(canvas, col, row, (col + row) % 2 == 1) //so one light one dark square will be generated
            }
        }
    }

    private fun drawSquareAt(canvas: Canvas, col: Int, row: Int, isDark: Boolean) {

        paint.color = if (isDark) {
            Color.rgb(93, 137, 114)

        } else {
            Color.rgb(114, 154, 133)
        }

        canvas.drawRect(
            originX + col * shift,
            originY + row * shift,
            originX + (col + 1) * shift,
            originY + (row + 1) * shift,
            paint
        )
        if (!isDark) {
            var darkSquare = DarkGreenSquares(col, row)
            darkSquares.add(darkSquare)
        }
    }



    private fun drawWhilePlaying(canvas: Canvas) {

        for (row in 0 until 8)
            for (col in 0 until 8)
                playPiece?.pieceAt(Square(col, row))?.let { piece ->
                    if (piece != movingPiece) {

                        drawPieceAt(canvas, col, row, piece.imageId)
                    }
                }


        if (movingPiece != null ) {
            // Draw a highlight around the selected piece's square
            paint.color = Color.rgb(0, 225, 125)
            canvas.drawRect(
                originX + movingPiece!!.col * shift,
                originY + (7 - movingPiece!!.row) * shift,
                originX + (movingPiece!!.col + 1) * shift,
                originY + ((7 - movingPiece!!.row) + 1) * shift,
                paint
            )
        }
        if(movingPiece != null) {
            var validMoveLocations = mutableListOf<Square>() //we need to fill the validlocations for movingpiece
            validMoveLocations = CheckersPlay.ValidLocations(movingPiece!!,validMoveLocations)
            validMoveLocations.forEachIndexed { index, square ->
                if(movingPiece!!.imageId == R.drawable.white || movingPiece!!.imageId == R.drawable.black){
                    highlight(square,canvas)
                }else if (movingPiece!!.imageId == R.drawable.white_king || movingPiece!!.imageId == R.drawable.black_king){
                    highlight_king(square,canvas)
                }
            }
        }




        movingPieceBitmap?.let {
            canvas.drawBitmap(
                it,
                null,
                RectF(
                    movingPieceX - shift / 2,
                    movingPieceY - shift / 2,
                    movingPieceX + shift / 2,
                    movingPieceY + shift / 2
                ),
                paint
            )
        }

    }
    private fun highlight(square : Square,canvas: Canvas){
        val col = square.col
        val row = square.row

        if (movingPiece != null ) {
            // Draw a highlight around the selected piece's square
            paint.color = Color.rgb(0, 225, 225)
            canvas.drawRect(
                originX + col * shift,
                originY + (7 - row) * shift,
                originX + (col + 1) * shift,
                originY + ((7 - row) + 1) * shift,
                paint
            )
        }
    }
    private fun highlight_king(square : Square,canvas: Canvas){
        val col = square.col
        val row = square.row

        if (movingPiece != null ) {
            // Draw a highlight around the selected piece's square
            paint.color = Color.rgb(225, 150, 0)
            canvas.drawRect(
                originX + col * shift,
                originY + (7 - row) * shift,
                originX + (col + 1) * shift,
                originY + ((7 - row) + 1) * shift,
                paint
            )
        }
    }

    private fun drawPieceAt(canvas: Canvas, col: Int, row: Int, imageId: Int) {
        canvas.drawBitmap(
            bitmaps[imageId]!!,
            null,
            RectF(
                originX + col * shift,
                originY + (7 - row) * shift,
                originX + (col + 1) * shift,
                originY + ((7 - row) + 1) * shift
            ),
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                fromCol = ((event.x - originX) / shift).toInt()
                fromRow = 7 - ((event.y - originY) / shift).toInt()
                if (isBlackSquare(fromCol, fromRow)) {


                    playPiece?.pieceAt(Square(fromCol, fromRow))?.let {
                        if(CheckersPlay.whiteTurn){
                            if(it.player == Player.WHITE) {
                                movingPiece = it
                                movingPieceBitmap = bitmaps[it.imageId]

                            }
                        }
                        if(!CheckersPlay.whiteTurn){
                            if(it.player == Player.BLACK) {
                                movingPiece = it
                                movingPieceBitmap = bitmaps[it.imageId]

                            }
                        }
                        //now we have movingPiece binded lets find out where this piece can go ?



                    }

                }
            }

            MotionEvent.ACTION_MOVE -> {

                movingPieceX = event.x
                movingPieceY = event.y
                invalidate()
            }


            MotionEvent.ACTION_UP -> {
                val col = ((event.x - originX) / shift).toInt()
                val row = 7 - ((event.y - originY) / shift).toInt()
                if (fromCol != col || fromRow != row) { //that means we changed movingPiece rol , col
                    if (isBlackSquare(col, row)) { //acceptable square ?
                        playPiece?.movePiece(Square(fromCol, fromRow), Square(col, row))

                    }
                }
                movingPiece = null //reset the movingPiece
                movingPieceBitmap = null
                invalidate()
            }
        }
        return true
    }


    private fun isBlackSquare(col: Int, row: Int): Boolean {

        for (square in darkSquares) {
            if (square.col == col && square.row == row) {
                return true
            }
        }
        return false
    }


}
