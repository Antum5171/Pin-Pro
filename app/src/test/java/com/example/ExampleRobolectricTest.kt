package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LatLng
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus
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
            LatLng(14.6900, 120.5200),
            LatLng(14.6900, 120.5400),
            LatLng(14.6650, 120.5400),
            LatLng(14.6650, 120.5200)
        )
        // Inside point
        val insidePoint = LatLng(14.6762, 120.5375)
        assertTrue(isPointInPolygon(insidePoint, polygon))
    }

    @Test
    fun `repository initializes with seeded barangays and zero mock reports`() {
        val repo = PinProRepository()
        assertEquals(2, repo.barangays.value.size)
        assertTrue(repo.users.value.isNotEmpty())
        assertTrue("Initial repository reports must be empty (0 mock data)", repo.animalReportPins.value.isEmpty())
    }

    @Test
    fun `submitting report adds new location pin with pending status and event log`() {
        val repo = PinProRepository()
        assertEquals(0, repo.animalReportPins.value.size)

        repo.submitReport(
            title = "Dangerous Snake Spotted",
            category = ReportCategory.DANGEROUS_ANIMALS,
            description = "Venomous cobra near drainage",
            latLng = LatLng(14.6762, 120.5375),
            address = "Poblacion Sector 1"
        )

        assertEquals(1, repo.animalReportPins.value.size)
        val added = repo.animalReportPins.value.first()
        assertEquals("Dangerous Snake Spotted", added.title)
        assertEquals(ReportCategory.DANGEROUS_ANIMALS, added.category)
        assertEquals(ReportStatus.PENDING, added.status)
        assertTrue("Event log must contain submitted event", repo.reportEvents.value.isNotEmpty())
    }

    @Test
    fun `official response flow updates status to accepted, arrived, and resolved`() {
        val repo = PinProRepository()
        repo.submitReport(
            title = "Street Altercation",
            category = ReportCategory.FIGHTS,
            description = "Loud fight near market",
            latLng = LatLng(14.6762, 120.5375),
            address = "Balanga Market"
        )

        val reportId = repo.animalReportPins.value.first().id

        // Accept
        repo.acceptReport(reportId, "Officer Berto Aquino")
        var report = repo.animalReportPins.value.first()
        assertTrue(report.status == ReportStatus.ACCEPTED || report.status == ReportStatus.ON_THE_WAY)
        assertEquals("Officer Berto Aquino", report.assignedOfficialName)

        // Arrived
        repo.markArrived(reportId, "Officer Berto Aquino")
        report = repo.animalReportPins.value.first()
        assertEquals(ReportStatus.ARRIVED, report.status)

        // Resolve
        repo.resolveReport(reportId, "Officer Berto Aquino", "Crowd dispersed and area secured")
        report = repo.animalReportPins.value.first()
        assertEquals(ReportStatus.RESOLVED, report.status)
        assertNotNull(report.resolvedAt)
    }

    @Test
    fun `toggling pending filter updates filter state`() {
        val repo = PinProRepository()
        assertFalse(repo.isPendingOnlyFilterActive.value)
        repo.togglePendingOnlyFilter()
        assertTrue(repo.isPendingOnlyFilterActive.value)
    }

    @Test
    fun `confirming user location sets active barangay and updates state`() {
        val repo = PinProRepository()
        assertFalse(repo.isLocationConfirmed.value)

        val sampleHall = repo.barangays.value.first { it.id == "sample_hall" }
        val loc = LatLng(14.6762, 120.5375)

        repo.confirmUserLocation(loc, sampleHall, "12 Acacia St.")
        assertTrue(repo.isLocationConfirmed.value)
        assertEquals("sample_hall", repo.confirmedBarangay.value?.id)
        assertEquals(loc, repo.confirmedLocation.value)

        repo.requestLocationReSelection()
        assertFalse(repo.isLocationConfirmed.value)
    }
}
