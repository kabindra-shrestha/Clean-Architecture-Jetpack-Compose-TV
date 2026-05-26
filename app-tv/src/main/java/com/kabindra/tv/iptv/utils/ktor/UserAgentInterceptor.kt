package com.kabindra.tv.iptv.utils.ktor

class UserAgentInterceptor(private val headersProvider: () -> Map<String, String>)
