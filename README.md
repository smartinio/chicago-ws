# Chicago over Websockets
Play the card game Chicago with friends

## Development

### Prerequisites
* [Java 21 (GraalVM)](https://www.graalvm.org/jdk21/docs/getting-started)
* [Taskfile](https://taskfile.dev/installation)
* [Node.js](https://nodejs.org/en/download)
* [PNPM](https://pnpm.io/installation)

### Setup
Running the tasks in separate terminal tabs is recommended

```shell
# tab 1 (rebuilds server on source change)
task server.build

# tab 2 (reboots server on build change)
task server.run

# tab 3 (frontend app)
task client.run
```

See [taskfile.yml](taskfile.yml) for more info
