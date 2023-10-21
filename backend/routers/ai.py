from fastapi import APIRouter, Request
from pydantic import BaseModel
from llama.run_quiz import get_quizzes
from llama.run_summary import get_summary
from sse_starlette.sse import EventSourceResponse


class QuizReportRequest(BaseModel):
    quiz_id: str
    reason: str


ai = APIRouter()


@ai.get("/summary")
async def ai_summary(request: Request, book_id: str, progress: float):
    """
    :param book_id: book id to generate quiz from
    :param progress: progress of the book
    :param key: key to identify the quiz session
    :param index: index of the quiz
    """

    async def event_generator():
        for delta_content, finished in get_summary(progress, book_id):
            if await request.is_disconnected():
                return
            yield {
                "event": "summary",
                "data": delta_content
            }

    return EventSourceResponse(event_generator())

@ai.get("/quiz")
def ai_quiz(request: Request, book_id: str, progress: float):
    """
    :param book_id: book id to generate quiz from
    :param progress: progress of the book
    """
    async def event_generator():
        for quiz, quiz_len in get_quizzes(progress, book_id):
            if await request.is_disconnected():
                return
            yield {
                "event": "quiz",
                "data": {
                    "quiz_id": "123123",
                    "quiz": quiz,
                    "quiz_len": quiz_len
                }
            }

    return EventSourceResponse(event_generator())


@ai.post("/quiz/report")
def ai_quiz_report(req: QuizReportRequest):
    return {}
