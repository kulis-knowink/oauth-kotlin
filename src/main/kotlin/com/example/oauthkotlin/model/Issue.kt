package com.example.oauthkotlin.model

data class Issue (
        val id: Number,
        var status: String,
        val callerName: String = "",
        val agentName: String = "",
        val description: String = "",
        var assignedRover: String = "",
        var priority: String = ""
)