# AGENTS.md

Prueba técnica: API REST en Spring Boot que expone el detalle de los productos similares a uno dado. El enunciado está en `docs/prueba-tecnica.md`. El README explica cómo ejecutar el proyecto.

## Aplicación

- La aplicación escucha en el puerto 5000.
- Contrato a exponer: `docs/similarProducts.yaml`. APIs existentes que se consumen: `docs/existingApis.yaml`, servidas por los mocks en `http://localhost:3001`.
- `shared/` y `docker-compose.yaml` forman parte de la evaluación y no se modifican. El contenido de los YAML de contrato tampoco.

## Git

- `main` es la rama troncal. Una rama corta por hito: `chore/...` o `feat/...`.
- Un pull request por hito y rebase and merge a `main`.
- Conventional Commits en inglés, sin scope.

## Asistencia al programador

No desarrolles código ni hagas modificaciones a no ser que se solicite expresamente o mediante un plan de desarrollo. Asiste investigando recursos externos, analizando las propuestas y apoyándote en el código y los requerimientos generados.
