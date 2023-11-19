package com.example.readability.data.user

// top class for all user exceptions
open class UserException : Throwable() {}

// exception for when the user is not signed in
class UserNotSignedInException : UserException() {}
