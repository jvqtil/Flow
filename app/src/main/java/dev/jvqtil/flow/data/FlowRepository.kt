package dev.jvqtil.flow.data

import kotlinx.coroutines.flow.Flow

class FlowRepository(
    private val flowDao: FlowDao
) {

    fun observeEntries(): Flow<List<Entry>> {
        return flowDao.observeNotes()
    }

    suspend fun getAllEntries(): List<Entry> {
        return flowDao.getAllNotes()
    }

    suspend fun getEntry(id: String): Entry? {
        return flowDao.getById(id)
    }

    suspend fun insertAtTop(entry: Entry) {
        flowDao.shiftPositionsDown()

        flowDao.insert(
            entry.copy(position = 0L)
        )
    }

    suspend fun updateEntry(entry: Entry) {
        flowDao.update(entry)
    }

    suspend fun deleteEntry(entry: Entry) {
        flowDao.delete(entry)
    }

    suspend fun restoreEntry(entry: Entry) {
        flowDao.upsert(entry)
    }

    suspend fun restoreEntry(entries: List<Entry>) {
        entries.forEach { note ->
            flowDao.upsert(note)
        }
    }

    suspend fun updateEntriesPositions(
        noteIds: List<String>
    ) {
        noteIds.forEachIndexed { index, id ->
            flowDao.updatePosition(
                id = id,
                position = index.toLong()
            )
        }
    }
}