package dev.slne.surf.content.creator.minestom.api

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.core.CoreContentCreatorApi
import dev.slne.surf.content.creator.minestom.ContentCreatorMinestomEntrypoint
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class MinestomContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = ContentCreatorMinestomEntrypoint.dataPath
}
