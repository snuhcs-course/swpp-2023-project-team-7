from fastapi import APIRouter, File, UploadFile
from fastapi.responses import FileResponse
from pydantic import BaseModel
import mysql.connector
import io
import PIL.Image as Image
import os
import uuid

class BookAddRequest(BaseModel):
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
def book_list():
  return [
      {"title": "book_title", "author": "book_author", "cover_image": "https://picsum.photos/200/300"},
      {"title": "book_title", "author": "book_author", "cover_image": "https://picsum.photos/200/300"},
      {"title": "book_title", "author": "book_author", "cover_image": "https://picsum.photos/200/300"},
      {"title": "book_title", "author": "book_author", "cover_image": "https://picsum.photos/200/300"},
      {"title": "book_title", "author": "book_author", "cover_image": "https://picsum.photos/200/300"}
    ]

@book.get("/book/{book_id}")
def book_detail(book_id: str):
  return {
    "title": "book_title",
    "author": "book_author",
    "cover_image": "https://picsum.photos/200/300",
    "content": "book_content"
  }


@book.post("/book/{book_id}/progress")
def book_progress(book_id: str, progress: float):
  return {}

@book.post("/book/add")
def book_add(req: BookAddRequest):
  user_dirname = req.email.replace(".", "")
  user_dirname = f"/home/swpp/readability_users/{user_dirname}"

  assert os.path.isdir(user_dirname) == True
  book_uuid = uuid.uuid4()
  image_url = f"{user_dirname}/{book_uuid}.png"
  content_url = f"{user_dirname}/{book_uuid}.txt"

  with open(content_url, 'w') as book_file:
    book_file.write(req.content)

  image = Image.open(io.BytesIO(req.cover_image))
  image.save(image_url)

  add_book = (
    "INSERT INTO Books (email, title, author, progress, cover_image, content)"
    "VALUES (%s, %s, %s, %s, %s, %s)"
  )
  test_data = (req.email, req.title, req.author, 0.0, image_url, content_url)

  cursor = books_db.cursor()
  cursor.execute(add_book, test_data)
  books_db.commit()
  return {}

@book.post("/book/image/upload")
def book_image_upload(file: UploadFile = File(...)):
  # TODO: return url of uploaded image
  return { "url": "https://picsum.photos/200/300" }

@book.get("/book/image/{image_id}")
def book_image(image_id: str):
  # TODO: create database to store image_id - image_path mapping
  return FileResponse("image.jpg")
