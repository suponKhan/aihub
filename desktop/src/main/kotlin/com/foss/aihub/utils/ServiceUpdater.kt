package com.foss.aihub.pc

import com.foss.aihub.pc.models.AiService
import com.foss.aihub.pc.models.ModifiedServiceInfo
import com.foss.aihub.pc.models.UpdateResult
import com.foss.aihub.pc.utils.CLOUD_BASE_URL
import com.foss.aihub.pc.utils.AI_SERVICES_FILE
import com.foss.aihub.pc.utils.DOMAINS_FILE
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File

suspend fun performServiceUpdate(
    servicesFile: File,
    domainsFile: File,
    settingsManager: SettingsManager
): UpdateResult? = withContext(Dispatchers.IO) {
    val oldServices = loadServices(servicesFile)
    CloudDataHandler.updateAiServices(servicesFile)
    val newServices = loadServices(servicesFile)

    val oldMap = oldServices.associateBy { it.name }
    val newMap = newServices.associateBy { it.name }

    val added = newServices.filter { it.name !in oldMap.keys }
    val removed = oldServices.filter { it.name !in newMap.keys }

    val modified = buildList {
        oldServices.forEach { old ->
            newMap[old.name]?.let { new ->
                val changes = mutableListOf<String>()
                if (old.url != new.url) changes.add("URL: ${old.url} → ${new.url}")
                if (old.pricing != new.pricing) changes.add("Pricing: ${old.pricing} → ${new.pricing}")
                if (old.privacy != new.privacy) changes.add("Privacy: ${old.privacy} → ${new.privacy}")
                if (old.loginRequired != new.loginRequired) {
                    val oldVal = if (old.loginRequired) "Yes" else "No"
                    val newVal = if (new.loginRequired) "Yes" else "No"
                    changes.add("Login Required: $oldVal → $newVal")
                }
                if (old.bestFor != new.bestFor) {
                    val oldStr = old.bestFor.joinToString(", ")
                    val newStr = new.bestFor.joinToString(", ")
                    changes.add("Best For: $oldStr → $newStr")
                }
                if (changes.isNotEmpty()) {
                    add(ModifiedServiceInfo(new, changes.map { it.capitalizeFirstLetter() }))
                }
            }
        }
    }

    val newCategoriesSet =
        (newServices.map { it.category }.toSet() - oldServices.map { it.category }.toSet())

    val hasChanges =
        added.isNotEmpty() || removed.isNotEmpty() || modified.isNotEmpty() || newCategoriesSet.isNotEmpty()
    if (!hasChanges) return@withContext null

    settingsManager.updateSettings { currentSettings ->
        val newEnabled = currentSettings.enabledServices.toMutableSet()
        val newOrder = currentSettings.serviceOrder.toMutableList()

        removed.forEach { service ->
            newEnabled.remove(service.name)
            newOrder.removeAll { it == service.name }
        }

        added.forEach { service ->
            if (!newOrder.contains(service.name)) {
                newOrder.add(service.name)
            }
        }

        if (currentSettings.enableNewServicesByDefault) {
            val preferredCategories = currentSettings.preferredCategories
            val preferredPrices = currentSettings.preferredPrices
            val preferredPrivacy = currentSettings.preferredPrivacy
            val preferredLoginRequired = currentSettings.preferredLoginRequired

            added.forEach { service ->
                val catOk =
                    preferredCategories.isEmpty() || preferredCategories.contains(service.category)
                val priceOk = preferredPrices.isEmpty() || preferredPrices.contains(service.pricing)
                val privacyOk =
                    preferredPrivacy.isEmpty() || preferredPrivacy.contains(service.privacy)
                val loginOk =
                    preferredLoginRequired == null || service.loginRequired == preferredLoginRequired
                if (catOk && priceOk && privacyOk && loginOk) {
                    newEnabled.add(service.name)
                }
            }
        }

        currentSettings.enabledServices = newEnabled
        currentSettings.serviceOrder = newOrder
    }

    UpdateResult(added, removed, modified, newCategoriesSet)
}