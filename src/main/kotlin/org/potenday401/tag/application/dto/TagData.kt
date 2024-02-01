package org.potenday401.tag.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagData(val id:String, val memberId:String, val name:String, val createdAt:Long, val modifiedAt:Long) {

}