package com.example.ch12.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraPermissionTest {

    @Test fun `izin sudah ada - langsung buka kamera`() {
        assertEquals(PermissionAction.OpenCamera, cameraClickAction(isGranted = true, shouldShowRationale = false))
    }

    @Test fun `pertama kali - minta izin ke sistem`() {
        assertEquals(PermissionAction.RequestPermission, cameraClickAction(isGranted = false, shouldShowRationale = false))
    }

    @Test fun `pernah ditolak - jelaskan alasan dulu`() {
        assertEquals(PermissionAction.ShowRationale, cameraClickAction(isGranted = false, shouldShowRationale = true))
    }

    @Test fun `hasil diizinkan - buka kamera`() {
        assertEquals(PermissionAction.OpenCamera, permissionResultAction(isGranted = true, shouldShowRationale = false))
    }

    @Test fun `ditolak sekali - tawarkan galeri`() {
        assertEquals(PermissionAction.ShowDenied, permissionResultAction(isGranted = false, shouldShowRationale = true))
    }

    @Test fun `ditolak permanen - arahkan ke Pengaturan`() {
        assertEquals(PermissionAction.OpenSettings, permissionResultAction(isGranted = false, shouldShowRationale = false))
    }
}
