# InterMW Integration Test

## Prerequisites

The integration test has the following prerequisites:

- Parliament
- RabbitMQ
- Inter-Platform Semantic Mediator (IPSM)

### Parliament Triple Store

Parliament can be started with the following command:
```
docker run -d --name parliament1 -p 8089:8089 daxid/parliament-triplestore
```

### RabbitMQ
RabbitMQ can be started with the following command:
```
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq:3.7-management-alpine
```

### IPSM
IPSM installation guide is available at [ipsm-deployment](https://git.inter-iot.eu/Inter-IoT/ipsm-deployment) repository.

Go to the IPSM Swagger UI at [http://localhost:8888/swagger/](http://localhost:8888/swagger/) and create channel for the `MWTestPlatform` platform using the `POST /channels` operation: 
```
{
  "source": "mw-ipsm-downstream-http_test.inter-iot.eu_test-platform1",
  "inpAlignmentId": 0,
  "outAlignmentId": 0,
  "sink": "ipsm-mw-downstream-http_test.inter-iot.eu_test-platform1",
  "parallelism": 1
}
```

```
{
  "source": "mw-ipsm-upstream-http_test.inter-iot.eu_test-platform1",
  "inpAlignmentId": 0,
  "outAlignmentId": 0,
  "sink": "ipsm-mw-upstream-http_test.inter-iot.eu_test-platform1",
  "parallelism": 1
}
```
