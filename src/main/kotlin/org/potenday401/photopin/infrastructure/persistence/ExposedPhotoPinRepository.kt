import PhotoPinTable.latitude
import PhotoPinTable.longitude
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.potenday401.photopin.domain.model.LatLng
import org.potenday401.photopin.domain.model.PhotoPin
import org.potenday401.photopin.domain.model.PhotoPinRepository
import org.potenday401.tag.domain.model.Tag
import org.potenday401.tag.infrastructure.persistence.TagTable


object PhotoPinTagIdsTable : Table("photo_pin_tag_ids") {
    val photoPinId = varchar("photo_pin_id", 64).references(PhotoPinTable.id)
    val tagId = varchar("tag_id", 64)
    override val primaryKey = PrimaryKey(photoPinId, tagId)
}

object PhotoPinTable : Table("photo_pin") {
    val id = varchar("id", 64)
    val memberId = varchar("member_id", 64)
    val photoUrl = varchar("photo_url", 255)
    val photoDateTime = datetime("photo_date_time")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val createdAt = datetime("created_at")
    val modifiedAt = datetime("modified_at")

    override val primaryKey = PrimaryKey(id)
}

class ExposedPhotoPinRepository : PhotoPinRepository {
    override fun create(photoPin: PhotoPin) {
        transaction {
            PhotoPinTable.insert {
                it[id] = photoPin.id
                it[memberId] = photoPin.memberId
                it[photoUrl] = photoPin.photoUrl
                it[photoDateTime] = photoPin.photoDateTime
                it[latitude] = photoPin.latLng.latitude
                it[longitude] = photoPin.latLng.longitude
                it[createdAt] = photoPin.createdAt
                it[modifiedAt] = photoPin.modifiedAt
            }

            photoPin.tagIds.forEach { tagId ->
                PhotoPinTagIdsTable.insert {
                    it[photoPinId] = photoPin.id
                    it[this.tagId] = tagId
                }
            }
        }
    }

    override fun findById(id: String): PhotoPin? {
        return transaction {
            val photoPinRow = PhotoPinTable.select { PhotoPinTable.id eq id }.singleOrNull()

            photoPinRow?.let { row ->
                val tagIds = PhotoPinTagIdsTable.select { PhotoPinTagIdsTable.photoPinId eq id }
                    .map { it[PhotoPinTagIdsTable.tagId] }

                PhotoPin(
                    id = row[PhotoPinTable.id],
                    memberId = row[PhotoPinTable.memberId],
                    tagIds = tagIds,
                    photoUrl = row[PhotoPinTable.photoUrl],
                    photoDateTime = row[PhotoPinTable.photoDateTime],
                    latLng = LatLng(
                        latitude = row[PhotoPinTable.latitude],
                        longitude = row[PhotoPinTable.longitude]
                    ),
                    createdAt = row[PhotoPinTable.createdAt],
                    modifiedAt = row[PhotoPinTable.modifiedAt]
                )
            }
        }
    }

    override fun findAllByTagId(tagId: String): List<PhotoPin> {
        return transaction {
            PhotoPinTable.join(
                PhotoPinTagIdsTable,
                JoinType.LEFT,
                additionalConstraint = { PhotoPinTable.id eq PhotoPinTagIdsTable.photoPinId })
                .select { PhotoPinTagIdsTable.tagId eq tagId }
                .groupBy { it[PhotoPinTable.id] }
                .map { (_, rows) ->
                    val firstRow = rows.first()
                    PhotoPin(
                        id = firstRow[PhotoPinTable.id],
                        memberId = firstRow[PhotoPinTable.memberId],
                        tagIds = rows.mapNotNull { it[PhotoPinTagIdsTable.tagId] }.distinct(),
                        photoUrl = firstRow[PhotoPinTable.photoUrl],
                        photoDateTime = firstRow[PhotoPinTable.photoDateTime],
                        latLng = LatLng(
                            latitude = firstRow[latitude],
                            longitude = firstRow[longitude]
                        ),
                        createdAt = firstRow[PhotoPinTable.createdAt],
                        modifiedAt = firstRow[PhotoPinTable.modifiedAt]
                    )
                }
        }
    }

    override fun findAll(memberId: String): List<PhotoPin> {
        return transaction {
            PhotoPinTable.join(
                PhotoPinTagIdsTable,
                JoinType.LEFT,
                additionalConstraint = { PhotoPinTable.id eq PhotoPinTagIdsTable.photoPinId })
                .select { (PhotoPinTable.memberId eq memberId)}
                .groupBy { it[PhotoPinTable.id] }
                .map { (_, rows) ->
                    val firstRow = rows.first()
                    PhotoPin(
                        id = firstRow[PhotoPinTable.id],
                        memberId = firstRow[PhotoPinTable.memberId],
                        tagIds = rows.mapNotNull { it[PhotoPinTagIdsTable.tagId] }.distinct(),
                        photoUrl = firstRow[PhotoPinTable.photoUrl],
                        photoDateTime = firstRow[PhotoPinTable.photoDateTime],
                        latLng = LatLng(
                            latitude = firstRow[latitude],
                            longitude = firstRow[longitude]
                        ),
                        createdAt = firstRow[PhotoPinTable.createdAt],
                        modifiedAt = firstRow[PhotoPinTable.modifiedAt]
                    )
                }
        }
    }
}
