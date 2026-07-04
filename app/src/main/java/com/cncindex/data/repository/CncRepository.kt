package com.cncindex.data.repository

import android.content.Context
import android.net.Uri
import com.cncindex.data.local.dao.ProgramDao
import com.cncindex.data.local.dao.ToolDao
import com.cncindex.data.local.entity.CncProgram
import com.cncindex.data.local.entity.Tool
import com.cncindex.model.IndexFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

sealed class ImportResult {
    data class Success(val programCount: Int, val toolCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

data class ProgramGroup(
    val master: CncProgram,
    val children: List<CncProgram> = emptyList()
) {
    val isDuplicateGroup: Boolean get() = children.isNotEmpty()
}

class CncRepository(
    private val context: Context,
    private val programDao: ProgramDao,
    private val toolDao: ToolDao
) {
    private val gson = Gson()
    private var toolMap: Map<Int, String> = emptyMap()

    private fun makeQueryPattern(q: String): Pattern? {
        if (q.isBlank()) return null
        return try {
            if (q.contains('*') || q.contains('?')) {
                val escaped = buildString {
                    for (ch in q) {
                        when (ch) {
                            '*' -> append(".*")
                            '?' -> append(".")
                            '\\', '.', '^', '$', '+', '{', '}', '[', ']', '|', '(', ')' ->
                                append("\\").append(ch)
                            else -> append(ch)
                        }
                    }
                }
                Pattern.compile(escaped, Pattern.CASE_INSENSITIVE)
            } else {
                try {
                    Pattern.compile(q, Pattern.CASE_INSENSITIVE)
                } catch (e: PatternSyntaxException) {
                    Pattern.compile(Pattern.quote(q), Pattern.CASE_INSENSITIVE)
                }
            }
        } catch (e: PatternSyntaxException) {
            Pattern.compile(Pattern.quote(q), Pattern.CASE_INSENSITIVE)
        }
    }

    private fun matches(program: CncProgram, pattern: Pattern?): Boolean {
        if (pattern == null) return true
        val hay = "${program.filename} ${program.programNumber ?: ""} ${program.programName ?: ""}"
        return pattern.matcher(hay).find()
    }

    fun searchGroups(query: String): Flow<List<ProgramGroup>> = flow {
        val q = query.trim()
        val pattern = makeQueryPattern(q)
        val all = programDao.getAllList()

        val duplicateGroups = mutableMapOf<String, MutableList<CncProgram>>()
        val normalList = mutableListOf<CncProgram>()

        for (p in all) {
            if (p.isDuplicate && p.md5Group != null) {
                duplicateGroups.getOrPut(p.md5Group) { mutableListOf() }.add(p)
            } else {
                normalList.add(p)
            }
        }

        val result = mutableListOf<ProgramGroup>()

        for (p in normalList) {
            if (matches(p, pattern)) result.add(ProgramGroup(master = p))
        }

        for ((_, group) in duplicateGroups) {
            val anyMatch = pattern == null || group.any { matches(it, pattern) }
            if (!anyMatch) continue
            val sorted = group.sortedWith(compareBy({ it.modified }, { it.filename }))
            result.add(ProgramGroup(master = sorted.first(), children = sorted.drop(1)))
        }

        result.sortWith(compareBy { it.master.programNumber?.toIntOrNull() ?: Int.MAX_VALUE })
        emit(result)
    }.flowOn(Dispatchers.IO)

    suspend fun getToolsForProgram(toolsJson: String): List<Tool> {
        val type = object : TypeToken<List<Int>>() {}.type
        val ids: List<Int> = gson.fromJson(toolsJson, type) ?: emptyList()
        if (ids.isEmpty()) return emptyList()
        return toolDao.getToolsByIds(ids)
    }

    suspend fun importToolsFile(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val json = readTextFromUri(uri)
            val type = object : TypeToken<Map<String, String?>>() {}.type
            val rawMap: Map<String, String?> = gson.fromJson(json, type)
                ?: return@withContext ImportResult.Error("Neispravan format datoteke alata")

            val tools = rawMap.entries.mapNotNull { (key, value) ->
                val number = key.removePrefix("T").toIntOrNull() ?: return@mapNotNull null
                val name = value ?: return@mapNotNull null
                Tool(toolNumber = number, name = name)
            }

            toolDao.deleteAll()
            toolDao.insertAll(tools)
            toolMap = tools.associate { it.toolNumber to it.name }
            ImportResult.Success(programDao.count(), tools.size)
        } catch (e: Exception) {
            ImportResult.Error("Greška pri uvozu alata: ${e.message}")
        }
    }

    suspend fun importIndexFile(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            if (toolMap.isEmpty()) {
                toolMap = toolDao.getAllList().associate { it.toolNumber to it.name }
            }

            val json = readTextFromUri(uri)
            val indexFile = gson.fromJson(json, IndexFile::class.java)
                ?: return@withContext ImportResult.Error("Neispravan format index datoteke")

            // Novi format: grupiranje po md5
            val md5ValidGroups = indexFile.entries
                .filter { it.duplicate && it.md5 != null }
                .groupBy { it.md5!! }
                .filter { it.value.size > 1 }
                .keys

            // Stari format (bez md5): grupiranje po program_number
            val pnValidGroups = indexFile.entries
                .filter { it.duplicate && it.md5 == null && it.programNumber != null }
                .groupBy { it.programNumber!! }
                .filter { it.value.size > 1 }
                .keys

            val entities = indexFile.entries.map { entry ->
                val names = entry.tools.mapNotNull { toolMap[it] }.joinToString(",")
                val md5Group = when {
                    entry.duplicate && entry.md5 != null && entry.md5 in md5ValidGroups -> entry.md5
                    entry.duplicate && entry.md5 == null && entry.programNumber in pnValidGroups -> entry.programNumber
                    else -> null
                }
                CncProgram(
                    filename = entry.filename,
                    programNumber = entry.programNumber,
                    programName = entry.programName,
                    toolsJson = gson.toJson(entry.tools),
                    toolNames = names,
                    modified = entry.modified.toLong(),
                    hasProblem = entry.problem,
                    isDuplicate = entry.duplicate,
                    md5Group = md5Group,
                    filepath = entry.filepath
                )
            }

            programDao.deleteAll()
            programDao.insertAll(entities)
            ImportResult.Success(entities.size, toolMap.size)
        } catch (e: Exception) {
            ImportResult.Error("Greška pri uvozu indexa: ${e.message}")
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            ?: throw IllegalStateException("Ne mogu čitati datoteku")
    }
}
