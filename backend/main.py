from typing import Union

from fastapi.staticfiles import StaticFiles
from routers.ai import ai
from routers.book import book
from routers.user import user
from fastapi import FastAPI

app = FastAPI()

app.include_router(ai)
app.include_router(book)
app.include_router(user)
app.mount("/static", StaticFiles(directory="static"), name="static")

if __name__ == "__main__":
  import uvicorn
  uvicorn.run(app, host="0.0.0.0", port=3000, reload=True)