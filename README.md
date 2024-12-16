# docker-plugin

The docker-plugin is one of many plugins of the [DeMAF](https://github.com/UST-DeMAF) project.
It is designed to transform Docker images into an [EDMM](https://github.com/UST-EDMM) representation.

The plugin only works (without adaptions) in the context of the entire DeMAF application using the [deployment-config](https://github.com/UST-DeMAF/deployment-config).
The documentation for setting up the entire DeMAF application locally is [here](https://github.com/UST-DeMAF/EnPro-Documentation).

## Usage

You can run the application without the [deployment-config](https://github.com/UST-DeMAF/deployment-config) but it will not run as it needs to register itself at the [analysis-manager](https://github.com/UST-DeMAF/analysis-manager).

If you want to boot it locally nevertheless use the following commands.

```shell
./mvnw spring-boot:run
```

or:

```shell
./mvnw package
java -jar target/docker-plugin-0.2.0-SNAPSHOT.jar
```

When running the project locally, ensure the plugin isn't also running in a Docker container to avoid port conflicts.

## Docker-Specific Configurations

Differences to other plugins:

## Debugging

If changes are made, the docker container has to be restarted in the [deployment-config](https://github.com/UST-DeMAF/deployment-config) shell, to update the plugin.

```shell
docker-compose pull && docker-compose up --build -d
```
