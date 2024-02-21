package com.example.checkers_skylb.main

import com.example.checkers_skylb.main.data.CheckerPiece
import com.example.checkers_skylb.main.data.Square

interface PlayPiece {
    fun pieceAt(square: Square): CheckerPiece? //null check
    fun movePiece(from: Square, to: Square)
}