package com.src.uscan.utils

import com.src.uscan.room.PDFEntity

interface RoomOperationCompleted {
    fun processFinish(output: ArrayList<PDFEntity>?)
}