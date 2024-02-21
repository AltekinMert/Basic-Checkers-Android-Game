package com.example.checkers_skylb.main

import android.annotation.SuppressLint
import com.example.checkers_skylb.R
import android.util.Log
import com.example.checkers_skylb.main.CheckersPlay.didKingAte
import com.example.checkers_skylb.main.CheckersPlay.pieceAt
import com.example.checkers_skylb.main.CheckersPlay.whiteTurn
import com.example.checkers_skylb.main.data.CheckerPiece
import com.example.checkers_skylb.main.data.Player
import com.example.checkers_skylb.main.data.Square
import com.example.checkers_skylb.main.ui.TAG
import kotlin.math.abs

object CheckersPlay {

    private var boardViewCallback: BoardViewCallback? = null

    fun setBoardViewCallback(callback: BoardViewCallback) {
        boardViewCallback = callback
    }

    fun updateView() {
        boardViewCallback?.update()
    }




    // shows if the king ate a piece in the previous move
    var didKingAte = false
    // give the coordinates of the king which ate a piece in the previous turn
    var kingCords = Square(col = -1, row = -1)
    var whiteTurn = true
    // piece counters

    private var whiteCount = 0
    private var blackCount = 0
    private var totalPieces = mutableSetOf<CheckerPiece>()
    private var removeList = mutableListOf<CheckerPiece>()
    // shows if a piece get eaten in the previous turn
    private var doubleJump = false

    init {
        reset()
    }


    fun checkForWinner(): Int {

        whiteCount = 0
        blackCount = 0
        for (piece in totalPieces) {
            if (piece.player == Player.WHITE) {
                whiteCount++
            }
            if (piece.player == Player.BLACK) {
                blackCount++
            }
        }
        if (blackCount < 1) {
            // white has won
            Log.d(TAG, "WHITE HAS WON")
            return 1
        }
        if (whiteCount < 1) {
            //black has won
            Log.d(TAG, "BLACK HAS WON")
            return -1
        }
        return 0
    }


    //CheckerPiece(var col: Int, var row: Int, val player: Player, val ID: Int)
    fun reset() {
        totalPieces.clear()
        whiteTurn = true
        // where red is the player and black is the ai
        val white = R.drawable.white

        val black = R.drawable.black

        for (row in 0..2) {
            for (col in 0..7) { // fill bottom (player) rows pieces info:
                if (col % 2 == 0 && ((7 - row) == 7 || (7 - row) == 5)) {
                    totalPieces.add((CheckerPiece(col + 1, 7 - row, Player.BLACK, black)))

                } else if (col % 2 != 0 && (7 - row) == 6) {
                    totalPieces.add(CheckerPiece(col - 1, 7 - row, Player.BLACK, black))
                } else if (col % 2 == 0) {
                    totalPieces.add(CheckerPiece(col + 1, row, Player.WHITE, white))
                } else { // fill top row pieces info:
                    totalPieces.add(CheckerPiece(col - 1, row, Player.WHITE, white))
                }
            }
        }
    }

    // check if basic piece can turn into king
    fun checkToKing() {
        for (piece in totalPieces) {
            if (piece.imageId == R.drawable.black || piece.imageId == R.drawable.white) {
                if (piece.player == Player.BLACK && piece.row == 0) {
                    totalPieces.remove(piece)
                    piece.imageId = R.drawable.black_king
                    totalPieces.add(piece)
                    updateView()
                }
                if (piece.player == Player.WHITE && piece.row == 7) {
                    totalPieces.remove(piece)
                    piece.imageId = R.drawable.white_king
                    totalPieces.add(piece)
                    updateView()
                }

            }
        }
    }

    // return the coordinates of a square where a piece in prevCol and prevRow can eat
    private fun canEat(prevCol: Int, prevRow: Int): Square {
        if (prevCol < 6 && prevRow < 6 && isSquareClear(prevCol + 2, prevRow + 2)) {
            if (MoveBasic(Square(prevCol, prevRow), Square(prevCol + 2, prevRow + 2))) {
                return Square(prevCol + 2, prevRow + 2)
            }
        }
        if (prevCol > 1 && prevRow < 6 && isSquareClear(prevCol - 2, prevRow + 2)) {
            if (MoveBasic(Square(prevCol, prevRow), Square(prevCol - 2, prevRow + 2))) {
                return Square(prevCol - 2, prevRow + 2)
            }
        }
        if (prevRow > 1 && prevCol > 1 && isSquareClear(prevCol - 2, prevRow - 2)) {
            if (MoveBasic(Square(prevCol, prevRow), Square(prevCol - 2, prevRow - 2))) {
                return Square(prevCol - 2, prevRow - 2)
            }

        }
        if (prevCol < 6 && prevRow > 1 && isSquareClear(prevCol + 2, prevRow - 2)) {
            if (MoveBasic(Square(prevCol, prevRow), Square(prevCol + 2, prevRow - 2))) {
                return Square(prevCol + 2, prevRow - 2)
            }

        }
        return Square(prevCol, prevRow)
    }

    // return true if king can eat a piece from its current position false otherwise
    fun canKingEat(fromCol: Int, fromRow: Int): Boolean {
        Log.d("cankingeatcalled", "cankingeatcalled")
        if(fromCol < 0 || fromCol > 7 || fromRow < 0 || fromRow > 7){
            return false
        }
        var diffCol = 1
        var diffRow = 1
        // moving UpRight
        while (fromCol + diffCol <= 7 && fromRow + diffRow <= 7) {
            if (moveKing(Square(fromCol, fromRow), Square(fromCol + diffCol, fromRow + diffRow))) {
                if (didKingAte) {
                    return true
                }
            }
            diffCol += 1
            diffRow += 1
        }
        diffCol = 1
        diffRow = 1
        // moving UpLeft
        while (fromCol - diffCol >= 0 && fromRow + diffRow <= 7) {
            if (moveKing(Square(fromCol, fromRow), Square(fromCol - diffCol, fromRow + diffRow))) {
                if (didKingAte) {
                    return true
                }
            }
            diffCol += 1
            diffRow += 1
        }
        diffCol = 1
        diffRow = 1
        // moving DownRight
        while (fromCol + diffCol < 6 && fromRow - diffCol >= 0) {
            if (moveKing(Square(fromCol, fromRow), Square(fromCol + diffCol, fromRow - diffRow))) {
                if (didKingAte) {
                    return true
                }
            }
            diffCol += 1
            diffRow += 1
        }
        diffCol = 1
        diffRow = 1
        // moving DownLeft
        while (fromCol - diffCol <= 7 && fromRow - diffCol >= 0) {
            if (moveKing(Square(fromCol, fromRow), Square(fromCol - diffCol, fromRow - diffRow))) {
                if (didKingAte) {
                    return true
                }
            }
            diffCol += 1
            diffRow += 1
        }
        return false
    }

    // return the piece at given square null otherwise
    fun pieceAt(square: Square): CheckerPiece? {
        return pieceAt(square.col, square.row)
    }

    private fun pieceAt(col: Int, row: Int): CheckerPiece? {
        for (piece in totalPieces) {

            if (col == piece.col && row == piece.row) {
                return piece
            }
        }
        return null
    }

    // check if a piece can move from given square to target
    // if it is possible go movePiece(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int)
    fun movePiece(from: Square, to: Square) {

        val movingPiece = pieceAt(from.col, from.row) ?: return

        if (movingPiece.imageId == R.drawable.black_king || movingPiece.imageId == R.drawable.white_king) {
            if (moveKing(from, to)) {
                Log.d("KİNG","KİNG")
                Log.d(TAG, "movePiece: moving from $from to $to")
                movePiece(from.col, from.row, to.col, to.row)
            }
        } else {
            if (MoveBasic(from, to)) {
                //check to see if he destination square is not occupied by another piece:
                if (isSquareClear(to.col, to.row)) {
                    Log.d(TAG, "movePiece: moving from $from to $to")
                    movePiece(from.col, from.row, to.col, to.row)
                }
            }
        }
        // check that the origin is not the same as the destination:
    }

    // make arrangements to board (add-remove pieces)
    // if a piece get eaten in previous turn, check for a follow up turns
    // algorithm makes decision for basic piece but for the king piece it allows you to choose location
    // if there is no room for consecutive movement then end turn.
    private fun movePiece(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int) {
        val movingPiece = pieceAt(fromCol, fromRow) ?: return
        if (movingPiece.imageId == R.drawable.black_king || movingPiece.imageId == R.drawable.white_king) {
            Log.d("Move piece içinde king tuttum","aaaaaaa")
            for (piece in removeList) {
                totalPieces.remove(piece)
            }
        }
        totalPieces.add(movingPiece.copy(col = toCol, row = toRow))
        totalPieces.remove(movingPiece)
        if (doubleJump) {
            if (movingPiece.imageId == R.drawable.white || movingPiece.imageId == R.drawable.black) {
                totalPieces.remove(pieceAt((toCol + fromCol) / 2, (toRow + fromRow) / 2))
                val point = canEat(toCol, toRow)
                if (point == Square(toCol, toRow)) {
                    doubleJump = false
                } else {
                    movePiece(toCol, toRow, point.col, point.row)
                }
            }
            else {
                kingCords = Square(toCol, toRow)
                if(!canKingEat(kingCords.col, kingCords.row)){
                    Log.d("King did not eat anyone in movePiece function","aaaaaaa")
                    doubleJump = false
                }
            }


        }

        if (movingPiece.player == Player.BLACK && !whiteTurn && !doubleJump) {
            Log.d(TAG, "whiteTurn set to true")
            whiteTurn = true
        }
        if (movingPiece.player == Player.WHITE && whiteTurn && !doubleJump) {
            Log.d(TAG, "whiteTurn set to false")
            whiteTurn = false
        }


    }

    // check if king piece can move from given square
    // check it in all four directions
    fun moveKing(from: Square, to: Square): Boolean {
        Log.d("movekingcalled", "movekingcalled")
        //Check if the square is empty
        if (!isSquareClear(to.col, to.row)) {
            Log.d("square is not clear","aaaaaaaaaa")
            return false
        }
        if (to == from) {
            return false
        }
        removeList = mutableListOf()
        val movingPiece = pieceAt(from) ?: return false

        if (to.col > 7 || to.row > 7 || to.row < 0 || to.col < 0) {
            return false
        }

        if (from.row == to.row) {
            return false
        }
        if (from.col == to.col) {
            return false
        }

        if (movingPiece.player == Player.BLACK && whiteTurn) {
            Log.d(TAG, "It is white's turn.")
            return false
        }
        if (movingPiece.player == Player.WHITE && !whiteTurn) {
            Log.d(TAG, "It is black's turn.")
            return false
        }
        if (abs(to.col - from.col) != abs(to.col - from.col)) {
            return false
        }
        var diff = abs(to.col - from.col)

        if (diff == 1) {
            Log.d("diff is 1 ", "diff is 1")
            if(didKingAte){
                return false
            }
            return MoveBasic(from, to)
        }
        Log.d("diff is not 1", diff.toString())
        didKingAte = false
        doubleJump = false
        var isMovingUp = false
        var isMovingRight = false

        if (abs(to.col - from.col) == to.col - from.col) {
            isMovingRight = true
        }
        if (abs(to.row - from.row) == to.row - from.row) {
            isMovingUp = true
        }
        if (isMovingRight) {
            if (isMovingUp) {
                while (diff > 1) {
                    diff -= 1
                    if (!isSquareClear(from.col + diff, from.row + diff)) {
                        val pieceOnWay = pieceAt(from.col + diff, from.row + diff)
                        if (pieceOnWay!!.player == movingPiece.player) {
                            return false
                        } else {
                            if (!isSquareClear(pieceOnWay.col + 1, pieceOnWay.row + 1)) {
                                return false
                            } else {
                                removeList.add(pieceOnWay)
                            }
                        }
                    }
                }
            } else {
                while (diff > 1) {
                    diff -= 1
                    if (!isSquareClear(from.col + diff, from.row - diff)) {
                        val pieceOnWay = pieceAt(from.col + diff, from.row - diff)
                        if (pieceOnWay!!.player == movingPiece.player) {
                            return false
                        } else {
                            if (!isSquareClear(pieceOnWay.col + 1, pieceOnWay.row - 1)) {
                                return false
                            } else {
                                removeList.add(pieceOnWay)
                            }
                        }
                    }
                }
            }
        } else {
            if (isMovingUp) {
                while (diff > 1) {
                    diff -= 1
                    if (!isSquareClear(from.col - diff, from.row + diff)) {
                        val pieceOnWay = pieceAt(from.col - diff, from.row + diff)
                        if (pieceOnWay!!.player == movingPiece.player) {
                            return false
                        } else {
                            if (!isSquareClear(pieceOnWay.col - 1, pieceOnWay.row + 1)) {
                                return false
                            } else {
                                removeList.add(pieceOnWay)
                            }
                        }
                    }
                }
            } else {
                while (diff > 1) {
                    diff -= 1
                    if (!isSquareClear(from.col - diff, from.row - diff)) {
                        val pieceOnWay = pieceAt(from.col - diff, from.row - diff)
                        if (pieceOnWay!!.player == movingPiece.player) {
                            return false
                        } else {
                            if (!isSquareClear(pieceOnWay.col - 1, pieceOnWay.row - 1)) {
                                return false
                            } else {
                                removeList.add(pieceOnWay)
                            }
                        }
                    }
                }

            }
        }
        Log.d("0 capture", removeList.size.toString())
        if (removeList.size == 0) {
            return true //we did not eat anything
        }
        doubleJump = true
        didKingAte = true
        return true
    }

    // confirm that square is not occupied by a piece
    // but be careful function also returns true if given location is outside of the scope
    // (to.col > 7 || to.row > 7 || to.row < 0 || to.col < 0)
    fun isSquareClear(toCol: Int, toRow: Int): Boolean {
        for (piece in totalPieces) {
            if (piece.col == toCol && piece.row == toRow) {
                return false
            }
        }

        return true
    }

    // Check if basic piece can move to target location from given location
    // be careful in this function king move w/o eating a piece also considered as basic move
    fun MoveBasic(from: Square, to: Square): Boolean {
        if(didKingAte){
            if(canKingEat(kingCords.col, kingCords.row)){
                return false
            }else{
                didKingAte = false
                whiteTurn = !whiteTurn
                return false
            }
        }

        doubleJump = false
        val movingPiece = pieceAt(from) ?: return false

        if (to.col > 7 || to.row > 7 || to.row < 0 || to.col < 0) {
            return false
        }

        if (from.row == to.row) {
            return false
        }
        if (from.col == to.col) {
            return false
        }

        if (movingPiece.player == Player.BLACK && whiteTurn) {
            Log.d(TAG, "It is white's turn.")
            return false
        }
        if (movingPiece.player == Player.WHITE && !whiteTurn) {
            Log.d(TAG, "It is black's turn.")
            return false
        }

        // make sure we can only move a max of two squares at a time:
        if (abs(from.col - to.col) > 2 || abs(from.row - to.row) > 2) {
            return false
        }
        if (isSquareClear(to.col, to.row)) {


            if (abs(to.col - from.col) == 2 && abs(to.row - from.row) == 2) {
                val middlePiece =
                    pieceAt((to.col + from.col) / 2, (to.row + from.row) / 2) ?: return false
                return if (middlePiece.player != movingPiece.player) {
                    //totalPieces.remove(middle_piece)
                    doubleJump = true
                    true
                } else {
                    doubleJump = false // ?
                    false
                }
            }
            // ensure that white can only up down a row, never backwards unless it is not king
            if (movingPiece.player == Player.WHITE && from.row > to.row) {
                if (movingPiece.imageId == R.drawable.white_king) {
                    didKingAte = false
                    return true
                }
                return false
            }

            // ensure that black can only move down a row, never backwards unless it is not king
            if (movingPiece.player == Player.BLACK && from.row < to.row) {
                //movingPiece.imageId != R.drawable.black_king &&
                if (movingPiece.imageId == R.drawable.black_king) {
                    didKingAte = false
                    return true
                }
                return false
            }


        }
        // this is to insure that the player cannot jump non-diagonally
        if (abs(to.row - from.row) > 1) {
            return false
        }
        if (abs(to.col - from.col) > 1) {
            return false
        }
        // we simply move one square:
        return true
    }

    fun isEnemyNearby(toCol : Int , toRow :Int,player : Player) : Boolean{
        val enemypiece = pieceAt(toCol,toRow) ?: return false
        return enemypiece.player != player
    }
    fun isAllyNearby(toCol : Int , toRow :Int,player : Player) : Boolean{
        val enemypiece = pieceAt(toCol,toRow) ?: return false
        return enemypiece.player == player
    }





    @SuppressLint("SuspiciousIndentation")
    fun ValidLocations(movingPiece : CheckerPiece, validMoveLocations : MutableList<Square>) : MutableList<Square> {
        var fromCol = movingPiece.col
        var fromRow = movingPiece.row
        var toCol = fromCol
        var toRow = fromRow
        var eatCol = 0
        var eatRow = 0

            if(movingPiece.imageId == R.drawable.white || movingPiece.imageId == R.drawable.black){//normal pieces
                for(i in 0..3){
                    if (i == 0){
                         toCol = fromCol+1
                         toRow = fromRow+1
                        eatCol = toCol + 1
                        eatRow = toRow +1
                    }else if ( i == 1){
                        toCol = fromCol-1
                        toRow = fromRow+1
                        eatCol = toCol -1
                        eatRow = toRow +1
                    }
                    else if (i == 2){
                        toCol = fromCol+1
                        eatCol = toCol +1
                        toRow = fromRow-1
                        eatRow = toRow -1
                    }else if (i == 3){
                        toCol = fromCol-1
                        eatCol = toCol -1
                        toRow = fromRow-1
                        eatRow = toRow -1
                    }
                    if(isEnemyNearby(toCol,toRow,movingPiece.player)){
                        if(isSquareClear(eatCol,eatRow)){
                            validMoveLocations.add(Square(eatCol,eatRow))
                        }
                    }else if(isSquareClear(toCol,toRow)&& (i==1 || i == 0) && movingPiece.imageId == R.drawable.white){
                        validMoveLocations.add(Square(toCol,toRow))
                    }else if (isSquareClear(toCol,toRow)&& (i==2 || i == 3) && movingPiece.imageId == R.drawable.black){
                        validMoveLocations.add(Square(toCol,toRow))
                    }
                }
            }
        if(movingPiece.imageId == R.drawable.white_king || movingPiece.imageId == R.drawable.black_king){
           for(i in 0..1){
               if(i == 0){
               while(toCol >= 0 && toRow >= 0){
                   if (isEnemyNearby(toCol,toRow,movingPiece.player)){
                       if(isSquareClear(toCol-1,toRow-1)){
                           validMoveLocations.add(Square(toCol-1,toRow-1))
                       }else{
                           break
                       }
                   }else if(isSquareClear(toCol,toRow)){
                       validMoveLocations.add(Square(toCol,toRow))
                   }
                   toCol -=1
                   toRow -=1
               }
               toCol = fromCol
               toRow = fromRow
               while(toCol <=7 && toRow <= 7){
                   if (isEnemyNearby(toCol,toRow,movingPiece.player)){
                       if(isSquareClear(toCol+1,toRow+1)){
                           validMoveLocations.add(Square(toCol+1,toRow+1))
                       }else{
                           break
                       }
                   }else if(isSquareClear(toCol,toRow)){
                       validMoveLocations.add(Square(toCol,toRow))
                   }
                   toCol +=1
                   toRow +=1
               }
                   toCol = fromCol
                   toRow = fromRow
           }

               if (i == 1){

                   while(toCol >= 0 && toRow <= 7){
                       if (isEnemyNearby(toCol,toRow,movingPiece.player)){
                           if(isSquareClear(toCol-1,toRow+1)){
                               validMoveLocations.add(Square(toCol-1,toRow+1))
                           }else{
                               break
                           }
                       }else if(isSquareClear(toCol,toRow)){
                           validMoveLocations.add(Square(toCol,toRow))
                       }
                       toCol -=1
                       toRow +=1
                   }

                   toCol = fromCol
                   toRow = fromRow
                   while(toCol <=7 && toRow >= 0){
                       if (isEnemyNearby(toCol,toRow,movingPiece.player)){
                           if(isSquareClear(toCol+1,toRow-1)){
                               validMoveLocations.add(Square(toCol+1,toRow-1))
                           }else{
                               break
                           }
                       }else if(isSquareClear(toCol,toRow)){
                           validMoveLocations.add(Square(toCol,toRow))
                       }
                       toCol +=1
                       toRow -=1
                   }

               }

           }

        }


        return validMoveLocations
    }

}
