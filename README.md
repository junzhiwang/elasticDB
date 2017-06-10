# elasticDB
* Developed an elastic distributed database by building a **MySQL replication** based on **asynchronized Master-Slave**
architecture and utilized it to serve as backend for a multi-tier web application running **TPC-W benchmark**.
* Created a **load balancer** to route browsing queries and ordering queries in **round-robin** manner.
* Guaranteed the **high availability (HA)** of database in real-time by utilizing **backup slaves**.
* **Scaled out/in** database server according to service level agreement when real-time workload changes.
* Monitored workload/database/operating system and demonstrate metrics by **CanvasJS**.
* To Improve the consistency, set a **semi-sync slave** and make it new master when old one is down.
