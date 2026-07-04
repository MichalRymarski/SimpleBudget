package prayit.simplebudget.core.utils

import androidx.compose.ui.tooling.preview.Preview

// ── Device Specs ──────────────────────────────────────────────────────────────

private object PreviewDevices {
    const val SMALL_PHONE = "spec:width=360dp,height=640dp,dpi=420"
    const val LARGE_PHONE = "spec:width=412dp,height=915dp,dpi=420"
    const val LARGE_PHONE_LANDSCAPE = "spec:width=915dp,height=412dp,dpi=420"
    const val TABLET = "spec:width=800dp,height=1280dp,dpi=240"
    const val TABLET_LANDSCAPE = "spec:width=1280dp,height=800dp,dpi=240"
}

private const val NIGHT_YES = 0x20 // NIGHT_YES

// ── Phone Previews ───────────────────────────────────────────────────────────

@Preview(device = PreviewDevices.SMALL_PHONE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true)
@Preview(device = PreviewDevices.SMALL_PHONE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true, uiMode = NIGHT_YES)
annotation class PhonePreviews

// ── Tablet Previews ──────────────────────────────────────────────────────────

@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.TABLET, showBackground = true)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
annotation class TabletPreviews

// ── All Device Previews ──────────────────────────────────────────────────────

@Preview(device = PreviewDevices.SMALL_PHONE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.TABLET, showBackground = true)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.SMALL_PHONE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
annotation class AllDevicePreviews

// ── Orientation Previews ──────────────────────────────────────────────────────

@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
annotation class PhonePortraitLandscapePreview

@Preview(device = PreviewDevices.TABLET, showBackground = true)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.TABLET, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
annotation class TabletPortraitLandscapePreview

@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.TABLET, showBackground = true)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true)
@Preview(device = PreviewDevices.LARGE_PHONE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.LARGE_PHONE_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET, showBackground = true, uiMode = NIGHT_YES)
@Preview(device = PreviewDevices.TABLET_LANDSCAPE, showBackground = true, uiMode = NIGHT_YES)
annotation class AllDevicePreviewsWithOrientation
