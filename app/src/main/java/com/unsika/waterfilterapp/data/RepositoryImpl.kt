package com.unsika.waterfilterapp.data

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.unsika.waterfilterapp.data.remote.DataState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

interface Repository {
    suspend fun fetchDataFromFirebase(): Flow<DataState<Water?>>
    suspend fun fetchHistory(): Flow<DataState<List<History?>>>
    suspend fun deleteHistory(): Flow<DataState<String>>
}

class RepositoryImpl @Inject constructor() : Repository {

    override suspend fun fetchDataFromFirebase(): Flow<DataState<Water?>> = callbackFlow {
        var value: Water?
        val database = Firebase.database
        val myRef = database.getReference("water")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                value = snapshot.getValue<Water>()
                Log.d("FirebaseData", "Received data: $value")
                trySend(DataState.Success(value)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseData", "Failed to fetch data: ${error.message}")
                trySend(DataState.Failure(error.message)).isFailure
            }
        }

        myRef.addValueEventListener(valueEventListener)
        awaitClose { myRef.removeEventListener(valueEventListener) }
    }

    override suspend fun fetchHistory(): Flow<DataState<List<History?>>> = callbackFlow {
        val database = Firebase.database
        val myRef = database.getReference("history")
        val listHistory: MutableList<History?> = mutableListOf()

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val history = snapshot.getValue<History>()
                listHistory.add(history)
                trySend(DataState.Success(listHistory)).isSuccess
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val historyChanged = snapshot.getValue<History>()
                val index = listHistory.indexOfFirst { it?.date == historyChanged?.date }

                if (index != -1) {
                    listHistory[index] = historyChanged
                    trySend(DataState.Success(listHistory)).isSuccess
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val historyDeleted = snapshot.getValue<History>()
                listHistory.removeAll { it?.date == historyDeleted?.date }
                trySend(DataState.Success(listHistory)).isSuccess
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(DataState.Failure(error.message)).isFailure
            }
        }

        myRef.addChildEventListener(childEventListener)
        awaitClose { myRef.removeEventListener(childEventListener) }
    }

    override suspend fun deleteHistory(): Flow<DataState<String>> = callbackFlow {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("history")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val oneMonthAgo = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val myRef = reference.orderByChild("date").endAt(dateFormat.format(oneMonthAgo))

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshotChild in dataSnapshot.children) {
                    dataSnapshotChild.ref.removeValue()
                    trySend(DataState.Success("Data Bulan Lalu Berhasil Dihapus")).isSuccess
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                trySend(DataState.Failure(databaseError.message)).isFailure
            }
        }

        myRef.addValueEventListener(valueEventListener)
        awaitClose { myRef.removeEventListener(valueEventListener) }
    }
}