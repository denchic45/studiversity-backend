package com.studiversity.feature.user

import com.studiversity.database.exists
import com.studiversity.database.table.*
import com.studiversity.feature.auth.model.MagicLinkToken
import com.studiversity.feature.auth.model.RefreshToken
import com.studiversity.feature.auth.model.UserByEmail
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.AddScopeRepoExt
import com.stuiversity.api.account.model.UpdateAccountPersonalRequest
import com.stuiversity.api.account.model.UpdateEmailRequest
import com.stuiversity.api.account.model.UpdatePasswordRequest
import com.stuiversity.api.auth.AuthErrors
import com.stuiversity.api.auth.model.CreateUserRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.user.model.User
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.*

class UserRepository(
    private val organizationId: UUID
) : AddScopeRepoExt {


    fun add(signupRequest: SignupRequest) {
        val hashed: String = BCrypt.hashpw(signupRequest.password, BCrypt.gensalt())
        add(signupRequest.toCreateUser(), hashed)
    }

    fun add(user: CreateUserRequest, password: String): User = UserDao.new {
        firstName = user.firstName
        surname = user.surname
        patronymic = user.patronymic
        email = user.email
        this.password = password
    }.toDomain().apply {
        addScope(id, ScopeType.User, organizationId)
    }

    fun findById(id: UUID): User? {
        return UserDao.findById(id)?.toDomain()
    }

    fun remove(userId: UUID): Boolean {
        return Users.deleteWhere { Users.id eq userId } == 1
    }

    fun update(userId: UUID, updateAccountPersonalRequest: UpdateAccountPersonalRequest) {
        UserDao.findById(userId)!!.apply {
            updateAccountPersonalRequest.firstName.ifPresent {
                firstName = it
            }
            updateAccountPersonalRequest.surname.ifPresent {
                surname = it
            }
            updateAccountPersonalRequest.patronymic.ifPresent {
                patronymic = it
            }
        }
    }

    fun update(userId: UUID, updatePasswordRequest: UpdatePasswordRequest) {
        UserDao.findById(userId)!!.apply {
            if (!BCrypt.checkpw(updatePasswordRequest.oldPassword, password))
                throw BadRequestException(AuthErrors.INVALID_PASSWORD)
            password = BCrypt.hashpw(updatePasswordRequest.newPassword, BCrypt.gensalt())
        }
    }

    fun update(userId: UUID, updateEmailRequest: UpdateEmailRequest) {
        UserDao.findById(userId)!!.email = updateEmailRequest.email
    }

    fun findUserByEmail(email: String): User? {
        return UserDao.find(Users.email eq email).singleOrNull()?.toDomain()
    }

    fun findEmailPasswordByEmail(email: String): UserByEmail? {
        return UserDao.find(Users.email eq email).singleOrNull()
            ?.let { UserByEmail(it.id.value, it.email, it.password) }
    }

    fun addToken(createRefreshToken: RefreshToken): String {
        return RefreshTokens.insert {
            it[userId] = createRefreshToken.userId
            it[token] = createRefreshToken.token
            it[expireAt] = createRefreshToken.expireAt
        }[RefreshTokens.token]
    }

    fun existByEmail(email: String): Boolean {
        return Users.exists { Users.email eq email }
    }

    fun findRefreshToken(refreshToken: String): RefreshToken? {
        val expiredTokens = RefreshTokens.select(RefreshTokens.expireAt less Instant.now()).limit(100)
            .map { it[RefreshTokens.id] }
        RefreshTokens.deleteWhere { RefreshTokens.id inList expiredTokens }
        return RefreshTokens.select(RefreshTokens.token eq refreshToken)
            .singleOrNull()?.let {
                RefreshToken(
                    it[RefreshTokens.userId].value,
                    it[RefreshTokens.token],
                    it[RefreshTokens.expireAt]
                )
            }
    }

    fun removeRefreshToken(refreshToken: String) {
        RefreshTokens.deleteWhere { token eq refreshToken }
    }

    fun addMagicLink(magicLinkToken: MagicLinkToken): String {
        return MagicLinks.insert {
            it[token] = magicLinkToken.token
            it[userId] = magicLinkToken.userId
            it[expireAt] = magicLinkToken.expireAt
        }[MagicLinks.token]
    }

    fun removeMagicLink(token: String) {
        MagicLinks.deleteWhere { this.token eq token }
    }

    fun findMagicLinkByToken(token: String): MagicLinkToken? {
        val expiredTokens = MagicLinks.select(MagicLinks.expireAt less Instant.now()).limit(100)
            .map { it[MagicLinks.id] }
        MagicLinks.deleteWhere { MagicLinks.id inList expiredTokens }

        return MagicLinks.select(MagicLinks.token eq token).singleOrNull()?.let {
            MagicLinkToken(
                userId = it[MagicLinks.userId].value,
                token = it[MagicLinks.token],
                expireAt = it[MagicLinks.expireAt]
            )
        }
    }

}