package dev.jvqtil.flow.data.backup

import java.time.Instant
import dev.jvqtil.flow.data.Attachment as DbAttachment
import dev.jvqtil.flow.data.Entry as DbEntry
import dev.jvqtil.flow.data.Folder as DbFolder

object BackupMapper {

    fun folder(
        folder: DbFolder
    ): Folder {
        return Folder(
            id = folder.id,
            name = folder.name,
            position = folder.position
        )
    }

    fun entry(
        entry: DbEntry
    ): Entry {
        return Entry(
            id = entry.id,
            text = entry.text,
            createdAt =
                Instant
                    .ofEpochMilli(
                        entry.createdAt
                    )
                    .toString(),
            position = entry.position,
            type = entry.type,
            completed = entry.completed,
            folderId = entry.folderId
        )
    }

    fun attachment(
        attachment: DbAttachment
    ): Attachment {
        return Attachment(
            id = attachment.id,
            entryId = attachment.entryId,
            fileName = attachment.fileName,
            mimeType = attachment.mimeType,
            size = attachment.size,
            createdAt =
                Instant
                    .ofEpochMilli(
                        attachment.createdAt
                    )
                    .toString()
        )
    }

    fun folder(
        folder: Folder
    ): DbFolder {
        return DbFolder(
            id = folder.id,
            name = folder.name,
            position = folder.position
        )
    }

    fun entry(
        entry: Entry
    ): DbEntry {
        return DbEntry(
            id = entry.id,
            text = entry.text,
            createdAt =
                Instant
                    .parse(
                        entry.createdAt
                    )
                    .toEpochMilli(),
            position = entry.position,
            type = entry.type,
            completed = entry.completed,
            folderId = entry.folderId
        )
    }

    fun attachment(
        attachment: Attachment,
        path: String
    ): DbAttachment {
        return DbAttachment(
            id = attachment.id,
            entryId = attachment.entryId,
            path = path,
            fileName = attachment.fileName,
            mimeType = attachment.mimeType,
            size = attachment.size,
            createdAt =
                Instant
                    .parse(
                        attachment.createdAt
                    )
                    .toEpochMilli()
        )
    }
}