package dev.jvqtil.flow.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {
    fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeNotes()
    }

    suspend fun getNote(id: String): Note? {
        return noteDao.getById(id)
    }

    suspend fun insertNote(note: Note) {
        noteDao.insert(note)
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
}