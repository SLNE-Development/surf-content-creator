package dev.slne.surf.content.creator.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.core.CoreContentCreatorApi
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class VelocityContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = plugin.dataPath
}