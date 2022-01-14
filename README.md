# Postgres

```shell
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees -e POSTGRES_PASSWORD=employees -p 5432:5432  --name employees-postgres postgres
docker build -t employees .
docker compose up
docker run -d -e POSTGRES_DB=timesheet -e POSTGRES_USER=timesheet -e POSTGRES_PASSWORD=timesheet -p 5433:5432  --name timesheet-postgres postgres
```