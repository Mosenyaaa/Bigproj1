import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PostResponseDto(

    @SerialName("is_ok") val isOk: Boolean,
    val datetime: String
)