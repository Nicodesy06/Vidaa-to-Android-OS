# Product makefile stub for a hypothetical AOSP/Android TV build on the Hisense
# 100E7Q PRO (MT9618). Inherits Android TV defaults and bundles the OCL app.
# The vendor HALs this product REQUIRES but does not have are listed as blockers in
# docs/blockers.md (B2/B3). Do not treat this as buildable-to-flash.
$(call inherit-product, device/google/atv/products/atv_base.mk)

PRODUCT_NAME := e7qpro
PRODUCT_DEVICE := e7qpro
PRODUCT_BRAND := Hisense
PRODUCT_MODEL := 100E7Q PRO

# OEM Compatibility Layer (buildable today).
PRODUCT_PACKAGES += HisenseOcl

# REQUIRED, NOT AVAILABLE (proprietary MediaTek/Hisense BSP) — see docs/blockers.md:
#   android.hardware.tv.input@*        (HDMI inputs + tuner)   [BLOCKED B2]
#   android.hardware.tv.cec@*          (HDMI-CEC)
#   android.hardware.audio@* (eARC)    (audio + passthrough)   [BLOCKED]
#   vendor MEMC/HDR/DV picture stack                            [BLOCKED B3]
