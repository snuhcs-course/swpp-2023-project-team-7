package com.example.readability.data.user

// top class for all user exceptions
open class UserException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Throwable()

// exception for when the user is not signed in
class UserNotSignedInException : UserException("User is not signed in")
