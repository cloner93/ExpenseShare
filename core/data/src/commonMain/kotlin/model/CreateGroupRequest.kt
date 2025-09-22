package model

data class CreateGroupRequest(val name: String, val memberIds: List<String>)
