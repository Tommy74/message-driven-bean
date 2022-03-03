# JBoss EAP Messaging: remote queues

This repo explains how-to create an MDB that consumes messages from a remote queue;
The project starting point is [Using the Integrated Artemis Resource Adapter for Remote Connections](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/configuring_messaging/index#use_provided_amq_adapter);

We have two node:
- node-1: is the node where the queue is
- node-2: is the node where the MDB is; the MDB consumes messages from the queue on node-1

We assume you have two JBoss EAP installations in the following folders:

### jboss-eap-7.4-node-1

```shell
$ tree -L 1 jboss-eap-7.4-node-1
jboss-eap-7.4-node-1
├── appclient
├── bin
├── docs
├── domain
├── JBossEULA.txt
├── jboss-modules.jar
├── LICENSE.txt
├── migration
├── modules
├── standalone
├── version.txt
└── welcome-content
```
### jboss-eap-7.4-node-2

```shell
$ tree -L 1 jboss-eap-7.4-node-2
jboss-eap-7.4-node-2
├── appclient
├── bin
├── docs
├── domain
├── JBossEULA.txt
├── jboss-modules.jar
├── LICENSE.txt
├── migration
├── modules
├── standalone
├── version.txt
└── welcome-content
```

## node-1 setup

```shell
./jboss-eap-7.4-node-1/bin/jboss-cli.sh

embed-server --server-config=standalone-full-ha.xml
/subsystem=messaging-activemq/server=default:write-attribute(name=security-enabled,value=false)
jms-queue add --queue-address=MDBQueue1Node1 --entries=[java:/queue/MDBQueue1Node1]
jms-queue add --queue-address=MDBQueue2Node1 --entries=[java:/queue/MDBQueue2Node1]
```

## node-2 setup

```shell
./jboss-eap-7.4-node-2/bin/jboss-cli.sh

embed-server --server-config=standalone-full-ha.xml
/subsystem=messaging-activemq/server=default:write-attribute(name=security-enabled,value=false)
/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=remote-server:add(host=127.0.0.1, port=8080)
/subsystem=messaging-activemq/server=default/http-connector=remote-http-connector:add(socket-binding=remote-server,endpoint=http-acceptor)
/subsystem=messaging-activemq/server=default/pooled-connection-factory=remote-artemis:add(connectors=[remote-http-connector], entries=[java:/jms/remoteCF])
```

## node-1 start

```shell
./jboss-eap-7.4-node-1/bin/standalone.sh --server-config=standalone-full-ha.xml -Djboss.default.jgroups.stack=tcp -Dprogram.name=wfl1 -Djboss.node.name=wfl1 -Djboss.default.multicast.address=230.0.0.1
```

## node-2 start

```shell
./jboss-eap-7.4-node-2/bin/standalone.sh --server-config=standalone-full-ha.xml -Djboss.socket.binding.port-offset=1000 -Djboss.default.jgroups.stack=tcp -Dprogram.name=wfl2 -Djboss.node.name=wfl2 -Djboss.default.multicast.address=230.0.0.2
```