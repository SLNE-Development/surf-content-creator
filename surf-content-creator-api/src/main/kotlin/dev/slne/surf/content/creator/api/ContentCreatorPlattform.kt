package dev.slne.surf.content.creator.api

import dev.slne.surf.content.creator.api.plattform.PlattformState
import dev.slne.surf.content.creator.api.plattform.PlattformType

/**
 * Represents a content creator plattform
 *
 * @param name The name of the plattform
 * @param plattform The type of the plattform
 * @param state The state of the plattform
 */
data class ContentCreatorPlattform(
    val name: String,
    val plattform: PlattformType,
    var state: PlattformState
)
