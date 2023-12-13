package com.dart.campushelper.model

class ScheduleNoteItem(val title: String, val description: String) {
    companion object {
        fun mock(): ScheduleNoteItem {
            return ScheduleNoteItem(
                title = "Meeting",
                description = "Team meeting at 10:00 AM"
            )
        }
    }
}