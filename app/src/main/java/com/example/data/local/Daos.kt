package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IconThemeDao {
    @Query("SELECT * FROM icon_themes ORDER BY isActive DESC, isPreset DESC, updatedAt DESC")
    fun observeAllThemes(): Flow<List<IconThemeEntity>>

    @Query("SELECT * FROM icon_themes WHERE isActive = 1 LIMIT 1")
    fun observeActiveTheme(): Flow<IconThemeEntity?>

    @Query("SELECT * FROM icon_themes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTheme(): IconThemeEntity?

    @Query("SELECT * FROM icon_themes WHERE id = :id LIMIT 1")
    suspend fun getThemeById(id: String): IconThemeEntity?

    @Query("SELECT COUNT(*) FROM icon_themes")
    suspend fun countThemes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: IconThemeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThemes(themes: List<IconThemeEntity>)

    @Update
    suspend fun updateTheme(theme: IconThemeEntity)

    @Query("UPDATE icon_themes SET isActive = CASE WHEN id = :themeId THEN 1 ELSE 0 END")
    suspend fun setActiveTheme(themeId: String)

    @Query("DELETE FROM icon_themes WHERE id = :themeId AND isPreset = 0")
    suspend fun deleteCustomTheme(themeId: String)
}

@Dao
interface ThemedAppDao {
    @Query("SELECT * FROM themed_apps ORDER BY isNewlyDetected DESC, appName COLLATE NOCASE ASC")
    fun observeAllApps(): Flow<List<ThemedAppEntity>>

    @Query("SELECT * FROM themed_apps ORDER BY appName COLLATE NOCASE ASC")
    suspend fun getAllApps(): List<ThemedAppEntity>

    @Query("SELECT * FROM themed_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): ThemedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: ThemedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<ThemedAppEntity>)

    @Query("DELETE FROM themed_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM themed_apps WHERE packageName NOT IN (:activePackages)")
    suspend fun deleteRemovedApps(activePackages: List<String>)
}

@Dao
interface DetectionEventDao {
    @Query("SELECT * FROM detection_events ORDER BY timestamp DESC LIMIT 50")
    fun observeRecentEvents(): Flow<List<DetectionEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DetectionEventEntity)

    @Query("DELETE FROM detection_events")
    suspend fun clearEvents()
}
