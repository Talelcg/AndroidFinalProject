package com.project.easytravel.base

data class GeminiRequest(
    val contents: List<Content> // יש לשלוח את המידע כאן
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)
data class Candidate(
    val content: Content?
)
data class Content(
    val parts: List<Part> // חלקים של התוכן שיבוצעו בהם הפעולות
)

data class Part(
    val text: String // הטקסט של כל חלק
)
