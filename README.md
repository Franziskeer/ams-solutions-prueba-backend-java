# AMS Solutions - Prueba técnica backend Java

API REST en Spring Boot que devuelve el detalle de los productos similares a uno dado. El enunciado está en [docs/prueba-tecnica.md](docs/prueba-tecnica.md).

## Versiones

- La solución está desarrollada con Java 25 y Spring Boot 4.1, y se construye con Maven a través del Maven Wrapper (`mvnw`), así que no hace falta tener Maven instalado.

Elegí Java 25 porque es la versión LTS actual y la aplicación usa virtual threads. Desde Java 24 un virtual thread que se bloquea dentro de un bloque `synchronized` ya no retiene el hilo real del sistema operativo (JEP 491), un matiz que en Java 21 obligaba a tener cuidado con algunas librerías.

## Cómo ejecutarlo

Es necesario tener instalado Docker para los mocks y la infraestructura de evaluación, y un JDK 25 para la aplicación.

Levantar los mocks y la infraestructura de evaluación:

```bash
docker-compose up -d simulado influxdb grafana
```

Levantar la aplicación, que escucha en el puerto 5000:

```bash
./mvnw spring-boot:run
```

Ejecutar el test de carga:

```bash
docker-compose run --rm k6 run scripts/test.js
```

Los resultados se ven en [Grafana](http://localhost:3000/d/Le2Ku9NMk/k6-performance-test).

### Tests

Los tests se lanzan con:

```bash
./mvnw test
```

### Integración continua

`.github/workflows/ci.yml` se ejecuta en cada pull request y en cada push a `main`. Lanza `./mvnw verify` con Java 25, que compila el proyecto y ejecuta los tests. El test de carga con k6 no forma parte de la pipeline: necesita los mocks, InfluxDB y Grafana levantados, y en los runners de GitHub los tiempos varían demasiado para que el resultado sea fiable. Se ejecuta en local con `docker-compose run --rm k6 run scripts/test.js`.

## Referencia

Este proyecto parte del enunciado y la infraestructura de evaluación de [dalogax/backendDevTest](https://github.com/dalogax/backendDevTest).
