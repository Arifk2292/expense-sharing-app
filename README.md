# Expense Sharing App

This is a backend application for a Splitwise clone, designed to manage shared expenses within groups. The application is built with Spring Boot and uses Google OAuth2 for secure authentication.

## Core Technologies

-   **Backend:** Spring Boot 3.5.6 with Java 17
-   **Database:** H2 (in-memory) with Spring Data JPA
-   **Security:** Spring Security with Google OAuth2
-   **API Documentation:** Swagger/OpenAPI
-   **Build Tool:** Maven

## Key Features

-   **Authentication:** Secure user login via Google OAuth2.
-   **Group Management:** Create groups and manage members.
-   **Expense Tracking:** Add expenses with equal or unequal splits.
-   **Balance Calculation:** Automatically calculates and tracks balances for each group member.
-   **Debt Settlement:** Functionality to record and settle debts between users.
-   **API Documentation:** Interactive API documentation via Swagger UI.

---

## Setup and Running the Application

Follow these steps to get the application running locally.

### 1. Configure Google OAuth2

The application uses Google for authentication. You'll need to get your own API credentials.

1.  Go to the [Google Cloud Console](https://console.cloud.google.com/).
2.  Create a new project.
3.  Navigate to **APIs & Services > Credentials**.
4.  Click **Create Credentials > OAuth client ID**.
5.  Select **Web application** as the application type.
6.  Under **Authorized redirect URIs**, add `http://localhost:8080/login/oauth2/code/google`.
7.  Click **Create**. You will receive a **Client ID** and a **Client Secret**.

Once you have your credentials, open `src/main/resources/application.properties` and add them:

```properties
spring.security.oauth2.client.registration.google.client-id=<YOUR_GOOGLE_CLIENT_ID>
spring.security.oauth2.client.registration.google.client-secret=<YOUR_GOOGLE_CLIENT_SECRET>
```

### 2. Run the Application

Open a terminal in the project's root directory and run the following command:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

---

## Testing the Application

### Useful Tools

-   **Swagger UI (API Documentation):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    Provides interactive documentation for all API endpoints. You can try them out directly from your browser.

-   **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    Allows you to view the in-memory database. Use the following credentials to log in:
    -   **Driver Class:** `org.h2.Driver`
    -   **JDBC URL:** `jdbc:h2:mem:testdb`
    -   **User Name:** `sa`
    -   **Password:** `password`

### API Testing Workflow

This workflow simulates a real-world scenario with two users, **Arif** and **John**.

#### Step 1: Authentication

-   **Action:** In a browser, navigate to `http://localhost:8080/oauth2/authorization/google`. This will redirect you to Google to log in. After a successful login, Spring Security creates an authenticated session.
-   **Note:** For testing with tools like Postman, you'll need to manage the session cookie (`JSESSIONID`). For this guide, we'll assume an active session is established. The first time users log in, they will be added to the `users` table (we'll assume Arif gets `id=1` and John gets `id=2`).

#### Step 2: Get Authenticated User Details

-   **Endpoint:** `GET /api/user`
-   **Description:** Retrieves the details of the currently logged-in user.
-   **Sample Response (for Arif):**
    ```json
    {
      "id": 1,
      "name": "Arif",
      "email": "arif.test@example.com",
      "oauthId": "12345678901234567890",
      "role": "USER"
    }
    ```

#### Step 3: Create a Group

-   **Endpoint:** `POST /groups`
-   **Description:** Arif (user with `id=1`) creates a new group.
-   **Sample Request:**
    ```json
    {
      "name": "Trip to Bali",
      "adminId": 1
    }
    ```
-   **Sample Response:**
    ```json
    {
      "id": 1,
      "name": "Trip to Bali",
      "members": [
        {
          "id": 1,
          "name": "Arif",
          "email": "arif.test@example.com",
          "oauthId": "12345678901234567890",
          "role": "USER"
        }
      ]
    }
    ```

#### Step 4: Add a Member to the Group

-   **Endpoint:** `POST /groups/1/members`
-   **Description:** Arif adds John (user with `id=2`) to the group (`id=1`).
-   **Sample Request:**
    ```json
    {
      "userId": 2
    }
    ```
-   **Sample Response:**
    ```json
    {
      "id": 1,
      "name": "Trip to Bali",
      "members": [
        {
          "id": 1,
          "name": "Arif",
          "email": "arif.test@example.com",
          "oauthId": "12345678901234567890",
          "role": "USER"
        },
        {
          "id": 2,
          "name": "John",
          "email": "john.test@example.com",
          "oauthId": "09876543210987654321",
          "role": "USER"
        }
      ]
    }
    ```

#### Step 5: Add an Expense

-   **Endpoint:** `POST /expenses`
-   **Description:** Arif (`id=1`) pays $100 for dinner and splits it equally ($50 each).
-   **Sample Request:**
    ```json
    {
      "description": "Dinner",
      "amount": 100.00,
      "payerId": 1,
      "groupId": 1,
      "splits": {
        "1": 50.00,
        "2": 50.00
      }
    }
    ```
-   **Sample Response:**
    ```json
    {
        "id": 1,
        "description": "Dinner",
        "amount": 100.00,
        "createdBy": {
            "id": 1,
            "name": "Arif",
            "email": "arif.test@example.com",
            "oauthId": "12345678901234567890",
            "role": "USER"
        },
        "group": {
            "id": 1,
            "name": "Trip to Bali",
            "members": []
        },
        "date": "2025-09-27T14:30:00.000+00:00",
        "splits": {
            "1": 50.00,
            "2": 50.00
        }
    }
    ```

#### Step 6: Check Group Balances

-   **Endpoint:** `GET /groups/1/balances`
-   **Description:** Check the balances. Arif paid $100 for his $50 share, so he is owed $50. John paid $0 for his $50 share, so he owes $50.
-   **Sample Response:**
    ```json
    {
      "Arif": 50.00,
      "John": -50.00
    }
    ```

#### Step 7: Settle the Debt

-   **Endpoint:** `POST /settlements`
-   **Description:** John (payer `id=2`) pays Arif (receiver `id=1`) the $50 he owes.
-   **Sample Request:**
    ```json
    {
      "payerId": 2,
      "receiverId": 1,
      "amount": 50.00,
      "groupId": 1
    }
    ```
-   **Sample Response:**
    ```json
    {
      "id": 1,
      "payer": {
        "id": 2,
        "name": "John",
        "email": "john.test@example.com",
        "oauthId": "09876543210987654321",
        "role": "USER"
      },
      "receiver": {
        "id": 1,
        "name": "Arif",
        "email": "arif.test@example.com",
        "oauthId": "12345678901234567890",
        "role": "USER"
      },
      "amount": 50.00,
      "group": {
        "id": 1,
        "name": "Trip to Bali",
        "members": []
      }
    }
    ```

#### Step 8: Check Final Balances

-   **Endpoint:** `GET /groups/1/balances`
-   **Description:** After the settlement, the balances for both users should be zero.
-   **Sample Response:**
    ```json
    {
      "Arif": 0.00,
      "John": 0.00
    }
    ```

#### Step 9: Delete the Group (Admin Only)

-   **Endpoint:** `DELETE /groups/1`
-   **Description:** Arif (the admin of group `id=1`) deletes the group. This action will fail if attempted by any other user, including John.
-   **Action:**
    1. Ensure you are authenticated as the user who created the group (Arif, user `id=1`).
    2. Execute the `DELETE` request to `/groups/1`.
-   **Expected Response:** A `204 No Content` status code, indicating the group was successfully deleted.