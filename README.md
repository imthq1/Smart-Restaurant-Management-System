# Smart Restaurant - QR-Based Self-Ordering System

A modern, high-performance Microservices system designed for smart restaurants. This application allows customers to scan a **unique QR code at their table** to browse the menu and place orders directly, eliminating the need for traditional manual service.

---

## Key Highlight: QR-Code Ordering
- **Table-Specific QR Codes:** Each table in the restaurant has a unique QR code generated/managed by the **Menu Service**.
- **Self-Service Workflow:** Customers scan the code -> View the real-time menu -> Place an order -> Order is instantly sent to the **Kitchen Service** via **Kafka**.
- **Table Management:** The **Menu Service** tracks table availability and links active sessions to specific table IDs.

---

## System Architecture

The project follows a cloud-native Microservices pattern:

* **API Gateway:** Routes customer requests from the QR-landing page to internal services.
* **Service Discovery (Eureka):** Dynamically manages service instances.
* **Event-Driven Architecture:** Uses **Apache Kafka** to decouple `Order Service` and `Kitchen Service`, ensuring orders are never lost.

---

## Microservices Overview

| Service | Primary Responsibility | Data Store |
| :--- | :--- | :--- |
| **Identity Service** | Auth & Permissions (Staff vs. Customer sessions). | MySQL |
| **Menu Service** | Digital Menu management & **Table/QR Code Management**. | MySQL |
| **Order Service** | Processes QR-initiated orders and tracks payment status. | MySQL |
| **Kitchen Service** | Receives real-time order streams from Kafka for chefs. | MySQL |
| **Calendar Service** | Internal scheduling (Staff shifts, Inventory audits, Meetings). | MySQL |
| **User Service** | Customer profile and loyalty points management. | MySQL |

---

## Tech Stack

* **Backend:** Java Spring Boot, Spring Cloud (Gateway, Eureka).
* **Database:** MySQL (Relational).
* **Messaging:** Apache Kafka (Event-driven updates).
* **Security:** JWT (JSON Web Tokens) for secure table sessions.
* **Build Tool:** Maven.

---

## How It Works

1.  **Customer Scans QR:** The URL contains a `table_id`. The system identifies the customer's location.
2.  **Menu Browsing:** `Menu Service` provides the digital catalog.
3.  **Order Placement:** `Order Service` validates the order and pushes a message to the `order-topic` in **Kafka**.
4.  **Kitchen Processing:** `Kitchen Service` consumes the message and displays the order on the chef's dashboard.
5.  **Internal Ops:** Managers use the `Calendar Service` to schedule "Stock-taking" or "Staff Meetings" without affecting the ordering flow.


## Author
- **GitHub:** [@imthq1](https://github.com/imthq1)
