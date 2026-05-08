package com.kabindra.tv.iptv.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kabindra.tv.iptv.domain.entity.User
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserDTO(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Room treats 0 as "not set" and generates a new ID
    @ColumnInfo(name = "server_name") val server_name: String? = "",
    @ColumnInfo(name = "username") val username: String? = "",
    @ColumnInfo(name = "password") val password: String? = "",
)

fun UserDTO.toDomain(): User {
    return User(
        id = id,
        server_name = server_name,
        username = username,
        password = password,
    )
}