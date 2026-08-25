# AWS Learning Project — Spring Boot + Serverless + Multi-Region

## 1. Goal

Build a simple Spring Boot application and progressively deploy it to AWS through 3 levels.

The purpose is not only to learn individual AWS services, but to understand how they work together in a real application.

### Learning path

```text
Level 1
Single Region

User
  |
  v
Route 53
  |
  v
API Gateway
  |
  v
Lambda
  |
  v
DynamoDB
```

```text
Level 2
Multi-Region Active-Passive

                    +--> Region A (ACTIVE)
User -> Route 53 --|
                    +--> Region B (PASSIVE)
```

```text
Level 3
Multi-Region Active-Active

                    +--> Region A (ACTIVE)
User -> Route 53 --|
                    +--> Region B (ACTIVE)
```

For Level 2 and Level 3, DynamoDB Global Tables can be used to replicate application data between Regions.

---

# 2. The Application

Build a simple REST API:

```text
GET    /users/{id}
POST   /users
PUT    /users/{id}
DELETE /users/{id}
```

Example user:

```json
{
  "id": "user-001",
  "name": "John",
  "email": "john@example.com"
}
```

The application will use:

* Java
* Spring Boot
* AWS Lambda
* Amazon API Gateway
* Amazon Route 53
* Amazon DynamoDB
* IAM
* CloudWatch

Later:

* DynamoDB Global Tables
* Multi-Region deployment
* Route 53 Failover Routing
* Route 53 Latency-Based Routing
* Active-Passive
* Active-Active

---

# 3. AWS Services

## 3.1 Amazon Route 53

### What is Route 53?

Amazon Route 53 is AWS's DNS service.

DNS converts a domain name such as:

```text
api.myapp.com
```

into the destination that should receive the request.

For example:

```text
User
 |
 | api.myapp.com
 v
Route 53
 |
 +----> Region A
 |
 +----> Region B
```

### Important idea

Route 53 does NOT normally execute our Java/Spring Boot application.

Instead, it decides:

> "Where should this user's request go?"

For example:

```text
api.myapp.com

        |
        v
    Route 53
        |
   +----+----+
   |         |
   v         v
Region A   Region B
```

### Important Route 53 concepts

Learn these first:

* Hosted Zone
* DNS Record
* A Record
* CNAME
* Alias Record
* TTL
* Health Check
* Failover Routing
* Latency-Based Routing
* Weighted Routing
* Geolocation Routing

### For this project

Level 1:

```text
api.example.com
       |
       v
API Gateway
```

Level 2:

```text
api.example.com
       |
       v
Route 53
       |
       +----> Region A
       |
       +----> Region B
```

Route 53 can detect unhealthy resources and route traffic away from them.

---

# 3.2 Amazon API Gateway

## What is API Gateway?

API Gateway provides an HTTP API endpoint for clients.

For example:

```text
POST https://api.example.com/users
```

It receives the HTTP request and forwards it to the backend.

Architecture:

```text
Client
  |
  | HTTP Request
  v
API Gateway
  |
  v
Lambda
```

### Why do we need API Gateway?

Because Lambda is a function, not traditionally a public REST API server.

API Gateway provides:

* HTTP endpoints
* Request routing
* Authentication/authorization integration
* Throttling
* Monitoring
* Integration with Lambda

### Example

```text
POST /users

        |
        v

API Gateway

        |
        v

Lambda

        |
        v

DynamoDB
```

---

# 3.3 AWS Lambda

## What is Lambda?

AWS Lambda is a serverless compute service.

You provide code.

AWS manages the servers required to execute that code.

Instead of running:

```text
Spring Boot
    |
    v
EC2
    |
    v
Linux Server
```

you can run:

```text
API Gateway
    |
    v
Lambda
    |
    v
Your Java Code
```

### Important Lambda concept

Lambda is event-driven.

For example:

```text
HTTP Request
     |
     v
API Gateway
     |
     v
Lambda
     |
     v
Java Code
```

Lambda can also be triggered by many other AWS services.

Examples:

```text
S3 Event
    |
    v
Lambda
```

```text
SQS Message
    |
    v
Lambda
```

```text
EventBridge Event
    |
    v
Lambda
```

### Important Lambda concepts

Learn:

* Function
* Handler
* Runtime
* Invocation
* Event
* Context
* Timeout
* Memory
* Environment Variables
* IAM Execution Role
* Cold Start
* Concurrency
* Provisioned Concurrency

---

# 3.4 DynamoDB

## What is DynamoDB?

Amazon DynamoDB is a fully managed NoSQL database.

Unlike a traditional relational database:

```text
PostgreSQL

Database
  |
  +-- Table
       |
       +-- Rows
            |
            +-- Columns
```

DynamoDB uses:

```text
DynamoDB
   |
   +-- Table
        |
        +-- Items
             |
             +-- Attributes
```

Example:

```json
{
  "id": "user-001",
  "name": "John",
  "email": "john@example.com"
}
```

### Important concepts

Learn:

* Table
* Item
* Attribute
* Partition Key
* Sort Key
* Primary Key
* Query
* Scan
* GSI
* LSI
* Capacity
* On-Demand
* Provisioned Capacity
* Eventually Consistent Read
* Strongly Consistent Read

---

# 3.5 IAM

## What is IAM?

IAM controls:

> Who can do what on which AWS resource?

Example:

```text
Lambda
   |
   | Assume Role
   v
IAM Role
   |
   | Permission
   v
DynamoDB
```

The Lambda function should NOT have unrestricted access to AWS.

Instead:

```text
Lambda
   |
   v
Lambda Execution Role
   |
   +---- dynamodb:GetItem
   +---- dynamodb:PutItem
   +---- dynamodb:UpdateItem
```

### Important IAM concepts

Learn:

* User
* Group
* Policy
* Role
* Permission
* Resource
* Action
* Principal
* Trust Policy
* Identity Policy
* Least Privilege

### Important rule

Do NOT put AWS access keys directly inside the Spring Boot source code.

Bad:

```java
String accessKey = "...";
String secretKey = "...";
```

Prefer IAM Roles whenever possible.

---

# 3.6 Amazon CloudWatch

## What is CloudWatch?

CloudWatch is used for monitoring and observability.

For Lambda, CloudWatch can provide:

* Logs
* Metrics
* Errors
* Invocation count
* Duration
* Alarms

Example:

```text
User
 |
 v
API Gateway
 |
 v
Lambda
 |
 +----> CloudWatch Logs
 |
 v
DynamoDB
```

If the Lambda fails:

```text
Lambda
   |
   X
   |
CloudWatch
   |
   +----> Error Log
```

---

# 4. Spring Boot Integration

## 4.1 Why Spring Boot?

The goal is to combine AWS knowledge with a technology that we already understand.

Instead of writing a Lambda function with a small amount of Java code, build the business logic using Spring Boot.

Example:

```text
Spring Boot

Controller
    |
    v
Service
    |
    v
Repository
    |
    v
DynamoDB
```

Example:

```text
UserController
      |
      v
UserService
      |
      v
UserRepository
      |
      v
DynamoDB
```

---

# 5. Recommended Project Structure

```text
aws-learning-project/
|
+-- src/
|   |
|   +-- main/
|       |
|       +-- java/
|           |
|           +-- com.example.aws/
|               |
|               +-- controller/
|               |    +-- UserController.java
|               |
|               +-- service/
|               |    +-- UserService.java
|               |
|               +-- repository/
|               |    +-- UserRepository.java
|               |
|               +-- model/
|               |    +-- User.java
|               |
|               +-- config/
|                    +-- DynamoDbConfig.java
|
+-- pom.xml
|
+-- README.md
```

---

# 6. Level 1 — Single Region

## Goal

Build a working application in ONE AWS Region.

Architecture:

```text
                         AWS Region A
                    +----------------------+

User
 |
 | HTTPS
 v
Route 53
 |
 v
API Gateway
 |
 v
Lambda
 |
 | AWS SDK
 v
DynamoDB
 |
 v
CloudWatch
```

### Request flow

When the user calls:

```text
GET https://api.example.com/users/user-001
```

the request flows:

```text
User
 |
 v
Route 53
 |
 | DNS resolution
 v
API Gateway
 |
 | Invoke
 v
Lambda
 |
 | Java/Spring Boot
 v
DynamoDB
 |
 v
Lambda
 |
 v
API Gateway
 |
 v
User
```

---

# 7. Level 1 — Spring Boot

## Option A — Spring Boot running normally

First, run Spring Boot as a normal application.

```text
Client
   |
   v
Spring Boot
   |
   v
DynamoDB
```

This is the easiest way to learn the AWS SDK.

The application can use:

```text
AWS SDK for Java
       |
       v
DynamoDB
```

Learn how to:

* Create a DynamoDB table
* Insert an item
* Get an item
* Update an item
* Delete an item
* Query an item

---

# 8. Level 1 — Move Spring Boot to Lambda

After the normal Spring Boot application works, deploy the backend to Lambda.

Architecture:

```text
API Gateway
     |
     v
Lambda
     |
     v
Spring Boot
     |
     v
DynamoDB
```

The important learning point is:

```text
Spring Boot
    |
    v
AWS Lambda
```

You are learning how a traditional Java backend can work in a serverless environment.

Be aware that Spring Boot can introduce startup overhead, so this stage is useful educationally but is not automatically the best choice for every Lambda workload.

---

# 9. Level 2 — Multi-Region Active-Passive

## Goal

Learn Disaster Recovery.

Use two AWS Regions:

```text
Region A = ACTIVE

Region B = PASSIVE
```

Architecture:

```text
                         Route 53
                            |
                            |
                 +----------+----------+
                 |                     |
                 v                     v
            Region A               Region B
             ACTIVE                PASSIVE
                 |                     |
                 v                     v
           API Gateway           API Gateway
                 |                     |
                 v                     v
              Lambda                Lambda
                 |                     |
                 v                     v
            DynamoDB              DynamoDB
```

Normal situation:

```text
User
 |
 v
Route 53
 |
 v
Region A
 |
 v
Application
```

If Region A fails:

```text
User
 |
 v
Route 53
 |
 X Region A
 |
 v
Region B
 |
 v
Application
```

Route 53 failover routing uses a primary and secondary resource and can switch to the secondary when the primary is unhealthy.

---

# 10. DynamoDB in Level 2

A problem appears:

```text
Region A
DynamoDB

        X

Region B
DynamoDB
```

If Region A has the latest data, Region B also needs that data when it becomes active.

Therefore, learn:

```text
DynamoDB Global Tables
```

Conceptually:

```text
              DynamoDB Global Table

                 +-------------+
                 |             |
                 v             v
             Region A       Region B
             DynamoDB       DynamoDB
                 ^             ^
                 |             |
                 +-------------+
                  Replication
```

The goal is:

```text
Region A
   |
   | replicate
   v
Region B
```

and vice versa where the chosen global-table configuration allows writes in multiple Regions.

---

# 11. Level 2 — Things to Learn

Before moving to Level 3, understand:

* AWS Regions
* Availability Zones
* Route 53 Health Checks
* Route 53 Failover Routing
* API Gateway regional endpoints
* Lambda deployment per Region
* DynamoDB Global Tables
* Data replication
* RTO
* RPO
* Disaster Recovery
* Failover
* Failback

---

# 12. RTO and RPO

## RTO — Recovery Time Objective

How quickly must the system recover?

Example:

```text
Region A fails

10:00:00 -> Failure
10:00:30 -> Region B receives traffic
```

RTO:

```text
30 seconds
```

---

## RPO — Recovery Point Objective

How much data can we potentially lose?

Example:

```text
Last replicated data:
10:00:00

Failure:
10:00:10
```

Potential data loss:

```text
10 seconds
```

RPO:

```text
10 seconds
```

---

# 13. Level 3 — Multi-Region Active-Active

## Goal

Both Regions serve traffic simultaneously.

```text
                    Route 53
                       |
              +--------+--------+
              |                 |
              v                 v
          Region A          Region B
           ACTIVE             ACTIVE
              |                 |
              v                 v
         API Gateway       API Gateway
              |                 |
              v                 v
           Lambda             Lambda
              |                 |
              v                 v
         DynamoDB <--------> DynamoDB
                    |
                    |
              Global Tables
```

Unlike Active-Passive:

```text
Active-Passive

Region A = serving traffic
Region B = waiting
```

Active-Active:

```text
Region A = serving traffic
Region B = serving traffic
```

Route 53 can use routing policies such as latency-based routing to direct users to appropriate healthy regional endpoints.

---

# 14. Level 3 — Why Active-Active Is Harder

Active-Active is not simply:

```text
Deploy the same application twice.
```

You need to think about:

### 1. Data synchronization

```text
Region A
   |
   v
DynamoDB
   |
   +------ replication ------+
                              |
                              v
                         Region B
```

### 2. Write conflicts

Imagine:

```text
Region A:

User name = "John"
```

At almost the same time:

```text
Region B:

User name = "Johnny"
```

Which value should win?

You need a strategy for conflict handling.

### 3. Traffic distribution

Users should be routed to the appropriate Region.

For example:

```text
Vietnam users
      |
      v
Singapore Region

US users
      |
      v
US Region
```

### 4. Failure handling

If Region A fails:

```text
Before:

        Route 53
        /      \
       v        v
    Region A  Region B
    ACTIVE    ACTIVE
```

After:

```text
        Route 53
             |
             v
          Region B
           ACTIVE
```

Region B must have enough capacity to handle the additional traffic.

---

# 15. Three-Level Learning Roadmap

## Level 1 — Single Region

### AWS

* [ ] Create AWS account/environment
* [ ] Learn IAM
* [ ] Create DynamoDB table
* [ ] Learn DynamoDB CRUD
* [ ] Create Lambda
* [ ] Deploy Java code to Lambda
* [ ] Create API Gateway
* [ ] Connect API Gateway to Lambda
* [ ] Create Route 53 Hosted Zone
* [ ] Connect custom domain to API Gateway
* [ ] Learn CloudWatch Logs

### Spring Boot

* [ ] Create Spring Boot REST API
* [ ] Create UserController
* [ ] Create UserService
* [ ] Create UserRepository
* [ ] Integrate AWS SDK for Java
* [ ] Connect Spring Boot to DynamoDB
* [ ] Test CRUD operations
* [ ] Deploy application to Lambda

### Final architecture

```text
Internet
   |
   v
Route 53
   |
   v
API Gateway
   |
   v
Lambda
   |
   v
Spring Boot
   |
   v
DynamoDB
```

---

# 16. Level 2 — Active-Passive

### AWS

* [ ] Create Region A
* [ ] Create Region B
* [ ] Deploy Lambda to both Regions
* [ ] Deploy API Gateway to both Regions
* [ ] Create DynamoDB Global Table
* [ ] Configure Route 53 Health Check
* [ ] Configure Route 53 Failover Routing
* [ ] Test Region A failure
* [ ] Verify traffic moves to Region B
* [ ] Test data availability after failover
* [ ] Learn RTO
* [ ] Learn RPO
* [ ] Practice failback

### Final architecture

```text
                     Route 53
                         |
              +----------+----------+
              |                     |
              v                     v
          Region A              Region B
           ACTIVE                PASSIVE
              |                     |
         API Gateway           API Gateway
              |                     |
           Lambda                Lambda
              |                     |
         DynamoDB <----------> DynamoDB
                 Global Table
```

---

# 17. Level 3 — Active-Active

### AWS

* [ ] Deploy both Regions as ACTIVE
* [ ] Configure Route 53 Latency-Based Routing
* [ ] Configure DynamoDB Global Tables
* [ ] Test writes in Region A
* [ ] Verify replication to Region B
* [ ] Test writes in Region B
* [ ] Test concurrent writes
* [ ] Understand conflict resolution
* [ ] Simulate Region A failure
* [ ] Verify Region B handles traffic
* [ ] Measure recovery time
* [ ] Test Region recovery
* [ ] Test failback

### Final architecture

```text
                         Route 53
                            |
                +-----------+-----------+
                |                       |
                v                       v
            Region A                Region B
             ACTIVE                  ACTIVE
                |                       |
           API Gateway             API Gateway
                |                       |
             Lambda                  Lambda
                |                       |
           DynamoDB <--------------> DynamoDB
                    Global Table
```

---

# 18. Recommended Project Progression

Do NOT start with Multi-Region.

Follow this order:

```text
1. Spring Boot locally
        |
        v
2. Spring Boot -> DynamoDB
        |
        v
3. Lambda
        |
        v
4. API Gateway -> Lambda
        |
        v
5. Route 53 -> API Gateway
        |
        v
6. CloudWatch
        |
        v
7. Region A + Region B
        |
        v
8. Active-Passive
        |
        v
9. DynamoDB Global Tables
        |
        v
10. Active-Active
```

This progression is important because every level introduces only a few new concepts.

---

# 19. What You Should Understand at Each Level

## Level 1

The main question is:

> How does a request travel through AWS?

You should be able to explain:

```text
User
 ↓
Route 53
 ↓
API Gateway
 ↓
Lambda
 ↓
Spring Boot
 ↓
DynamoDB
```

---

## Level 2

The main question is:

> What happens if an entire AWS Region fails?

You should be able to explain:

```text
Region A fails
      ↓
Route 53 detects failure
      ↓
Traffic goes to Region B
      ↓
Region B processes requests
      ↓
Data is available through replicated storage
```

---

## Level 3

The main question is:

> How can multiple Regions serve users at the same time?

You should be able to explain:

```text
             Route 53
             /      \
            /        \
       Region A    Region B
       ACTIVE       ACTIVE
          |            |
       Lambda       Lambda
          |            |
       DynamoDB <-> DynamoDB
```

And, more importantly, explain:

* How traffic is distributed
* How data is replicated
* What happens when one Region fails
* How conflicts are handled
* How the remaining Region handles additional traffic
* How the system recovers

---

# 20. Final Learning Goal

After completing all three levels, you should be able to look at this architecture:

```text
                         INTERNET
                            |
                            v
                       Route 53
                            |
                 +----------+----------+
                 |                     |
                 v                     v
             REGION A              REGION B
              ACTIVE                ACTIVE
                 |                     |
            API Gateway           API Gateway
                 |                     |
              Lambda                Lambda
                 |                     |
          Spring Boot App      Spring Boot App
                 |                     |
            DynamoDB  <----------> DynamoDB
                       Global Table
                 |
                 v
              CloudWatch
```

and explain:

1. What every component does
2. Why every component exists
3. How a request flows through the system
4. How IAM protects communication between services
5. How data is stored in DynamoDB
6. How Lambda executes the Java application
7. How Route 53 distributes traffic
8. How Active-Passive works
9. How Active-Active works
10. What happens when a Region fails
11. What RTO and RPO mean
12. What the trade-offs are between simplicity, cost, availability, and complexity

---

# 21. Important Note

The three levels are a good **learning progression**, but they should not be interpreted as:

```text
Level 1 = production architecture
Level 2 = better architecture
Level 3 = best architecture
```

Instead:

```text
Level 1
Learn AWS fundamentals

        ↓

Level 2
Learn Disaster Recovery

        ↓

Level 3
Learn High Availability + Multi-Region
```

Active-Active provides strong availability characteristics, but it is also significantly more complex and expensive. It requires careful handling of traffic distribution, capacity, data replication, and conflicts.

The goal of this project is therefore not just to "deploy something on AWS", but to understand **why each architecture works and what problems it solves**.
