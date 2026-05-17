package org.milad.expense_share.utils

object Messages {
    // Auth
    const val REGISTRATION_FAILED = "Registration failed"
    const val INVALID_CREDENTIALS = "Invalid credentials"
    const val INVALID_TOKEN = "Invalid token"

    // Groups
    const val CREATE_GROUP_FAILED = "Failed to create group"
    const val NOT_GROUP_OWNER = "Only group owner can perform this action"
    const val FETCH_DATA_FAILED = "Failed to fetch data"
    const val INVALID_GROUP_ID = "Invalid group ID"
    const val GROUP_NOT_FOUND = "Group not found"

    // Friends
    const val FETCH_FRIENDS_FAILED = "Failed to fetch friends"
    const val FETCH_REQUESTS_FAILED = "Failed to fetch requests"
    const val FETCH_BLOCKED_FAILED = "Failed to fetch blocked users"
    const val PHONE_REQUIRED = "Phone number is required"
    const val NO_FRIENDSHIP_FOUND = "No friendship found"
    const val STATUS_FAILED = "Failed to get status"
    const val ACTION_FAILED = "Action failed"
    const val INVALID_STATUS = "Invalid status. Valid values: PENDING, ACCEPTED, BLOCKED, REJECTED"
}

object ErrorCodes {
    const val REGISTER_FAILED = "REGISTER_FAILED"
    const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    const val INVALID_TOKEN = "INVALID_TOKEN"
    const val CREATE_GROUP_FAILED = "CREATE_GROUP_FAILED"
    const val NOT_GROUP_OWNER = "NOT_GROUP_OWNER"
    const val FETCH_DATA_FAILED = "FETCH_DATA_FAILED"
    const val INVALID_GROUP_ID = "INVALID_GROUP_ID"
    const val INVALID_STATUS = "INVALID_STATUS"
    const val FETCH_FAILED = "FETCH_FAILED"
    const val PHONE_REQUIRED = "PHONE_REQUIRED"
    const val NOT_FOUND = "NOT_FOUND"
    const val STATUS_FAILED = "STATUS_FAILED"
    const val ALREADY_EXISTS = "ALREADY_EXISTS"
    const val INVALID_ACTION = "INVALID_ACTION"
}
