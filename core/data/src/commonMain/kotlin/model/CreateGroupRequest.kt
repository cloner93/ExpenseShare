package model
import kotlinx.serialization.Serializable


@Serializable
data class CreateGroupRequest(val name: String, val memberIds: List<Int>)
