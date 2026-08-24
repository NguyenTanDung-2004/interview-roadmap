## Semisynchronous Replication

When we enable `Semisynchronous Replication`, every transaction that your source commits must be acknowledge at least one replica. The acknowledgement must be confirmed by they replica received it and wrote the commit to the its own relay log.

Since each transaction must wait the response from other nodes. => more latency to every transaction => Consider the trade-off involved.

Need to set the time frame. If after this time period, the replica still not acknowledge the commit. It not fail -> It will revert to asynchronous replication.