package org.potenday401.tag.application.service

import org.potenday401.tag.application.dto.TagCreationData
import org.potenday401.tag.application.dto.TagData
import org.potenday401.tag.domain.model.Tag
import org.potenday401.tag.domain.model.TagRepository
import java.time.LocalDateTime
import java.time.ZoneId

class TagApplicationService(private val tagRepository: TagRepository) {

    fun getTagById(id: String): TagData? {
        val tag = tagRepository.findById(id)
        if(tag != null) {
            return toTagData(tag)
        } else {
            return null
        }
    }

    fun getAllTags(): List<TagData> {
        return tagRepository.findAll().map { toTagData(it) }
    }

    fun getAllTagsByNameIn(names: List<String>): List<TagData> {
        return tagRepository.findAllByNameIn(names).map { toTagData(it) }
    }

    fun createTag(tagCreationData: TagCreationData) {
        val tag = Tag(tagCreationData.id, tagCreationData.name)
        tagRepository.create(tag)
    }

    private fun toTagData(tag: Tag): TagData {
        return TagData(
            id = tag.id,
            name = tag.name,
            createdAt = tag.createdAt.toEpochMilli(),
            modifiedAt = tag.modifiedAt.toEpochMilli()
        )
    }

    private fun LocalDateTime.toEpochMilli(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}