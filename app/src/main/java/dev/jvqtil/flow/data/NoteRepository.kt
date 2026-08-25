package dev.jvqtil.flow.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {

    fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeNotes()
    }

    suspend fun getAllNotes(): List<Note> {
        return noteDao.getAllNotes()
    }

    suspend fun getNote(id: String): Note? {
        return noteDao.getById(id)
    }

    suspend fun insertNoteAtTop(note: Note) {
        noteDao.shiftPositionsDown()

        noteDao.insert(
            note.copy(position = 0L)
        )
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    suspend fun restoreNote(note: Note) {
        noteDao.upsert(note)
    }

    suspend fun restoreNotes(notes: List<Note>) {
        notes.forEach { note ->
            noteDao.upsert(note)
        }
    }

    suspend fun updateNotePositions(
        noteIds: List<String>
    ) {
        noteIds.forEachIndexed { index, id ->
            noteDao.updatePosition(
                id = id,
                position = index.toLong()
            )
        }
    }
}