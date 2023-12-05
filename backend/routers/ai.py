from fastapi import APIRouter, Request, Depends, HTTPException, status
from pydantic import BaseModel
from llama.run_quiz import get_quizzes_from_intermediate, get_quizzes_from_text
from llama.run_summary import get_summary_from_intermediate, get_summary_from_text
from sse_starlette.sse import EventSourceResponse
import mysql.connector
import os

from routers.user import get_user_with_access_token
from llama.custom_type import ProxyAIBackend, GPT4Backend

class QuizReportRequest(BaseModel):
    quiz_id: str
    reason: str

ai = APIRouter()

@ai.get("/summary")
async def ai_summary(request: Request, book_id: str, progress: float, email: str = Depends(get_user_with_access_token)):
    """
    :param book_id: book id to generate summary from
    :param progress: cutoff to which the summary is generated
    :param email(access_token): requesting user's email
    """
    if email is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    books_db = mysql.connector.connect(
        host=os.environ["MYSQL_ENDPOINT"],
        user=os.environ["MYSQL_USER"],
        password=os.environ["MYSQL_PWD"],
        database="readability",
    )
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Books WHERE id = '{book_id}'")
    result = cursor.fetchall()

    # if the num_total_inferences is 1, 
    # then the books was too short to divide.
    # therefore, we don't utilize a split summary.
    

    user_dirname = f"/home/swpp/readability_users/"
    book_content_url = os.path.join(user_dirname,result[0][6])
    ai_backend = ProxyAIBackend(GPT4Backend())

    if result[0][8] == 1:
        async def event_generator():
            for delta_content, finished in ai_backend.get_summary_from_text(progress, book_content_url):
                if await request.is_disconnected():
                    return
                yield {
                    "event": "summary",
                    "data": delta_content
                }
        return EventSourceResponse(event_generator())

    summary_tree_url = os.path.join(user_dirname,result[0][7])
    async def event_generator():
        for delta_content, finished in ai_backend.get_summary_from_intermediate(progress, book_content_url, summary_tree_url):
            if await request.is_disconnected():
                return
            yield {
                "event": "summary",
                "data": delta_content
            }
    return EventSourceResponse(event_generator())

@ai.get("/quiz")
def ai_quiz(request: Request, book_id: str, progress: float, email: str = Depends(get_user_with_access_token)):
    """
    :param book_id: book id to generate quiz from
    :param progress: progress of the book
    :param email(access_token): requesting user's email
    """
    if email is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    books_db = mysql.connector.connect(
        host=os.environ["MYSQL_ENDPOINT"],
        user=os.environ["MYSQL_USER"],
        password=os.environ["MYSQL_PWD"],
        database="readability",
    )
    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Books WHERE id = '{book_id}'")
    result = cursor.fetchall()
    
    user_dirname = f"/home/swpp/readability_users/"
    book_content_url = os.path.join(user_dirname,result[0][6])
    ai_backend = ProxyAIBackend(GPT4Backend())

    if result[0][8] == 1:
        async def event_generator():
            for delta_content, finished in ai_backend.get_quiz_from_text(progress, book_content_url):
                if await request.is_disconnected():
                    return
                yield {
                    "event": "summary",
                    "data": delta_content
                }
        return EventSourceResponse(event_generator())

    summary_tree_url = os.path.join(user_dirname,result[0][7])
    async def event_generator():
        for delta_content, finished in ai_backend.get_quiz_from_intermediate(progress, book_content_url, summary_tree_url):
            if await request.is_disconnected():
                return
            yield {
                "event": "quiz",
                "data": delta_content
            }
    return EventSourceResponse(event_generator())


@ai.post("/quiz/report")
def ai_quiz_report(req: QuizReportRequest):
    return {}
