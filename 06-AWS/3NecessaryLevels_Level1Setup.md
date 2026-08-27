# Step-by-Step Setup Guide

## Step1: Configure IAM Permissions
1. **Create or select an IAM User/Group in the AWS Management Console.**
2. **Attach the required policies:**
- **AWSLambda_FullAccess:** Deploy and manage Lambda functions.

- **AmazonAPIGatewayAdministrator:** Create and configure API Gateways.

- **AmazonRoute53FullAccess:** Manage DNS records and Hosted Zones.

- **IAMFullAccess (or iam:PassRole):** Allow passing execution roles to Lambda.

## Step2: Create Lambda Execution Role
1. Navigate to IAM -> Roles -> Create role.
2. Select AWS service -> Lambda
3. Attach the managed policy AWSLambdaBasicExecutionRole
4. Set Role Name: ...

## Step3: Create & Configure AWS Lambda
1. Go to AWS Lambda -> Create function -> Author from scratch
2. Set Function Name
3. Set Runtime: Java 21 (or ...)
4. Under **Execution Role**, Select Use an **Existing Role** -> choose the Role that you created in **Step2**
5. Upload SpringBoot jar via **CodeSource**
6. Set Runtime settings for **Handler**
7. Update genernal in **Configuration** tab
- Memory: 1024 MB
- Timeout: 15 seconds

## Step4: Link AWS API Gateway (HTTP API)
1. Go to API Gateway -> HTTP API -> Build
2. Add Integration: Lambda -> to the lambda service created in Step3
3. Configure Routes:
    - ANY / {proxy+} -> Target: to lambda service
    - ANY / -> TargetL to Lambda service
4. Keep Stage as $default with Auto-deploy enabled

*(Note: There are 2 Payload format version - 1.0 and 2.0.)*