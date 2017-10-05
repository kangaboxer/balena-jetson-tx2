SUMMARY = "Prepare bsp binaries for flashing"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${RESIN_COREBASE}/COPYING.Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

DEPENDS_class-native = "tegra-binaries"

inherit deploy native
SRC_URI = "file://flash.xml"

SHARED = "${TMPDIR}/work-shared/L4T-${SOC_FAMILY}-${PV}-${PR}/Linux_for_Tegra"
B = "${WORKDIR}/build"
S = "${WORKDIR}"

do_compile() {
    tegrahost="${SHARED}/bootloader/tegrahost_v2"
    tegraparser="${SHARED}/bootloader/tegraparser_v2"
    tegrasign="${SHARED}/bootloader/tegrasign_v2"

    files=" \
        ${SHARED}/bootloader/mce_mts_d15_prod_cr.bin \ 
        ${SHARED}/bootloader/cboot.bin \
        ${SHARED}/bootloader/tos.img \
        ${SHARED}/bootloader/eks.img \
        ${SHARED}/bootloader/bpmp.bin \
        ${SHARED}/bootloader/camera-rtcpu-sce.bin \
        ${SHARED}/bootloader/t186ref/warmboot.bin \
        ${SHARED}/bootloader/t186ref/tegra186-a02-bpmp-quill-p3310-1000-c01-00-te770d-ucm2.dtb \
        ${SHARED}/kernel/dtb/tegra186-quill-p3310-1000-c03-00-base.dtb \
        "

    for file in $files; do
        cp $file ${B}
    done
    
    cp ${WORKDIR}/flash.xml ${B}

    ${tegraparser} --pt flash.xml
    ${tegrahost} --chip 0x18 --partitionlayout flash.bin --list images_list.xml zerosbk
    ${tegrasign} --key None --list images_list.xml --pubkeyhash pub_key.key
    ${tegrahost} --chip 0x18 --partitionlayout flash.bin --updatesig images_list_signed.xml
}

do_deploy() {
    install -d ${DEPLOYDIR}/tegra-binaries-signed
    cp ${B}/*.encrypt ${DEPLOYDIR}/tegra-binaries-signed
}

INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_configure[depends] += "tegra-binaries:do_preconfigure"
do_populate_lic[depends] += "tegra-binaries:do_unpack"

addtask do_deploy before do_package after do_install
