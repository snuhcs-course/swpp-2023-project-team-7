from fastapi import APIRouter
import mysql.connector
import os
from passlib.context import CryptContext

user = APIRouter()
books_db = mysql.connector.connect(
    host=os.environ["MYSQL_ENDPOINT"],
    user=os.environ["MYSQL_USER"],
    password=os.environ["MYSQL_PWD"],
    database="readability",
)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

@user.post("/user/signup")
def user_signup(username: str, email: str, password: str):
    cursor = books_db.cursor()

    cursor.execute(f"SELECT * FROM Users WHERE email = '{email}'")
    result = cursor.fetchall()
    if len(result) != 0:
        return {"error": "Email already exists"}

    cursor.execute(f"SELECT * FROM Users WHERE username = '{username}'")
    result = cursor.fetchall()
    if len(result) != 0:
        return {"error": "Username already exists"}

    hashed_password = get_password_hash(password)
    cursor.execute(f"INSERT INTO Users (username, email, password) VALUES ('{username}', '{email}', '{hashed_password}')")
    books_db.commit()
    return {"success": True}

@user.post("/user/login")
def user_login(email: str, password: str):
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Users WHERE email = '{email}'")
    result = cursor.fetchall()
    if len(result) == 0:
        return {"error": "Email does not exist"}
    if not verify_password(password, result[0][1]):
        return {"error": "Password is incorrect"}

    return {"success": True}
