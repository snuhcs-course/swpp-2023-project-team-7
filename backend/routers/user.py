from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
import mysql.connector
import os
import jwt
from datetime import datetime, timedelta, timezone
from passlib.context import CryptContext
from pydantic import BaseModel

class UserSignupRequest(BaseModel):
    username: str
    email: str
    password: str

user = APIRouter()
books_db = mysql.connector.connect(
    host=os.environ["MYSQL_ENDPOINT"],
    user=os.environ["MYSQL_USER"],
    password=os.environ["MYSQL_PWD"],
    database="readability",
)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

#TODO:
SECRET_KEY = "your_secret_key"  # Use an environment variable or a config file for the real secret
ALGORITHM = "HS256"  # Can be HS256 or RS256
ACCESS_TOKEN_EXPIRE_MINUTES = 30
REFRESH_TOKEN_EXPIRE_WEEKS = 2

def check_token_expired(token):
    decoded_token = jwt.decode(token.replace('"',''), SECRET_KEY, algorithms=[ALGORITHM])
    exp_timestamp = decoded_token.get("exp")
    if exp_timestamp:
        current_time = datetime.now(timezone.utc).timestamp()
        return current_time > exp_timestamp

def get_user_with_access_token(access_token):
    if not books_db.is_connected():
        books_db.reconnect()

    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Users WHERE access_token = '{access_token}'")
    result = cursor.fetchall()

    if len(result) == 0:
        return None
    # assert integrity of the token
    decoded_token = jwt.decode(access_token.replace('"',''), SECRET_KEY, algorithms=[ALGORITHM])
    decoded_email = decoded_token.get("sub")

    if (decoded_email != result[0][0]):
        return None
    if check_token_expired(access_token):
        return None

    # should be impossible as we already check whether the user exists
    return result[0][0]

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

@user.post("/user/signup")
def user_signup(user_signup_request: UserSignupRequest):
    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()

    cursor.execute(f"SELECT * FROM Users WHERE email = '{user_signup_request.email}'")
    result = cursor.fetchall()
    if len(result) != 0:
        raise HTTPException(
                status_code=409,
                detail="Email already exists"
            )
        # return {"error": "Email already exists"}

    cursor.execute(f"SELECT * FROM Users WHERE username = '{user_signup_request.username}'")
    result = cursor.fetchall()
    if len(result) != 0:
        raise HTTPException(
                status_code=409,
                detail="Username already exists"
            )
        # return {"error": "Username already exists"}

    hashed_password = get_password_hash(user_signup_request.password)
    cursor.execute(f"INSERT INTO Users (username, email, password) VALUES ('{user_signup_request.username}', '{user_signup_request.email}', '{hashed_password}')")
    books_db.commit()
    os.mkdir(f"/home/swpp/readability_users/{user_signup_request.username}")
    return {"success": True}

def check_user_exists_in_db(email:str, password:str):
    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Users WHERE email = '{email}'")
    result = cursor.fetchall()
    print(result)
    if len(result) == 0:
        return False
    # email, password, username, created_at, verified, refresh_token
    if not verify_password(password, result[0][1]):
        return False
    return True

def create_jwt_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)

    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def insert_access_token_to_user(email, access_token):
    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()
    cursor.execute(f"UPDATE Users SET access_token = '{access_token}' WHERE email = '{email}'")
    books_db.commit()

def insert_refresh_token_to_user(email, refresh_token):
    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()
    cursor.execute(f"UPDATE Users SET refresh_token = '{refresh_token}' WHERE email = '{email}'")
    books_db.commit()

def get_user_refresh_token(email):
    if not books_db.is_connected():
        books_db.reconnect()

    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Users WHERE email = '{email}'")
    result = cursor.fetchall()
    # should be impossible as we already check whether the user exists
    if len(result) == 0:
        return None
    if result[0][5] is None:
        return None
    return result[0][5]

@user.post("/token")
def login(form_data: OAuth2PasswordRequestForm = Depends()):
    email = form_data.username
    password = form_data.password

    result = check_user_exists_in_db(email, password) 
    if not result:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    token_data = {"sub": email}
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_jwt_token(
        data=token_data, expires_delta=access_token_expires
    )

    refresh_token = get_user_refresh_token(email)
    if not refresh_token or not check_token_expired(refresh_token):
        refresh_token_expires = timedelta(weeks=REFRESH_TOKEN_EXPIRE_WEEKS)
        refresh_token = create_jwt_token(
            data=token_data, expires_delta=refresh_token_expires
        )
        insert_refresh_token_to_user(email, refresh_token)

    insert_access_token_to_user(email, access_token)
    return {"access_token": access_token, "refresh_token": refresh_token, "token_type": "bearer"}

@user.post("/user/info")
def get_user_info(
    access_token: str
):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials.",
        headers={"WWW-Authenticate": "Bearer"},
    )

    if not get_user_with_access_token(access_token):
        raise credentials_exception

    if not books_db.is_connected():
        books_db.reconnect()
    cursor = books_db.cursor()
    cursor.execute(f"SELECT * FROM Users WHERE access_token = '{access_token}'")
    result = cursor.fetchall()

    if len(result) == 0:
        return None
    
    return {
        "username": result[0][2],
        "email": result[0][0],
        "created_at": result[0][3],
        "verified": result[0][4],
    }

@user.post("/token/refresh")
def refresh_access_token(refresh_token: str):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials.",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(refresh_token.replace('"',''), SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")

        if email is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Could not find username.",
                headers={"WWW-Authenticate": "Bearer"},
            )
        if check_token_expired(refresh_token):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token expired.",
                headers={"WWW-Authenticate": "Bearer"},
            )

    except Exception as exc:
        print(exc)
        raise credentials_exception from exc

    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    new_access_token = create_jwt_token(
        data={"sub": email}, expires_delta=access_token_expires
    )

    insert_access_token_to_user(email, new_access_token)
    return {"access_token": new_access_token, "token_type": "bearer"}
