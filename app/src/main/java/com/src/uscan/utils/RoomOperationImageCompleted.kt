package com.src.uscan.utils

import com.src.uscan.room.ImageEntity
import com.src.uscan.room.PDFEntity

interface RoomOperationImageCompleted {
    fun processFinish(output: ArrayList<ImageEntity>?)
}