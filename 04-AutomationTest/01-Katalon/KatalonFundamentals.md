## 1. Core Architecture & Key Concepts
Katalons operates on top of underlying frameword of **Selenium** and **Appium**.
- **Object Repository:** a centralized store where all UI element (button, textfield,...) are captured and organized using Locators (like CSS, ID, XPath)
- **Test Cases:** Individual Automated script that is designed to perform specific scenarios. 
    Katalon supports 2 main Modes:
    - **Manual Mode (Low-Code):** Use dropdown menu to drag and drop without writting code. 
    - **Script Mode (Groovy/Java):** Full access to code editor for complex testing.
- **Test Suites & Collections:** is used to groups test cases to execute sequentially or in parallel across different browser or environment.
    - **Test Suite:** Level1 Of Grouping
        - **Use Case:** Grouping test cases by feature, module, or test type (e.g., Login Module Suite, Checkout Flow Suite, Smoke Tests)
        - **Environment:** When executing a Test Suite directly, you choose one execution context (e.g., Chrome Edge, or Firefox) at the time of launching the run. All test cases inside will execute on that single selected browser.
        - **Data Driven:** This is where you map external data files (like Excel sheets) to the variable inside your test cases. 
    - **Test Suite Collection:** higher level container that groups multiple Test Suites together.
        - **Use Case:** Running cross-browser matrix tests, nightly regression builds, or multi-environment runs.
        - **Environment Flexibility:** Each Test Suite within the collection can be assinged its own target environment independently (SuiteA: on chrome, SuiteB: on Firefox,...)
        - **Parallelel Execution:** You can configured the collection to run test suites in parrallel using multiple threads to drastically reduce execution time

## 2. Mode of Test Creation
- **Record and Playback:** 
    - Best For: Rapid Prototyping & beginners
    - How it works:  Record the user action on a live Browser/app and auto generates test steps and UI objects.
- **Built-in Keyword** *(Simply select the action from a dropdown list and double-click to pass your values (like text, timeouts, or UI element,...) Katalon automatically generates the corresponding Groovy script behind the scenes in real time.)*
    - Best for: Low-code automation
    - How it works: Pre-made commands (setText, click,...) selected via Dropdown list.
- **Custom Keyword**
    - Best for: Advanced framework needs
    - How it works: Writing custom Groovy classes to extend native functionality

## 3. Essential Features
- **Self-Healing Locators:** When application code changes (eg., a button ID update). Katalon automatically attempts alternative locators (XPath, CSS) to prevent test breakage during execution
*(is only designed specifically to handle minor change like ID update,...)*
- **Data-Driven Testing (DDT):** Connects tests directly to external data source (Excel, CSV, SQL Database) to run the same script against multiple data sets.
- **Multi-Platform Support:** Single platform for webUI, Rest/SOAP APIs, Mobile (IOS, Android), and Desktop Apps *(Once Katalon is installed, you simply open the application, click New Project, and choose what you want to automate inside that single window.)*
- **Integration:** Direct plugins for CI/CD pipelines (Jenkins, Gitlab, Github Actions) and issue tracking (Jira,..).

*(KRE - Katalon Runtime Enginee: At aumotation step: pipeline pull the code of test-repo, then use KRE to run)*

*(KDI - Katalon Docker Image: Like KRE, but it use KDI to run the test repository - no need to install Java,...)*

*(Cloud API: Unlike KRE, KDI that runs code in the pipeline server. In this way, we have another server to do that.<br> - Step1: Trigger CI/CD by creating MR,... <br> - Step2: Pipleline has step that call to cloud server to run test (The cloud server return **JobID**) <br> - Step3: Cloud Server pulls code from test-repository <br> - Step4: Run Test on Cloud Server. <br> - Step5: (Run parallelly with the Step2) Polls to cloud server with **JobId** to get the status every 15-30 seconds<br> ...)*

## 4. Standard Katalon Workflow
1. **Create Project:** Choose application type (Web, API, Mobile, Desktop)
2. **Capture Objects:** Use Web Spy or Record to capture the elements locators into the Object Repository
3. **Build Test Steps:** Combine captured objects with keywords or Groovy code.
4. **Add Verifications:** Implement assertions (eg., check text present, check response code 200)
5. **Group into Test Suites:** Bundle tests into suites for structured execution.
6. **Execute & Analyze:** Run locally or via CI/CD, then review generated execution logs and reports.

## 5. Ecosystem Overview
- **Katalon Studio:** Primary IDE for test creation and local execution 

    - Test cases are written on Groovy -> when run, Katalon Studio compiles them into Java ByteCode via embedded JVM (Java Virtual Machine)
    - Katalon passes the low-level commands (click(), findElement(),...) down to standard underlying driver engines, based on what you are testing (Mobile App, Web, Desktop,...)
    - Katalon listens to browser/device responses => format logs/screenshot into its Job Progress panel and reports.
- **Katalon TestOps:** Cloud platform for centralized reporting, analytics, and execution orchestration.
- **Katalon TestCloud:** On-demand cloud infrastructure to execute tests across multiple browser
- **Katalon Academy:** Free, official courses and certifications for learning the platform.
