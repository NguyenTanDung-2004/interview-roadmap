## Global Transaction Identifiers 
MySQL gives every single Database change transaction its own unique ID passport.

A GTID looks like a combination of the server's unique ID and a counter number.
`b9acac5a-7bbe-11eb-a043-42010af8001a:1`

- How it work: When the main server creates a database, it labels that action as a transaction #1
- When a backup copies that transaction, this backup marks this transaction as `DONE`
- If the backup servers are turn off a few days while the main server still serve #2, #3, #4,... It does not matter. When the backup servers are turned back on, it simply tells the main server: `Hey, I have done up to #1. Please send me everything starting from #2`