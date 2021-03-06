/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.error

import com.intellij.errorreport.bean.ErrorBean
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.util.SystemProperties

object IdeaITNProxy {
    fun getKeyValuePairs(error: ErrorBean,
                         appInfo: ApplicationInfoEx,
                         namesInfo: ApplicationNamesInfo): LinkedHashMap<String, String?> {
        val params = LinkedHashMap<String, String?>(21)

        params["error.description"] = error.description

        params["Plugin Name"] = error.pluginName
        params["Plugin Version"] = error.pluginVersion

        params["OS Name"] = SystemProperties.getOsName()
        params["Java Version"] = SystemProperties.getJavaVersion()
        params["Java VM Vendor"] = SystemProperties.getJavaVmVendor()

        params["App Name"] = namesInfo.productName
        params["App Full Name"] = namesInfo.fullProductName
        params["App Version Name"] = appInfo.versionName
        params["Is EAP"] = appInfo.isEAP.toString()
        params["App Build"] = appInfo.build.asString()
        params["App Version"] = appInfo.fullVersion

        if (error.lastAction.isNullOrBlank()) {
            params["Last Action"] = "None"
        } else {
            params["Last Action"] = error.lastAction
        }

        params["error.message"] = error.message
        params["error.stacktrace"] = error.stackTrace

        for (attachment in error.attachments) {
            params["attachment.name"] = attachment.name
            params["attachment.value"] = attachment.encodedBytes
        }

        return params
    }
}
