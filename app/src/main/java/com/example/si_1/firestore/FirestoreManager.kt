package com.example.si_1.firestore

import com.example.si_1.Complaint
import com.example.si_1.model.User
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun saveUser(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(user.uid).set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun submitComplaint(complaint: Complaint, onComplete: (Boolean) -> Unit) {
        db.collection("complaints").add(complaint)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}