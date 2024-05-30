import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun fetchFootballStadiums():MutableList<Stadium>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/footballStadium/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<Stadium>>() {}.type
        val gson = Gson()
        var matches : MutableList<Stadium> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
fun fetchHandballStadiums():MutableList<Stadium>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/handballStadium/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<Stadium>>() {}.type
        val gson = Gson()
        var matches : MutableList<Stadium> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}

fun createFootballMatch(token: String, match: FootballMatch): Boolean {
    val client = OkHttpClient()

    val gson = Gson()
    val jsonMatch = gson.toJson(match)
    val requestBody: RequestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Create successful: ${response.body?.string()}")
            true
        } else {
            println("Create failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun createMatchH(token: String,match: FootballMatch,id: String):Boolean {
    val client = OkHttpClient()

    val gson = Gson()
    val jsonMatch: JsonObject = gson.toJsonTree(match).asJsonObject

    jsonMatch.addProperty("stadium", id)

    // Convert JsonObject back to JSON string
    val jsonMatchString = gson.toJson(jsonMatch)
    val requestBody: RequestBody = jsonMatchString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/handballMatch")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Create successful: ${response.body?.string()}")
            true
        } else {
            println("Create failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun createMatch(token: String,match: FootballMatch,id: String):Boolean {
    val client = OkHttpClient()

    val gson = Gson()
    val jsonMatch: JsonObject = gson.toJsonTree(match).asJsonObject

    // Manually set the stadium field to the id value
    jsonMatch.addProperty("stadium", id)

    // Convert JsonObject back to JSON string
    val jsonMatchString = gson.toJson(jsonMatch)
    val requestBody: RequestBody = jsonMatchString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Create successful: ${response.body?.string()}")
            true
        } else {
            println("Create failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun fetchFootballMatches(token : String):MutableList<FootballMatch>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/")
        .build()
    println("fetch")
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<FootballMatch>>() {}.type
        val gson = Gson()
        var matches : MutableList<FootballMatch> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
fun updateFootballMatch(token: String, updatedMatch: FootballMatch): Boolean {
    val client = OkHttpClient()
    val gson = Gson()
    val jsonMatch = gson.toJson(updatedMatch)
    val requestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/${updatedMatch._id}")
        .put(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Updating match with ID: ${updatedMatch._id}")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Update successful: ${response.body?.string()}")
            true
        } else {
            println("Update failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun deleteFootballMatch(token: String, id: String): Boolean {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/$id")
        .delete()
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Deleting match with ID: $id")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Delete successful: ${response.body?.string()}")
            true
        } else {
            println("Delete failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun fetchHandballMatches(token : String):MutableList<FootballMatch>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/handballMatch/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<FootballMatch>>() {}.type
        val gson = Gson()
        var matches : MutableList<FootballMatch> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
suspend fun fetchStandingsHandball():MutableList<Standing>{

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/handballStanding/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<Standing>>() {}.type
        val gson = Gson()
        var standings : MutableList<Standing> = gson.fromJson(json,type)

        return standings
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }

}
fun fetchStandingsFootball():MutableList<Standing>{

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://localhost:3000/footballStanding/")
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            val json = response.body?.string() ?: ""
            val type = object : TypeToken<MutableList<Standing>>() {}.type
            val gson = Gson()
            var standings : MutableList<Standing> = gson.fromJson(json,type)

            return standings
        } catch (e: IOException) {
            e.printStackTrace()
            return mutableListOf()
        }

}
fun updateHandballMatch(token: String, updatedMatch: FootballMatch): Boolean {
    val client = OkHttpClient()
    val gson = Gson()
    val jsonMatch = gson.toJson(updatedMatch)
    val requestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/handballMatch/${updatedMatch._id}")
        .put(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Updating match with ID: ${updatedMatch._id}")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Update successful: ${response.body?.string()}")
            true
        } else {
            println("Update failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun deleteHandballMatch(token: String, id: String): Boolean {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("http://localhost:3000/handballMatch/$id")
        .delete()
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Deleting match with ID: $id")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Delete successful: ${response.body?.string()}")
            true
        } else {
            println("Delete failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun createHandballMatch(token: String, match: FootballMatch): Boolean {
    val client = OkHttpClient()

    val gson = Gson()
    val jsonMatch = gson.toJson(match)
    val requestBody: RequestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/handballMatch")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Create successful: ${response.body?.string()}")
            true
        } else {
            println("Create failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
