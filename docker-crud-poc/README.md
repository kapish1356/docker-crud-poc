# Docker CRUD POC (Spring Boot + H2)

Simple **Employee CRUD** REST API (Java 21, Spring Boot 3.3, H2) jisko **Docker** me run karna sikhne ke liye banaya gaya hai.

## Project structure

```
docker-crud-poc/
├── Dockerfile              <- image kaise banegi (multi-stage)
├── docker-compose.yml      <- container kaise chalega (port, volume)
├── .dockerignore           <- Docker build se kya skip karna hai
├── pom.xml
└── src/main/java/com/example/dockerpoc/
    ├── controller/  (EmployeeController, HomeController)
    ├── service/     (EmployeeService)
    ├── repository/  (EmployeeRepository)
    ├── model/       (Employee)
    ├── exception/   (404 + validation handler)
    └── config/      (DataLoader - 2 sample employees)
```

## API endpoints

| Method | URL | Kaam |
|--------|-----|------|
| GET    | `/api/employees`      | Sab employees |
| GET    | `/api/employees/{id}` | Ek employee |
| POST   | `/api/employees`      | Naya employee |
| PUT    | `/api/employees/{id}` | Update |
| DELETE | `/api/employees/{id}` | Delete |
| GET    | `/`                   | App info + container id |
| GET    | `/actuator/health`    | Health check |
| GET    | `/h2-console`         | H2 DB UI |

Sample body (POST / PUT):

```json
{
  "name": "Amit Kumar",
  "email": "amit@example.com",
  "department": "Finance",
  "salary": 50000
}
```

---

# Docker step by step

## Step 0 - Docker install check

Docker Desktop install karo (Windows/Mac) ya Docker Engine (Linux), phir:

```bash
docker --version
docker compose version
```

Docker Desktop **open/running** hona chahiye.

## Step 1 - (Optional) Bina Docker ke pehle app chalake dekho

```bash
mvn spring-boot:run
```

Browser: http://localhost:8080/api/employees  -> Ctrl+C se band karo.

## Step 2 - Dockerfile samjho

`Dockerfile` me 2 stages hain:

1. **build stage** - `maven + JDK` image me code compile hota hai aur `app.jar` banta hai.
2. **runtime stage** - sirf `JRE` image me jar copy hoti hai. Isse final image chhoti rehti hai.

Important lines:

| Line | Matlab |
|------|--------|
| `FROM ... AS build` | Base image + stage ka naam |
| `COPY pom.xml .` + `dependency:go-offline` | Dependencies cache hoti hain, baar baar download nahi |
| `COPY --from=build` | Pehle stage se sirf jar uthana |
| `VOLUME /app/data` | H2 DB files yaha save hoti hain |
| `EXPOSE 8080` | App ka port (documentation) |
| `HEALTHCHECK` | Docker check karta hai app zinda hai ya nahi |
| `ENTRYPOINT` | Container start hone par command |

## Step 3 - Image build karo

Project folder me (jaha `Dockerfile` hai):

```bash
docker build -t docker-crud-poc:1.0 .
```

* `-t` = tag (naam:version)
* `.` = current folder build context

Pehli baar 3-5 minute lag sakte hain (dependencies download). Check karo:

```bash
docker images
```

## Step 4 - Container run karo

```bash
docker run -d --name employee-app -p 8080:8080 docker-crud-poc:1.0
```

* `-d` = background (detached)
* `--name` = container ka naam
* `-p 8080:8080` = **laptop port : container port**

Check karo:

```bash
docker ps
docker logs -f employee-app        # live logs (Ctrl+C se bahar)
```

## Step 5 - API test karo

Browser / Postman / curl:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/employees
```

POST (naya employee):

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Amit Kumar","email":"amit@example.com","department":"Finance","salary":50000}'
```

PUT (update):

```bash
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Rahul Sharma","email":"rahul@example.com","department":"Engineering","salary":75000}'
```

DELETE:

```bash
curl -X DELETE http://localhost:8080/api/employees/2
```

`GET /` me `hostname` dikhega - wo **container ID** hai, matlab app sach me container ke andar chal rahi hai.

## Step 6 - Container ke andar jaakar dekho

```bash
docker exec -it employee-app bash
ls /app
exit
```

## Step 7 - Stop / Start / Delete

```bash
docker stop employee-app
docker start employee-app
docker rm -f employee-app        # container delete
docker rmi docker-crud-poc:1.0   # image delete
```

## Step 8 - Data persist karna (Volume) - IMPORTANT

Bina volume ke, container delete hote hi H2 data chala jata hai. Volume ke saath run karo:

```bash
docker rm -f employee-app
docker run -d --name employee-app -p 8080:8080 -v h2-data:/app/data docker-crud-poc:1.0
```

**Test:**
1. Ek employee POST karo.
2. `docker rm -f employee-app`
3. Dobara upar wali `docker run -v ...` command chalao.
4. `GET /api/employees` -> tumhara employee abhi bhi milega.

```bash
docker volume ls
docker volume rm h2-data     # data permanently delete
```

## Step 9 - Docker Compose (sabse easy tarika)

Ab har baar lambi `docker run` command nahi likhni. `docker-compose.yml` sab define karta hai:

```bash
docker compose up -d --build     # build + start
docker compose ps
docker compose logs -f
docker compose down              # stop + remove container (volume safe rehta hai)
docker compose down -v           # volume bhi delete
```

## Step 10 - Environment variable se config badalna

Example: port badalna (laptop par 9090):

```bash
docker run -d --name employee-app -p 9090:8080 docker-crud-poc:1.0
```

Spring property override (env var se):

```bash
docker run -d --name employee-app -p 8080:8080 \
  -e SPRING_JPA_SHOW_SQL=true \
  docker-crud-poc:1.0
```

## H2 Console (container ke saath)

http://localhost:8080/h2-console

* JDBC URL: `jdbc:h2:file:./data/employeedb`
* User: `sa`, Password: *(khali)*

## Common problems

| Problem | Solution |
|---------|----------|
| `Cannot connect to the Docker daemon` | Docker Desktop start karo |
| `port is already allocated` | Port 8080 busy hai -> `-p 9090:8080` use karo ya IntelliJ app band karo |
| Build me dependency download fail | Internet / proxy check karo, phir dobara `docker build` |
| Container turant exit | `docker logs employee-app` dekho |
| Data gayab ho gaya | Volume (`-v h2-data:/app/data`) use karo |
| Code change ke baad purana code chal raha | `docker compose up -d --build` ya dobara `docker build` |

## Useful commands cheat sheet

```bash
docker build -t name:tag .        # image banao
docker images                     # images list
docker run -d -p H:C --name n img # container chalao
docker ps / docker ps -a          # running / all containers
docker logs -f n                  # logs
docker exec -it n bash            # andar jao
docker stop n / start n / rm -f n
docker rmi img                    # image delete
docker system prune               # unused cheezein saaf
```
