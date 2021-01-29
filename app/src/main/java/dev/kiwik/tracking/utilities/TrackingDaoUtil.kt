package dev.kiwik.tracking.utilities

import androidx.sqlite.db.SimpleSQLiteQuery

    object TrackingDaoUtil {
        fun deleteNotSyncQuery(ids: Array<Int>) : SimpleSQLiteQuery {
            val codes = ids.joinToString(",")
            val query = "DELETE FROM m_tracking WHERE id in ($codes)"
            return SimpleSQLiteQuery(query)
        }
    }

object TrackingBaseDaoUtil {
    fun deleteNotSyncQuery(ids: Array<Int>) : SimpleSQLiteQuery {
        val codes = ids.joinToString(",")
        val query = "DELETE FROM m_tracking WHERE id in ($codes)"
        return SimpleSQLiteQuery(query)
    }
}