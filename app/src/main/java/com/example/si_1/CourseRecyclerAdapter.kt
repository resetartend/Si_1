package com.example.si_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseRecyclerAdapter(
    private val courses: List<CourseModel>,
    private val onClick: (CourseModel) -> Unit
) : RecyclerView.Adapter<CourseRecyclerAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCourse: ImageView = view.findViewById(R.id.imgCourse)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.tvCourseName.text = course.name
        holder.imgCourse.setImageResource(course.imageResId)

        holder.itemView.setOnClickListener {
            onClick(course)
        }
    }

    override fun getItemCount(): Int = courses.size
}
