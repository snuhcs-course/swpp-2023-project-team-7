from fastapi import APIRouter
from pydantic import BaseModel

class QuizReportRequest(BaseModel):
  quiz_id: str
  reason: str

ai = APIRouter()

@ai.get("/summary")
def ai_summary(book_id: str, progress: float):
  return {
    "summary_content": "This is a summary of the book",
  }

@ai.get("/quiz")
def ai_quiz(book_id: str, progress: float):
  return {
    "quiz_list": [
      {"id": "quiz_id", "question": "quiz_question", "answer": "choice1"}
    ]
  }

@ai.post("/quiz/report")
def ai_quiz_report(req: QuizReportRequest):
  return {}