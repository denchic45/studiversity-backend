package com.studiversity.feature.course.submission.model

import kotlinx.serialization.Serializable


@Serializable
sealed class SubmissionValidation<T : SubmissionContent> {

    abstract val conditions: List<(content: T) -> String?>

    fun validate(submission: T): String? {
        for (condition in conditions) {
            val result = condition(submission)
            if (result != null) return result
        }
        return null
    }

    protected fun requireCondition(condition: Boolean, message: String): String? {
        if (!condition) return message
        return null
    }
}

object SubmissionErrors {
    const val TEXT_DISABLED = "TEXT_DISABLED"
    const val MATERIALS_DISABLED = "MATERIAL_DISABLED"
    const val TEXT_REQUIRED = "TEXT_REQUIRED"
    const val TEXT_LIMIT_EXCEEDED = "TEXT_LIMIT_EXCEEDED"
    const val MATERIALS_REQUIRED = "MATERIALS_REQUIRED"
}

enum class Requirement { Disabled, Optional, Required }

@Serializable
data class AssignmentSubmissionValidation(
    val containsText: Requirement,
    val containsMaterials: Requirement
) : SubmissionValidation<AssignmentSubmission>() {
    override val conditions: List<(content: AssignmentSubmission) -> String?> = listOf(
        { content ->
            when (containsText) {
                Requirement.Disabled -> requireCondition(
                    content.text == null,
                    SubmissionErrors.TEXT_DISABLED
                )

                Requirement.Optional -> null
                Requirement.Required -> requireCondition(
                    content.text != null,
                    SubmissionErrors.TEXT_REQUIRED
                ) ?: requireCondition(
                    content.text!!.length <= 500,
                    SubmissionErrors.TEXT_LIMIT_EXCEEDED
                )
            }
        },
        { content ->
            when (containsMaterials) {
                Requirement.Disabled -> requireCondition(
                    content.materialIds.isEmpty(),
                    SubmissionErrors.MATERIALS_DISABLED
                )

                Requirement.Optional -> null
                Requirement.Required -> requireCondition(
                    content.materialIds.isNotEmpty(),
                    SubmissionErrors.MATERIALS_REQUIRED
                )
            }
        }
    )
}