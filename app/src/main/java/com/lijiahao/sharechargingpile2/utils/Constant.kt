package com.lijiahao.sharechargingpile2.utils

const val VISIBLE: Int = 0
const val INVISIBLE: Int = 1
const val GONE: Int = 2

const val SHARED_PREFERENCES_NAME = "SharedChargingPile2"

const val LOGIN_OUT_OF_TIME = "LoginOutOfTime"

const val USER_ID_IN_PREFERENCES = "userId"
const val TOKEN_IN_PREFERENCES = "token"
const val USER_ACCOUNT_IN_PREFERENCES = "userAccount"
const val USER_PASSWORD_IN_PREFERENCES = "userPassword"

const val WEB_SOCKET_NORMAL_CLOSE_CODE = 1000 // webSocket完成任务正常关闭

const val MESSAGE_ARRIVED_BROADCAST_ACTION = "Broadcast Message Arrived Action"
const val MESSAGE_BROADCAST_BUNDLE = "message"

//
//const val SERVER_BASE_DOMAIN_PORT="10.0.2.2:30000"
//const val SERVER_BASE_HTTP_URL="http://$SERVER_BASE_DOMAIN_PORT/"
//const val SERVER_BASE_WEB_SOCKET_URL="ws://$SERVER_BASE_DOMAIN_PORT/publishMessage/"



const val SERVER_BASE_DOMAIN_PORT="172.16.191.206:30000"
const val SERVER_BASE_HTTP_URL="http://$SERVER_BASE_DOMAIN_PORT/"
const val SERVER_BASE_WEB_SOCKET_URL="ws://$SERVER_BASE_DOMAIN_PORT/publishMessage/"
