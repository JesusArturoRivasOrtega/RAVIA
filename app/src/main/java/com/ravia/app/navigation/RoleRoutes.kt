package com.ravia.app.navigation

import com.ravia.app.domain.model.User
import com.ravia.app.domain.model.UserRole

fun User.roleHomeRoute(): String = when (role) {
    UserRole.ADMIN, UserRole.MODERATOR -> Screen.Moderation.route
    UserRole.CITIZEN -> Screen.Home.route
}
