## Data distribution
- **Statement-Based Replication (SBR):** Instead of sending the final data, this method sends the exact SQL command (the instructions) you typed.
    + MySQL sends `DELETE FROM users WHERE age > 60;` over the network. Your backup receives this command and run it.
    + Very little bandwidth because the text sentences are tiny.
    + The problem: it can cause errors with unpredictable commands. If  you type `INSERT INTO orders VALUES(NOW())` The backup server will run that command a few seconds later and insert the wrong time.

- **Row-Based Replication (RBR):** Instead of sending the intructions, this method sends the actual data changes.
    + If one command affected 5,000 users, MySQL will log and send the exact 5,000 rows that were removed.
    + It is 100% accurate and safe.
    + The problem: it uses much more bandwidth.


**=> MySQL also has a hybrid mode called Mixed-Based Replication. It uses the lightweight text commands by default to save the nextwork bandwidth, but automatically switches to sending the raw row data if it detects a tricky command (like a timestamp)**

## Scaling Read Traffic
MySQL replication can help you distribute read queries across several servers, which work very well for read-intensive applications.