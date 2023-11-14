from fastapi.testclient import TestClient
import sys
from datetime import datetime, timedelta
import jwt
import time

sys.path.append("/home/swpp/swpp-2023-project-team-7/backend")
from routers.user import SECRET_KEY, ALGORITHM
from main import app

client = TestClient(app)

def create_test_jwt_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(seconds=60)

    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def test_refresh_access_token():
    # Arrange
    test_email = "user@example.com"
    test_refresh_token = create_test_jwt_token({"sub": test_email})

    # Act
    response = client.post(f"/token/refresh?refresh_token={test_refresh_token}")

    # Assert
    assert response.status_code == 200
    assert "access_token" in response.json()
    assert response.json()["token_type"] == "bearer"

    # Check that the new access token works by decoding it
    payload = jwt.decode(response.json()["access_token"], SECRET_KEY, algorithms=[ALGORITHM])
    assert payload["sub"] == test_email

def test_login_fails():
    # Arrange
    test_email = "fail_fail_fail_fail_fail"
    test_password = "fail_fail_fail_fail_fail"
    login_data = {
        "username": test_email,
        "password": test_password
    }

    # Act
    response = client.post("/token", data=login_data)

    # Assert
    assert response.status_code == 401

def test_refresh_access_token_expired():
    # Arrange
    test_email = "user@example.com"
    test_refresh_token = create_test_jwt_token({"sub": test_email}, expires_delta=timedelta(seconds=1))
    time.sleep(2)

    # Act
    response = client.post(f"/token/refresh?refresh_token={test_refresh_token}")

    # Assert
    assert response.status_code == 401