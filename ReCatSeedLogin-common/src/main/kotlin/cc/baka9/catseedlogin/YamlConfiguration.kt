package cc.baka9.catseedlogin

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.HashMap
import java.util.Collections
import java.util.stream.Collectors

class YamlConfiguration(var file: File?) {
    private val yaml = org.yaml.snakeyaml.Yaml()
    private var data: MutableMap<String, Any> = HashMap()
    
    companion object {
        fun loadConfiguration(file: File?): YamlConfiguration {
            val config = YamlConfiguration(file)
            if (file != null && file.exists()) {
                try {
                    config.load()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return config
        }
    }
    
    fun load() {
        if (file != null && file.exists()) {
            try {
                java.io.FileInputStream(file).use { fis ->
                    val loaded = yaml.load(InputStreamReader(fis, StandardCharsets.UTF_8))
                    if (loaded is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        data = HashMap(loaded as Map<String, Any>)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    fun loadFromResource(inputStream: InputStream) {
        inputStream.use { stream ->
            InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
                val loaded = yaml.load(reader)
                if (loaded is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    data = HashMap(loaded as Map<String, Any>)
                }
            }
        }
    }
    
    fun save() {
        if (file == null) return
        file?.parentFile?.mkdirs()
        try {
            java.io.OutputStreamWriter(java.io.FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
                yaml.dump(data, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    val dataMap: Map<String, Any>
        get() = data
    
    fun getBoolean(path: String, defaultValue: Boolean = false): Boolean {
        val value = get(path)
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> defaultValue
        }
    }
    
    fun getInt(path: String, defaultValue: Int = 0): Int {
        val value = get(path)
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    
    fun getLong(path: String, defaultValue: Long = 0L): Long {
        val value = get(path)
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    
    fun getString(path: String, defaultValue: String = ""): String {
        val value = get(path)
        return value?.toString() ?: defaultValue
    }
    
    fun getDouble(path: String, defaultValue: Double = 0.0): Double {
        val value = get(path)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    }
    
    fun getStringList(path: String): List<String> {
        val value = get(path)
        return if (value is List<*>) {
            value.map { it.toString() }
        } else {
            Collections.emptyList()
        }
    }
    
    fun getSection(path: String): ConfigurationSection {
        return object : ConfigurationSection {
            override fun getPath(): String = path
            override fun getRoot(): Configuration = this@YamlConfiguration
        }
    }
    
    fun set(path: String, value: Any?) {
        setNestedValue(data, path.split("\\.".toRegex()).toTypedArray(), 0, value)
    }
    
    fun contains(path: String): Boolean {
        return get(path) != null
    }
    
    private fun get(path: String): Any? {
        val parts = path.split("\\.".toRegex())
        var current: MutableMap<String, Any> = data
        for (i in 0 until parts.size - 1) {
            val next = current[parts[i]] ?: return null
            if (next !is Map<*, *>) return null
            @Suppress("UNCHECKED_CAST")
            current = next as MutableMap<String, Any>
        }
        return current[parts.last()]
    }
    
    private fun setNestedValue(map: MutableMap<String, Any>, keys: Array<String>, index: Int, value: Any?) {
        if (index == keys.size - 1) {
            if (value == null) {
                map.remove(keys[index])
            } else {
                map[keys[index]] = value
            }
            return
        }
        val next = map.getOrPut(keys[index]) { HashMap<String, Any>() }
        if (next !is MutableMap<*, *>) {
            map[keys[index]] = HashMap<String, Any>().also { (it as MutableMap<String, Any>) }
        }
        @Suppress("UNCHECKED_CAST")
        setNestedValue(next as MutableMap<String, Any>, keys, index + 1, value)
    }
    
    interface ConfigurationSection {
        fun getPath(): String
        fun getRoot(): Configuration
    }
    
    interface Configuration {
        fun getBoolean(path: String, defaultValue: Boolean): Boolean
        fun getInt(path: String, defaultValue: Int): Int
        fun getLong(path: String, defaultValue: Long): Long
        fun getString(path: String, defaultValue: String): String
        fun getDouble(path: String, defaultValue: Double): Double
        fun getStringList(path: String): List<String>
        fun getSection(path: String): ConfigurationSection
        fun set(path: String, value: Any?)
        fun contains(path: String): Boolean
    }
}