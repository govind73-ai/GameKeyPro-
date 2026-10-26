package com.gamekeypro.model
import org.json.JSONObject

data class KeyMapping(val keyCode: Int, val targetX: Float, val targetY: Float, val isCrazyTap: Boolean = false, val tapIntervalMs: Long = 50L) {
    fun toJson(): JSONObject = JSONObject().apply { put("keyCode", keyCode); put("x", targetX.toDouble()); put("y", targetY.toDouble()); put("crazyTap", isCrazyTap); put("interval", tapIntervalMs) }
    companion object {
        fun fromJson(json: JSONObject) = KeyMapping(json.getInt("keyCode"), json.getDouble("x").toFloat(), json.getDouble("y").toFloat(), json.optBoolean("crazyTap", false), json.optLong("interval", 50L))
    }
}
