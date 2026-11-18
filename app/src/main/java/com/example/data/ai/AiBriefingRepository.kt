package com.example.data.ai

import com.example.i18n.AppLanguage

data class AiBriefingResult(
    val summary: String,
    val isPlaceholder: Boolean
)

/**
 * Abstraction for generative executive shift briefings.
 * Because Firebase setup was declined, this uses a clearly labeled offline placeholder
 * backed by aggregated deterministic risk metrics rather than calling an external LLM API.
 */
interface AiBriefingRepository {
    suspend fun generateShiftBriefing(
        organizationName: String,
        urgentCount: Int,
        elevatedCount: Int,
        peakTemperatureC: Double?,
        peakAqi: Int?,
        language: AppLanguage
    ): AiBriefingResult
}

class LocalPlaceholderAiBriefingRepository : AiBriefingRepository {
    override suspend fun generateShiftBriefing(
        organizationName: String,
        urgentCount: Int,
        elevatedCount: Int,
        peakTemperatureC: Double?,
        peakAqi: Int?,
        language: AppLanguage
    ): AiBriefingResult {
        val tempStr = peakTemperatureC?.let { "${it}°C" } ?: "N/A"
        val aqiStr = peakAqi?.toString() ?: "N/A"
        val text = when (language) {
            AppLanguage.HI ->
                "[सिम्युलेटेड ऑफ़लाइन ब्रीफिंग] $organizationName: $urgentCount तत्काल और $elevatedCount बढ़ी हुई चेतावनियाँ सक्रिय हैं। अधिकतम तापमान: $tempStr, अधिकतम AQI: $aqiStr। सुझाव: दोपहर के चरम समय में 15 मिनट का छाया-विश्राम और ORS हाइड्रेशन अनिवार्य करें।"
            AppLanguage.TE ->
                "[సిమ్యులేటెడ్ ఆఫ్‌లైన్ బ్రీఫింగ్] $organizationName: $urgentCount తక్షణ మరియు $elevatedCount అధిక హెచ్చరికలు ఉన్నాయి. గరిష్ట ఉష్ణోగ్రత: $tempStr, గరిష్ట AQI: $aqiStr. సూచన: మధ్యాహ్నం నీడ విశ్రాంతి & ORS నీటి విరామాలను అమలు చేయండి."
            AppLanguage.UR ->
                "[سمیولیٹڈ آف لائن بریفنگ] $organizationName: $urgentCount فوری اور $elevatedCount بلند انتباہات فعال ہیں۔ زیادہ سے زیادہ درجہ حرارت: $tempStr، زیادہ سے زیادہ AQI: $aqiStr۔ تجویز: دوپہر کے وقت 15 منٹ سائے میں آرام اور ORS پانی لازمی قرار دیں۔"
            AppLanguage.EN ->
                "[Sample Placeholder Briefing] $organizationName currently has $urgentCount Urgent and $elevatedCount Elevated alerts (Peak temp: $tempStr, Peak AQI: $aqiStr). Recommended shift priority: enforce hourly shaded hydration breaks, rotate heavy thermal tasks, and verify respiratory PPE where AQI > 150."
        }
        return AiBriefingResult(summary = text, isPlaceholder = true)
    }
}
