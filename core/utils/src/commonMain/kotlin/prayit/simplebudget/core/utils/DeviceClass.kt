package prayit.simplebudget.core.utils

enum class DeviceClass {
    PhonePortrait,
    TabletPortrait,
    TabletLandscape,
    LargeTabletDesktop;

    val isCompact get() = this == PhonePortrait
    val isMedium get() = this == TabletPortrait
    val isExpanded get() = this == TabletLandscape || this == LargeTabletDesktop
}
