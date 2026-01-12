package com.example.flash_drop

import java.io.Serializable
import java.util.Date
import java.util.UUID

data class TransferHistory(
    val id: String = UUID.randomUUID().toString(),
    val deviceName: String,
    val deviceAddress: String,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,
    val date: Date = Date(),
    var status: String = "Concluído"
) : Serializable