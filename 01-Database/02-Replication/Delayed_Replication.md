## DELAYED REPLICATION

The main reason to use delayed replication is to safeguard against accidential data-loss scenarios by human error or bad application deployments. (an accidental `DROP TABLE` or a `DELETE` statement)

If a table is accidentally dropped:

**1. Traditional Backup Restore:** Restoring from a standard backup can take hours or even days depending on the size of the database.

**2. Delayed Replica Restore:** Because the delayed replica is running hours behind, the accidental change hasn't executed on it yet. You can find GTID (Global Transaction Identifier) of destructive statement, stop replication on the delayed replica, and only replicate to the exact transaction just before the mistake happend. 

## TRADE OFFs and Complexities
- **Failover Risks:** If your database environment uses automated failover (promote a replica to become new source when the primary dies), you must explicitly exclude the delayed replica. Accidentally using this config can cause massive data loss and inconsistency.

- **Monitorning Complications:** Standard Replication Monitoring usually allerts administrators when a replica falls behind (lag). Monitors must be configured to understand that what replica is intentionally lagging => preventing false alarms

- **Data Age:** The data on delayed replica is stale. It can not be reliably used for live read-scaling or real-time analytics reporting.