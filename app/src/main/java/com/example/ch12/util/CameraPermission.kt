package com.example.ch12.util

// Alur izin pada Bab 12 sebagai fungsi murni. Composable hanya mengumpulkan
// fakta dari sistem (sudah diizinkan? perlu rationale?) lalu menjalankan hasilnya.
enum class PermissionAction {
    OpenCamera,         // izin ada: langsung pakai
    ShowRationale,      // pernah ditolak: jelaskan alasannya dulu
    RequestPermission,  // tampilkan dialog sistem
    ShowDenied,         // baru saja ditolak: tawarkan galeri sebagai alternatif
    OpenSettings        // ditolak permanen: dialog sistem tidak akan muncul lagi
}

// Dipanggil saat pengguna mengetuk "Kamera", yaitu DALAM KONTEKS fitur.
// Izin diperiksa setiap kali: bisa saja dicabut dari Pengaturan atau
// hanya diberikan "Hanya kali ini".
fun cameraClickAction(isGranted: Boolean, shouldShowRationale: Boolean): PermissionAction = when {
    isGranted -> PermissionAction.OpenCamera
    shouldShowRationale -> PermissionAction.ShowRationale
    else -> PermissionAction.RequestPermission
}

// Dipanggil dengan hasil RequestPermission(). Setelah ditolak, sistem
// menyalakan shouldShowRationale. Jika ditolak tetapi rationale MATI,
// berarti pengguna menolak permanen (atau sistem tidak lagi menampilkan dialog).
fun permissionResultAction(isGranted: Boolean, shouldShowRationale: Boolean): PermissionAction = when {
    isGranted -> PermissionAction.OpenCamera
    shouldShowRationale -> PermissionAction.ShowDenied      // masih bisa dijelaskan
    else -> PermissionAction.OpenSettings                   // dialog sistem tidak akan muncul lagi
}
