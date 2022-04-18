package com.lijiahao.sharechargingpile2.exception

import java.lang.Exception

class TimeNotCoverAllDayException(msg:String?=null): Exception("时间没有覆盖全天 + ${msg?:""}"){
}