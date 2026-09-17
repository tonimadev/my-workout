package digital.tonima.myworkout.data.wearable

import kotlinx.serialization.json.Json

/**
 * Json instance shared by every phone<->watch sync payload. The two APKs are updated
 * independently (a phone update doesn't imply the watch got reinstalled, and vice versa), so a
 * payload can carry fields the other side's currently-installed build doesn't know about yet.
 * ignoreUnknownKeys keeps that a no-op instead of failing the whole decode.
 */
val WearableJson = Json { ignoreUnknownKeys = true }
