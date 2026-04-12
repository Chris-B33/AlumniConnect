# AlumniConnect — system context

This diagram is the ISO-style **system context** view: actors, the platform boundary, and external systems.

```mermaid
flowchart TB
    subgraph actors["Actors"]
        S["Student"]
        A["Alumni"]
        AD["Administrator"]
    end

    subgraph platform["AlumniConnect platform"]
        GW["API Gateway\n:8080"]
        ID["Identity service\n:8081"]
        MT["Mentorship service\n:8082"]
        EV["Event service\n:8083"]
        EU["Eureka\n:8761"]
        CFG["Config Server\n:8888"]
    end

    S --> GW
    A --> GW
    AD --> GW
    GW --> ID
    GW --> MT
    GW --> EV
    ID --> EU
    MT --> EU
    EV --> EU
    GW --> EU
    ID --> CFG
    MT --> CFG
    EV --> CFG
    EU --> CFG
```

Traffic enters through the **gateway**; services **register** with Eureka and load **centralised configuration** from the Config Server. See `README.md` for run order and ports.
