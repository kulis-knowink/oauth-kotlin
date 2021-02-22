package com.example.oauthkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer




@SpringBootApplication
class OauthKotlinApplication

fun main(args: Array<String>) {
	runApplication<OauthKotlinApplication>(*args)
}
