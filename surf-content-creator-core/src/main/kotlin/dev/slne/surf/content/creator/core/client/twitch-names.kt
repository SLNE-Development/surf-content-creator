package dev.slne.surf.content.creator.core.client

private val VALID_TWITCH_NAME_REGEX = "^[a-zA-Z0-9_]{4,25}$".toRegex()

/**
 * Whether [name] can be a twitch login name.
 */
internal fun isValidTwitchName(name: String) = name.matches(VALID_TWITCH_NAME_REGEX)

/**
 * Splits [names] into the ones that can be a twitch login name and the ones that cannot.
 */
internal fun partitionTwitchNames(names: List<String>): TwitchNamePartition {
    val (valid, invalid) = names.partition { isValidTwitchName(it) }

    return TwitchNamePartition(valid, invalid)
}

internal data class TwitchNamePartition(val valid: List<String>, val invalid: List<String>)
