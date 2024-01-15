# Configuration

## Hazelcast
We need to define a standard way for the schedulers cache(hazelcast) to discover each other

### EC2
Since schedulers will only run in AWS we plan on using tags in conjunction with:
Tags: 1. `Hazelcast:Scheduler`

Should be a simple tag to build off.