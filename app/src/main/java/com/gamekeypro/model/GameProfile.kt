package com.gamekeypro.model
import org.json.JSONArray
import org.json.JSONObject

data class GameProfile(val profileName: String, val packageName: String, val mappings: MutableList<KeyMapping> = mutableListOf()) {
    fun toJson(): String {
        val root = JSONObject().apply { put("name", profileName); put("package", packageName) }
        val array = JSONArray()
        mappings.forEach { array.put(it.toJson()) }
        root.put("mappings", array)
        return root.toString()
    }
    companion object {
        fun fromJson(jsonString: String): GameProfile {
            val root = JSONObject(jsonString)
            val profile = GameProfile(root.getString("name"), root.getString("package"))
            root.optJSONArray("mappings")?.let { for (i in 0 until it.length()) profile.mappings.add(KeyMapping.fromJson(it.getJSONObject(i))) }
            return profile
        }
    }
}
