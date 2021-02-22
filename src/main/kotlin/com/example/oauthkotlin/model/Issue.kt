package com.example.oauthkotlin.model

import com.fasterxml.jackson.annotation.JsonProperty


class Issue (

        @JsonProperty
        val id: Number,

        @JsonProperty
        var callerName: String = "",

        @JsonProperty
        var agentName: String = "",

        @JsonProperty
        var description: String = "",

        @JsonProperty
        var status: String = "",

        @JsonProperty
        var assignedRover: String = "",

        @JsonProperty
        var priority: String = ""
)