## The Infrastructure (Embeedings & Vector DBs)
These two components work together to give AI a mathematical way to read and remember information.

**1. Embeddings**
- **What is it:** The process of converting human data (text, images, audio) into a long list of numbers called a vector.
- **Why it matters:** Computers cannot read meaning, but they can calculate distances. An embedding model assigns coordinates to pieces of data so that things with simmilar meanings are mathematically placed close together.

**2. Vector Databases (Vector DBs)**
- **What it is:** A specialized database desinged specificially to store and rapidly search through milions of these embeddings.
- **Why it matters:** Traditional database look for exact keyword matches. A vector DB uses mathematical equations to perform a similarity search, allowing an AI system to instantly find concepts, ideas, or documents that match the intent of user's query. Even if the exact words don't match.
