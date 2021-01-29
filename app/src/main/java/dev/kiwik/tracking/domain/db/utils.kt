package dev.kiwik.tracking.domain.db

import androidx.sqlite.db.SimpleSQLiteQuery

object IncidentDaoUtil {
    fun deleteByIds(ids: List<Int>) : SimpleSQLiteQuery {
        val codes = ids.joinToString(",")
        val query = "DELETE FROM m_incident where incidentId in ($codes)"
        return SimpleSQLiteQuery(query)
    }
}