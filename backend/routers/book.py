from fastapi import APIRouter, File, UploadFile
from fastapi.responses import FileResponse
from pydantic import BaseModel
import mysql.connector
import io
import PIL.Image as Image
import os
import uuid

class BookAddRequest(BaseModel):
    # TODO: Replace email when using OAuth
    email: str
    title: str
    content: str
    author: str = None
    cover_image: str = None

books_db = mysql.connector.connect(
    host=os.environ["MYSQL_ENDPOINT"],
    user=os.environ["MYSQL_USER"],
    password=os.environ["MYSQL_PWD"],
    database="readability",
)
book = APIRouter()

@book.get("/test_db")
def test_book_get():
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Books")
    result = cursor.fetchall()
    return {"test":result}

@book.get("/books")
def book_list(email: str):
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Books WHERE email = '{email}'")
    result = cursor.fetchall()

    books = []
    for row in result:
        books.append({
            "book_id": row[0],
            "title": row[2],
            "author": row[3],
            "progress": row[4],
            "cover_image": row[5],
        })
    return {"books": books}


@book.get("/book/{book_id}/detail")
def book_detail(book_id: str):
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Books WHERE id = '{book_id}'")
    result = cursor.fetchall()

    return {
        "title": result[0][2],
        "author": result[0][3],
        "progress": result[0][4],
        "cover_image": result[0][5],
    }

@book.put("/book/{book_id}/progress")
def book_progress(book_id: str, progress: float):
    cursor = books_db.cursor()
    cursor.execute(f"UPDATE Books SET progress = {progress} WHERE id = '{book_id}'")
    books_db.commit()
    return {}

@book.post("/book/add")
def book_add(req: BookAddRequest):
    # from the Users table, get the user's username by querying with the email
    cursor = books_db.cursor()
    cursor.execute(f"SELECT username FROM Users WHERE email = '{req.email}'")
    result = cursor.fetchall()
    if len(result) == 0:
        return {"error": "User does not exist"}

    # get username from result
    username = result[0][0]
    user_dirname = f"/home/swpp/readability_users/{username}"
    assert os.path.isdir(user_dirname) is True

    book_uuid = uuid.uuid4()
    image_url = f"{user_dirname}/{book_uuid}.png"
    content_url = f"{user_dirname}/{book_uuid}.txt"

    with open(content_url, 'w') as book_file:
        book_file.write(req.content)
    # asssumes that the client is sending the image as a byte array.
    # TODO: decide encoding of the image
    image = Image.open(io.BytesIO(req.cover_image))
    image.save(image_url)

    add_book = (
        "INSERT INTO Books (email, title, author, progress, cover_image, content)"
        "VALUES (%s, %s, %s, %s, %s, %s)"
    )
    image_url = "/".join(image_url.split("/")[-2:])
    content_url = "/".join(content_url.split("/")[-2:])
    test_data = (req.email, req.title, req.author, 0.0, image_url, content_url)

    cursor = books_db.cursor()
    cursor.execute(add_book, test_data)
    books_db.commit()
    return {}

@book.get("/book/image")
def book_image(image_url: str):
    image_url = os.path.join('/home/swpp/readability_users', image_url)
    return FileResponse(image_url)

@book.get("/book/content")
def book_content(content_url: str):
    content_url = os.path.join('/home/swpp/readability_users', content_url)
    return FileResponse(content_url)
