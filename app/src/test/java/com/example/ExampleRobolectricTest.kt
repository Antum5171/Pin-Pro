package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LatLng
import com.example.data.model.isPointInPolygon
import com.example.data.repository.PinProRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        assertEquals("PinPro", appName)
    }

    @Test
    fun `point in polygon correctly detects barangay boundary`() {
        val polygon = listOf(
            LatLng(14.6590, 121.0440),
            LatLng(14.6590, 121.0550),
            LatLng(14.6480, 121.0550),
            LatLng(14.6480, 121.0440)
        )
        // Inside point
        val insidePoint = LatLng(14.6530, 121.0500)
        assertTrue(isPointInPolygon(insidePoint, polygon))
    }

    @Test
    fun `repository initializes with seeded barangays and accounts`() {
        val repo = PinProRepository()
        assertEquals(2, repo.barangays.value.size)
        assertTrue(repo.users.value.isNotEmpty())
        assertTrue(repo.tanods.value.isNotEmpty())
        assertNotNull(repo.currentUser.value)
    }

    @Test
    fun `repository contains dangerous animal hazard report pins with pending status`() {
        val repo = PinProRepository()
        val animalPins = repo.animalReportPins.value
        assertTrue("Must contain initial dangerous animal reports", animalPins.isNotEmpty())
        val snakePin = animalPins.firstOrNull { it.animalType == com.example.data.model.AnimalType.SNAKE }
        val dogPin = animalPins.firstOrNull { it.animalType == com.example.data.model.AnimalType.AGGRESSIVE_DOG }
        assertNotNull("Must contain snake hazard report", snakePin)
        assertNotNull("Must contain aggressive dog hazard report", dogPin)
        assertEquals(com.example.data.model.PinStatus.PENDING, snakePin?.status)
    }

    @Test
    fun `dropping dangerous animal pin adds new location pin with pending status`() {
        val repo = PinProRepository()
        val initialCount = repo.animalReportPins.value.size
        repo.addAnimalReportPin(
            animalType = com.example.data.model.AnimalType.SNAKE,
            label = "Viper Spotted Near Drainage",
            description = "Coiled under metal drain cover",
            xRatio = 0.65f,
            yRatio = 0.55f,
            locationName = "Drainage Gate 2"
        )
        assertEquals(initialCount + 1, repo.animalReportPins.value.size)
        val added = repo.animalReportPins.value.first()
        assertEquals("Viper Spotted Near Drainage", added.label)
        assertEquals(com.example.data.model.AnimalType.SNAKE, added.animalType)
        assertEquals(com.example.data.model.PinStatus.PENDING, added.status)
    }

    @Test
    fun `marking animal pin as done and toggling status updates appropriately`() {
        val repo = PinProRepository()
        val pendingPin = repo.animalReportPins.value.first { it.status == com.example.data.model.PinStatus.PENDING }
        
        // Mark as done
        repo.markPinAsDone(pendingPin.id)
        val donePin = repo.animalReportPins.value.first { it.id == pendingPin.id }
        assertEquals(com.example.data.model.PinStatus.DONE, donePin.status)
        assertNotNull(donePin.resolvedAt)

        // Toggle back to pending
        repo.togglePinStatus(pendingPin.id)
        val reOpenedPin = repo.animalReportPins.value.first { it.id == pendingPin.id }
        assertEquals(com.example.data.model.PinStatus.PENDING, reOpenedPin.status)
    }

    @Test
    fun `toggling pending threats filter updates state`() {
        val repo = PinProRepository()
        assertFalse(repo.isPendingOnlyFilterActive.value)
        repo.togglePendingOnlyFilter()
        assertTrue(repo.isPendingOnlyFilterActive.value)
    }
}
