from fastapi import APIRouter, File, UploadFile, Depends, HTTPException, status, BackgroundTasks
from fastapi import BackgroundTasks
from fastapi.responses import FileResponse
from pydantic import BaseModel
import mysql.connector
import io
import PIL.Image as Image
import os
import uuid
import asyncio

from routers.user import get_user_with_access_token
from llama.preprocess_summary import generate_summary_tree

class BookAddRequest(BaseModel):
    # TODO: Replace email when using OAuth
    title: str
    content: str
    author: str = None
    cover_image: str = None


book = APIRouter()

@book.get("/test_db")
def test_book_get(query: str):
    books_db = mysql.connector.connect(
        host=os.environ["MYSQL_ENDPOINT"],
        user=os.environ["MYSQL_USER"],
        password=os.environ["MYSQL_PWD"],
        database="readability",
    )
    cursor = books_db.cursor()
    cursor.execute(query)
    result = cursor.fetchall()
    return {"test":result}

@book.get("/books")
def book_list(email: str = Depends(get_user_with_access_token)):
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
    cursor.execute(f"SELECT * FROM Books WHERE email = '{email}'")
    result = cursor.fetchall()

    books = []
    for row in result:
        books.append({
            "book_id": row[0],
            "title": row[2],
            "author": row[3],
            "progress": float(row[4]),
            "cover_image": row[5],
            "content": row[6],
            "summary_tree": row[7]
        })
    return {"books": books}


@book.get("/book/{book_id}/detail")
def book_detail(book_id: str, email: str = Depends(get_user_with_access_token)):
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

    return {
        "title": result[0][2],
        "author": result[0][3],
        "progress": float(result[0][4]),
        "cover_image": result[0][5],
        "content": result[0][6],
    }

@book.put("/book/{book_id}/progress")
def book_progress(book_id: str, progress: float, email: str = Depends(get_user_with_access_token)):
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
    cursor.execute(f"UPDATE Books SET progress = {progress} WHERE id = '{book_id}'")
    books_db.commit()
    return {}

@book.post("/book/add")
async def book_add(background_tasks:BackgroundTasks, req: BookAddRequest, email: str = Depends(get_user_with_access_token)):
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

    # from the Users table, get the user's username by querying with the email
    cursor = books_db.cursor()
    cursor.execute(f"SELECT username FROM Users WHERE email = '{email}'")
    result = cursor.fetchall()
    if len(result) == 0:
        return {"error": "User does not exist"}

    # get username from result
    username = result[0][0]
    user_dirname = f"/home/swpp/readability_users/{username}"

    # added mkdir
    if not os.path.isdir(user_dirname):
        os.mkdir(user_dirname)
    # assert os.path.isdir(user_dirname) is True

    book_uuid = uuid.uuid4()
    image_url = f"{user_dirname}/{book_uuid}.png"
    content_url = f"{user_dirname}/{book_uuid}.txt"

    with open(content_url, 'w') as book_file:
        book_file.write(req.content)

    content_url = "/".join(content_url.split("/")[-2:])
    # asssumes that the client is sending the image as a byte array.
    if req.cover_image != "":
        image = Image.open(io.BytesIO(bytearray.fromhex(req.cover_image)))
        image.save(image_url)
        image_url = "/".join(image_url.split("/")[-2:])
    else:
        image_url = None

    add_book = (
        "INSERT INTO Books (email, title, author, progress, cover_image, content)"
        "VALUES (%s, %s, %s, %s, %s, %s)"
    )
    book_data = (email, req.title, req.author, 0.0, image_url, content_url)

    cursor = books_db.cursor()
    cursor.execute(add_book, book_data)
    books_db.commit()
    book_id = cursor.lastrowid

    background_tasks.add_task(generate_summary_tree, book_id, req.content)
    return {}

@book.get("/book/image")
def book_image(image_url: str, email: str = Depends(get_user_with_access_token)):
    if email is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    image_url = os.path.join('/home/swpp/readability_users', image_url)
    return FileResponse(image_url)

@book.get("/book/content")
def book_content(content_url: str, email: str = Depends(get_user_with_access_token)):
    if email is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    content_url = os.path.join('/home/swpp/readability_users', content_url)
    return FileResponse(content_url)

@book.delete("/book/delete")
def book_delete(book_id: str, email: str = Depends(get_user_with_access_token)):
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
    books_db.cursor().execute(f"DELETE FROM Books WHERE id = '{book_id}'")
    books_db.commit()
    return {}

@book.get("/book/{book_id}/current_inference")
def book_inference(book_id: str, email: str = Depends(get_user_with_access_token)):
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

    num_total_inference = result[0][8]
    num_current_inference = result[0][9]
    current_ratio = float(num_current_inference/ num_total_inference)
    return {"summary_progress": current_ratio}
