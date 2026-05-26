package com.kabindra.mobile.iptv.utils.ktor

class UserAgentInterceptor(private val headersProvider: () -> Map<String, String>)
