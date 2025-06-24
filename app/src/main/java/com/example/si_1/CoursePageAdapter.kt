package com.example.si_1

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Course(val name: String, val imageRes: Int)

class CourseAdapter(
    private val courses: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
        val name = layout.findViewById<TextView>(R.id.tvCourseName)
        val image = layout.findViewById<ImageView>(R.id.imgCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false) as LinearLayout
        return CourseViewHolder(layout)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name
        holder.image.setImageResource(course.imageRes)
        holder.layout.setOnClickListener { onCourseClick(course) }
    }

    override fun getItemCount() = courses.size
}

