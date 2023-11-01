from typing import Union
from routers.ai import ai
from routers.book import book
from routers.user import user
from fastapi import FastAPI

app = FastAPI()

app.include_router(ai)
app.include_router(book)
app.include_router(user)

if __name__ == "__main__":
  import uvicorn
  uvicorn.run(app, host="0.0.0.0", port=3000, reload=True)