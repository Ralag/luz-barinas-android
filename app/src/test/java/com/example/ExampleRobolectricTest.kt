package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SectorEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PAC Barinas", appName)
  }

  @Test
  fun `verify room sector dao insert and retrieve`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val dao = db.sectorDao()

    val testSector = SectorEntity(
      id = "test_sector_don_samuel",
      name = "Don Samuel",
      circuitCode = "Don Samuel 13.8kV",
      status = "NORMAL",
      voltage = 120.0f,
      confirmedReportsCount = 10,
      withoutPowerPercentage = 0,
      lastUpdatedMillis = System.currentTimeMillis(),
      rotationBlock = "Bloque A",
      polygonPointsRaw = "8.63,-70.24;8.64,-70.23;8.62,-70.22"
    )

    dao.insertOrUpdateSector(testSector)
    val retrieved = dao.getSectorById("test_sector_don_samuel")
    assertNotNull(retrieved)
    assertEquals("Don Samuel", retrieved?.name)
    assertEquals("Bloque A", retrieved?.rotationBlock)
  }
}

