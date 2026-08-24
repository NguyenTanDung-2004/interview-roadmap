## The Architecture (RAG & AI Agents)
**1. RAG (Retrieval-Augmented Generation)**
- **What it is:** An architecture connects LLMs with an external data source (like a Vector DBs) to give it real-time or private information.
- **Why it matters:** LLMs only know what they were trained on. RAG acts like an open-book exam. When you ask a question, they system uses embeddings to lookup relevant documents in a Vector DB, pastes that information into your prompt, and hands in to the LLM to write a perfectly accurate answer.

**2. AI Agents**
- **What it is:** An advanced AI desinged to act autonomously, make decisions, and execute multi-step tasks.
- **Why it matters:** While standard RAG just answer questions, an AI Agent can act. It is given a goal, loops through a reasoning process, choose which tools to use and complets the task on its own.