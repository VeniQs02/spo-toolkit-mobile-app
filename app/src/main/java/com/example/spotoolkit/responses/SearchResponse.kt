import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("artists") val artists: ArtistsContainer? = null,
    @SerializedName("tracks") val tracks: TracksContainer? = null,
    @SerializedName("albums") val albums: AlbumsContainer? = null,
    @SerializedName("playlists") val playlists: PlaylistsContainer? = null,
    @SerializedName("shows") val shows: ShowsContainer? = null,
    @SerializedName("episodes") val episodes: EpisodesContainer? = null,
    @SerializedName("audiobooks") val audiobooks: AudiobooksContainer? = null
)

data class ArtistsContainer(@SerializedName("items") val items: List<Artist>? = emptyList())
data class TracksContainer(@SerializedName("items") val items: List<Track>? = emptyList())
data class AlbumsContainer(@SerializedName("items") val items: List<Album>? = emptyList())
data class PlaylistsContainer(@SerializedName("items") val items: List<Playlist>? = emptyList())
data class ShowsContainer(@SerializedName("items") val items: List<Show>? = emptyList())
data class EpisodesContainer(@SerializedName("items") val items: List<Episode>? = emptyList())
data class AudiobooksContainer(@SerializedName("items") val items: List<Audiobook>? = emptyList())

data class Artist(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("popularity") val popularity: Int? = 0,
    @SerializedName("genres") val genres: List<String>? = emptyList(),
    @SerializedName("followers") val followers: Followers? = null,
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class Track(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("duration_ms") val durationMs: Int? = 0,
    @SerializedName("album") val album: AlbumSimple? = null,
    @SerializedName("artists") val artists: List<ArtistSimple>? = emptyList(),
    @SerializedName("preview_url") val previewUrl: String? = null,
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

val Track.primaryArtistName: String
    get() = artists?.firstOrNull()?.name ?: "Unknown"

val Track.albumArtUrl: String?
    get() = album?.images?.firstOrNull()?.url

data class Album(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("album_type") val albumType: String? = null,
    @SerializedName("total_tracks") val totalTracks: Int? = 0,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("artists") val artists: List<ArtistSimple>? = emptyList(),
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class Playlist(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("owner") val owner: Owner? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("tracks") val tracks: PlaylistTracks? = null,
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class Show(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("publisher") val publisher: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("total_episodes") val totalEpisodes: Int? = 0,
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class Episode(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("duration_ms") val durationMs: Int? = 0,
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap(),
    @SerializedName("audio_preview_url") val audioPreviewUrl: String? = null
)

data class Audiobook(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("authors") val authors: List<Author>? = emptyList(),
    @SerializedName("narrators") val narrators: List<Author>? = emptyList(),
    @SerializedName("description") val description: String? = null,
    @SerializedName("images") val images: List<Image>? = emptyList(),
    @SerializedName("publisher") val publisher: String? = null,
    @SerializedName("total_chapters") val totalChapters: Int? = 0,
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class Image(
    @SerializedName("url") val url: String? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("width") val width: Int? = null
)

data class Followers(@SerializedName("total") val total: Int? = 0)

data class Owner(
    @SerializedName("id") val id: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("external_urls") val externalUrls: Map<String, String>? = emptyMap()
)

data class PlaylistTracks(@SerializedName("total") val total: Int? = 0)

data class AlbumSimple(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("images") val images: List<Image>? = emptyList()
)

data class ArtistSimple(
    @SerializedName("id") val id: String? = null, @SerializedName("name") val name: String? = null
)

data class Author(@SerializedName("name") val name: String? = null)


sealed class SearchResultItem {
    data class ArtistItem(val artist: Artist) : SearchResultItem()
    data class TrackItem(val track: Track) : SearchResultItem()
    data class AlbumItem(val album: Album) : SearchResultItem()
    data class PlaylistItem(val playlist: Playlist) : SearchResultItem()
    data class ShowItem(val show: Show) : SearchResultItem()
    data class EpisodeItem(val episode: Episode) : SearchResultItem()
}
