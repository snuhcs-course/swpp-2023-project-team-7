from fastapi import APIRouter, File, UploadFile
from fastapi.responses import FileResponse
from pydantic import BaseModel

class BookAddRequest(BaseModel):
  title: str
  content: str
  author: str = None
  cover_image: str = None
  
book = APIRouter()

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
  return {}

@book.post("/book/image/upload")
def book_image_upload(file: UploadFile = File(...)):
  # TODO: return url of uploaded image
  return { "url": "https://picsum.photos/200/300" }

@book.get("/book/image/{image_id}")
def book_image(image_id: str):
  # TODO: create database to store image_id - image_path mapping
  return FileResponse("image.jpg")