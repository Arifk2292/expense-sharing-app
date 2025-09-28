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

## **Complete Application Testing Guide**

This guide will walk you through testing the Expense Sharing App using three users: **Arif Khan**, **Alimun**, and **Armish Khan**.

### **Step 1: Setup and Authentication**

1.  **Run the Application**:
    Start the application from your IDE or by running the command in the project root:
    ```bash
    ./mvnw spring-boot:run
    ```

2.  **Open the API Documentation (Swagger UI)**:
    Navigate to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser. This will be your primary tool for testing.

3.  **Simulate User Logins**:
    We will simulate the login for our three users. In a real-world scenario, each user would log in with their own Google account. The first time they log in, the application creates a user record for them.

    *   **Arif Khan logs in**: Navigate to `http://localhost:8080/oauth2/authorization/google`, complete the login.
    *   **Alimun logs in**: Open a new private/incognito browser window and repeat the login process for Alimun.
    *   **Armish Khan logs in**: Repeat the login process in another private window for Armish Khan.

    After this, the `users` table in the H2 database will contain three users. We will assume they have IDs **1, 2, and 3** respectively.

4.  **Get Authenticated User Details**:
    To verify which user is currently "logged in" in your browser session, you can use the `GET /api/user` endpoint in Swagger.

    *   **Sample Response (for Arif Khan)**:
        ```json
        {
          "id": 1,
          "name": "Arif Khan",
          "email": "arifkhan2292@gmail.com",
          "oauthId": "...",
          "role": "USER"
        }
        ```

### **Step 2: Group and User Management**

We will create two separate groups to test multi-group functionality.

1.  **Arif Khan Creates Group 1: "Road Trip"**
    *   **Action**: Arif Khan, as the authenticated user, creates a new group.
    *   **Endpoint**: `POST /groups`
    *   **Request Body**:
        ```json
        {
          "name": "Road Trip",
          "adminId": 1
        }
        ```
    *   **Expected Response (`200 OK`)**: A group object is returned with Arif Khan as the admin and only member. **Note the `id` of this group (e.g., 1).**

2.  **Arif Khan Adds Members to "Road Trip"**
    *   **Action**: Arif Khan (admin) adds Alimun (`userId: 2`) and Armish Khan (`userId: 3`) to the group.
    *   **Endpoint**: `POST /groups/1/members`
    *   **Request Body (to add Alimun)**:
        ```json
        {
          "userId": 2
        }
        ```
    *   **(Repeat for Armish Khan with `userId: 3`)**
    *   **Expected Response (`200 OK`)**: The updated group object is returned, now with all three members.

3.  **Alimun Creates Group 2: "Project Lunch"**
    *   **Action**: In Alimun's browser session, he creates a new group.
    *   **Endpoint**: `POST /groups`
    *   **Request Body**:
        ```json
        {
          "name": "Project Lunch",
          "adminId": 2
        }
        ```
    *   **Expected Response (`200 OK`)**: A new group object is returned with Alimun as the admin. **Note the `id` of this group (e.g., 2).**

4.  **Alimun Adds Arif Khan to "Project Lunch"**
    *   **Action**: Alimun adds Arif Khan (`userId: 1`) to his new group.
    *   **Endpoint**: `POST /groups/2/members`
    *   **Request Body**:
        ```json
        {
          "userId": 1
        }
        ```
    *   **Expected Response (`200 OK`)**: The updated group object is returned with both Alimun and Arif Khan as members.

### **Step 3: Expense and Settlement Scenarios**

1.  **Scenario A: "Road Trip" - Equal Split**
    *   **Action**: Arif Khan pays 90 AED for Gas, split equally among all 3 members.
    *   **Endpoint**: `POST /expenses`
    *   **Request Body**:
        ```json
        {
          "description": "Gas",
          "amount": 90.00,
          "payerId": 1,
          "groupId": 1,
          "splits": {
            "1": 30.00,
            "2": 30.00,
            "3": 30.00
          }
        }
        ```
    *   **Check Balances**: Use `GET /groups/1/balances`.
    *   **Expected Balances**:
        *   Arif Khan paid 90 for his 30 share -> he is owed 60.
        *   Alimun & Armish Khan paid 0 for their 30 shares -> they each owe 30.
        ```json
        {
          "Arif Khan": 60.00,
          "Alimun": -30.00,
          "Armish Khan": -30.00
        }
        ```

2.  **Scenario B: "Road Trip" - Unequal Split**
    *   **Action**: Alimun pays 50 AED for Snacks, with a custom split.
    *   **Endpoint**: `POST /expenses`
    *   **Request Body**:
        ```json
        {
          "description": "Snacks",
          "amount": 50.00,
          "payerId": 2,
          "groupId": 1,
          "splits": {
            "1": 15.00,
            "2": 10.00,
            "3": 25.00
          }
        }
        ```
    *   **Check Balances**: Use `GET /groups/1/balances`.
    *   **Expected Balances** (cumulative):
        *   Arif Khan: Was owed 60, now owes 15 -> `+45.00`
        *   Alimun: Owed 30, but paid 50 for a 10 share -> `+10.00`
        *   Armish Khan: Owed 30, now owes another 25 -> `-55.00`
        ```json
        {
          "Arif Khan": 45.00,
          "Alimun": 10.00,
          "Armish Khan": -55.00
        }
        ```

3.  **Scenario C: "Project Lunch" - Independent Balances**
    *   **Action**: In Alimun's browser session, add an expense to the other group. Arif Khan pays 80 AED for Pizza, split equally.
    *   **Endpoint**: `POST /expenses`
    *   **Request Body**:
        ```json
        {
          "description": "Pizza",
          "amount": 80.00,
          "payerId": 1,
          "groupId": 2,
          "splits": {
            "1": 40.00,
            "2": 40.00
          }
        }
        ```
    *   **Check Balances**: Use `GET /groups/2/balances`.
    *   **Expected Balances** (This proves balances are independent per group):
        ```json
        {
          "Arif Khan": 40.00,
          "Alimun": -40.00
        }
        ```

4.  **Scenario D: "Road Trip" - Settlement**
    *   **Action**: Armish Khan pays Arif Khan 55 AED to settle his debt.
    *   **Endpoint**: `POST /settlements`
    *   **Request Body**:
        ```json
        {
          "payerId": 3,
          "receiverId": 1,
          "amount": 55.00,
          "groupId": 1
        }
        ```
    *   **Check Balances**: Use `GET /groups/1/balances`.
    *   **Expected Balances**:
        *   Arif Khan: Was owed 45, received 55 -> now owes 10.
        *   Armish Khan: Owed 55, paid 55 -> now settled.
        ```json
        {
          "Arif Khan": -10.00,
          "Alimun": 10.00,
          "Armish Khan": 0.00
        }
        ```

### **Step 4: Security and Authorization**

1.  **Unauthorized Group Access**:
    *   **Action**: Let's assume a fourth user, **Eve**, logs in but is not a member of any group.
    *   In Eve's browser session, try to get the expenses for the "Road Trip" group.
    *   **Endpoint**: `GET /groups/1/expenses`
    *   **Expected Response**: `403 Forbidden`.

2.  **Admin-Only Operations**:
    *   **Action**: In Alimun's browser session (he is a member, not admin of "Road Trip"), try to add Eve to the group.
    *   **Endpoint**: `POST /groups/1/members`
    *   **Request Body**: `{ "userId": 4 }`
    *   **Expected Response**: `403 Forbidden`.
    *   **Action**: In Alimun's session, try to delete the group.
    *   **Endpoint**: `DELETE /groups/1`
    *   **Expected Response**: `403 Forbidden`.

### **Step 5: Leaving a Group**

1.  **Attempt to Remove Member with Unsettled Balance**:
    *   **Context**: In the "Project Lunch" group (ID 2), Alimun owes Arif Khan 40 AED.
    *   **Action**: In Alimun's session (as admin), he tries to remove Arif Khan.
    *   **Endpoint**: `DELETE /groups/2/members/1`
    *   **Expected Response**: `400 Bad Request` or `500 Internal Server Error` with the message `"User has an unsettled balance and cannot be removed."`.

2.  **Successfully Remove Member After Settling**:
    *   **Action 1**: Alimun settles his debt with Arif Khan.
    *   **Endpoint**: `POST /settlements`
    *   **Request Body**: `{ "payerId": 2, "receiverId": 1, "amount": 40.00, "groupId": 2 }`
    *   **Action 2**: Check balances for group 2. They should both be `0.00`.
    *   **Action 3**: Alimun, as admin, now removes Arif Khan from the group.
    *   **Endpoint**: `DELETE /groups/2/members/1`
    *   **Expected Response**: `204 No Content`.

### **Step 6: Concurrency Testing (Conceptual)**

This test demonstrates the pessimistic locking.

1.  **Context**: In the "Road Trip" group, Alimun is owed 10 AED by Arif Khan.
2.  **Action**:
    *   Open two separate Swagger UI windows/tabs.
    *   In window 1, prepare a settlement from **Arif Khan to Alimun** for 5 AED.
    *   In window 2, prepare another settlement from **Arif Khan to Alimun** for 5 AED.
3.  **Execution**:
    *   Click "Execute" in both windows as quickly as possible.
4.  **Expected Behavior**:
    *   One of the requests will execute immediately.
    *   The second request will hang for a moment and then execute.
    *   Check the balances for group 1. The final balance should be correct (`Arif Khan: 0.00, Alimun: 0.00`). The locking mechanism prevents a race condition, ensuring both payments are processed sequentially and correctly.