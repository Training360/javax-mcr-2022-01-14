
class: inverse, center, middle

# Microservice alkalmazás felépítése Spring Boot keretrendszerrel Docker környezetben

---

class: inverse, center, middle

# Integrációs tesztek

---

## Integrációs tesztek

* Üres teszt a konfiguráció ellenőrzésére, elindul-e az application context
* `@SpringBootTest` annotáció: tartalmazza <br /> az `@ExtendWith(SpringExtension.class)` annotációt
* Tesztesetek között cache-eli az application contextet
* Beanek injektálhatóak az `@Autowired` annotációval

---

## Integrációs tesztek példa

```java
@SpringBootTest
public class EmployeesControllerIT {

  @Autowired
  EmployeesController employeesController;

  @Test
  void testSayHello() {
    String message = employeesController
      .helloWorld();
    assertThat(message).startsWith("Hello");
  }
}
```

---

class: inverse, center, middle

# Bevezetés a Docker használatába

---

## Docker

* Operációs rendszer szintű virtualizáció
* Jól elkülönített környezetek, saját fájlrendszerrel és telepített szoftverekkel
* Jól meghatározott módon kommunikálhatnak egymással
* Kevésbé erőforrásigényes, mint a virtualizáció

---

## Megvalósítása

* Kliens - szerver architektúra, REST API
* Kernelt nem tartalmaz, hanem a host Linux kernel izoláltan futtatja
 * namespaces: operációs rendszer szintű elemek izolálására: folyamatok, InterProcess Communication (IPC), 
     fájlrendszer, hálózat, UTS (UNIX Timesharing System - host- és domainnév), felhasználók
 * cGroups (Control Groups): erőforrás limitáció
* Union FS (írásvédett, vagy írható/olvasható rétegek)

---

## Docker

<img src="images/container-what-is-container.png" alt="Docker" width="500" />

---

## Docker Windowson

* Docker Toolbox: VirtualBoxon futó Linuxon
* Docker Desktop
  * Hyper-V megoldás: LinuxKit, Linux Containers for Windows (LCOW), MobyVM
  * WSL2 - Windows Subsystem for Linux - 2-es verziótól Microsoft által Windowson fordított és futtatott Linux kernel

---

## Docker felhasználási módja

* Saját fejlesztői környezetben reprodukálható erőforrások
  * Adatbázis (relációs/NoSQL), cache, kapcsolódó rendszerek <br /> (kifejezetten microservice környezetben)
* Saját fejlesztői környezettől való izoláció
* Docker image tartalmazza a teljes környezetet, függőségeket is
* Portabilitás (különböző környezeten futattható, pl. saját gép, <br /> privát vagy publikus felhő)

---

## További Docker komponensek

* Docker Hub - publikus szolgáltatás image-ek megosztására
* Docker Compose - több konténer egyidejű kezelése
* Docker Swarm - natív cluster támogatás
* Docker Machine - távoli Docker környezetek üzemeltetéséhez

---

## Docker fogalmak

<img src="images/docker-containers.jpg" alt="Image és container" width="500" />

---

## Docker folyamat

* Alkalmazás
* Dockerfile
* Image
* Konténer

---

## Docker konténerek

```shell
docker version
docker run hello-world
docker run -p 8080:80 nginx
docker run -d -p 8080:80 nginx
docker ps
docker stop 517e15770697
docker run -d -p 8080:80 --name nginx nginx
docker stop nginx
docker ps -a
docker start nginx
docker logs -f nginx
docker stop nginx
docker rm nginx
```

Használható az azonosító első n karaktere is, amely egyedivé teszi

---

## Műveletek image-ekkel

```shell
docker images
docker rmi nginx
```

---

## Linux elindítása, bejelentkezés

```shell
docker run  --name myubuntu -d ubuntu tail -f /dev/null
docker exec -it myubuntu bash
```

---

class: inverse, center, middle

# Java alkalmazások Dockerrel

---

## Saját image összeállítása

`Dockerfile` fájl tartalma:

```dockerfile
FROM adoptopenjdk:14-jre-hotspot
WORKDIR /opt/app
COPY target/*.jar employees.jar
CMD ["java", "-jar", "employees.jar"]
```

Parancsok:

```shell
docker build -t employees .
docker run -d -p 8080:8080 employees
```

---

## docker-maven-plugin

* Fabric8
* Alternatíva: Spotify dockerfile-maven, Google [JIB Maven plugin](https://github.com/GoogleContainerTools/jib)

---

## Plugin

```xml
<plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>0.32.0</version>
    <!-- ... -->    
</plugin>
```

---

## Plugin konfiguráció

.small-code-14[
```xml
<configuration>
    <verbose>true</verbose>
    <images>
        <image>
            <name>employees</name>
            <build>
                <dockerFileDir>${project.basedir}/src/main/docker/</dockerFileDir>
                <assembly>
                    <descriptorRef>artifact</descriptorRef>
                </assembly>
                <tags>
                    <tag>latest</tag>
                    <tag>${project.version}</tag>
                </tags>
            </build>
            <run>
                <ports>8080:8080</ports>
            </run>
        </image>
    </images>
</configuration>
```
]

---

## Dockerfile

```dockerfile
FROM adoptopenjdk:14-jre-hotspot
RUN mkdir /opt/app
ADD maven/${project.artifactId}-${project.version}.jar \
  /opt/app/employees.jar
CMD ["java", "-jar", "/opt/app/employees.jar"]
```

Property placeholder

---

## Parancsok

```shell
mvnw package docker:build
mvnw docker:start
mvnw docker:stop
```

A `docker:stop` törli is a konténert

---

## 12Factor hivatkozás: <br /> Disposability

* Nagyon gyorsan induljanak és álljanak le
* Graceful shutdown
* Ne legyen inkonzisztens adat
* Batch folyamatoknál: megszakíthatóvá, újraindíthatóvá (reentrant)
    * Tranzakciókezeléssel
    * Idempotencia


---

class: inverse, center, middle

# Docker layers

---

## Layers

<img src="images/docker-layers.png" alt="Docker layers" width="500"/>

`docker image inspect employees`

---

## Legjobb gyakorlat

* Külön változó részeket külön layerbe tenni
* Operációs rendszer, JDK, libraries, alkalmazás saját fejlesztésű része külön <br /> layerbe kerüljön

---

## Manuálisan

* Jar fájlt ki kell csomagolni, úgy is futtatható
  * `BOOT-INF/lib` - függőségek
  * `META-INF` - leíró állományok
  * `BOOT-INF/classes` - alkalmazás saját fájljai
  
```shell
java -cp BOOT-INF\classes;BOOT-INF\lib\* training.employees.EmployeesApplication
```  

---

## Dockerfile

* [Multi-stage build](https://docs.docker.com/develop/develop-images/multistage-build/)
  
```dockerfile
FROM adoptopenjdk:14-jdk-hotspot as builder
WORKDIR app
COPY target/employees-0.0.1-SNAPSHOT.jar employees.jar
RUN jar xvf employees.jar

FROM adoptopenjdk:14-jre-hotspot
WORKDIR app
COPY --from=builder app/BOOT-INF/lib lib
COPY --from=builder app/META-INF META-INF
COPY --from=builder app/BOOT-INF/classes classes

ENTRYPOINT ["java", "-cp", "classes:lib/*", \
            	"training.employees.EmployeesApplication"]
```

---

## Spring támogatás

* Spring 2.3.0.M2-től
    * [Bejelentés](https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1)
* Layered JAR-s
* Buildpacks

---

## Layered JAR-s

* A JAR felépítése legyen layered
* Ki kell csomagolni
* Létrehozni a Docker image-t

---

## Layered JAR

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <layers>
      <enabled>true</enabled>
    </layers>
  </configuration>
</plugin>
```

---

## Kicsomagolás

```shell
java -Djarmode=layertools -jar target/employees-0.0.1-SNAPSHOT.jar list

java -Djarmode=layertools -jar target/employees-0.0.1-SNAPSHOT.jar extract
```

---

## Dockerfile

```dockerfile
FROM adoptopenjdk:14-jre-hotspot as builder
WORKDIR application
COPY target/employees-0.0.1-SNAPSHOT.jar employees.jar
RUN java -Djarmode=layertools -jar employees.jar extract

FROM adoptopenjdk:14-jre-hotspot
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", \
  "org.springframework.boot.loader.JarLauncher"]
```

---

## Buildpacks

* Dockerfile-hoz képest magasabb absztrakciós szint (Cloud Foundry vagy Heroku)
* Image készítése közvetlen Maven-ből vagy Grade-ből
* Alapesetben Java 11, spring-boot-maven-plugin konfigurálandó <br /> `BP_JAVA_VERSION` értéke `13.0.2`

```shell
mvnw spring-boot:build-image
docker run -d -p 8080:8080 --name employees employees:0.0.1-SNAPSHOT
docker logs -f employees
```

---

## 12Factor hivatkozás: <br /> Dependencies

* Az alkalmazás nem függhet az őt futtató környezetre telepített <br /> semmilyen csomagtól
* Függőségeket explicit deklarálni kell
* Nem a függőségek közé soroljuk a háttérszolgáltatásokat, <br /> mint pl. adatbázis, mail szerver, cache szerver, stb.
* Docker és Maven/Gradle segít
* Egybe kell csomagolni a függőségekkel, <br /> hiszen a futtató környezetben szükség van rá
* Függőségek ritkábban változnak: Docker layers
* Vigyázni az ismételhetőségre: ne használjunk <br /> intervallumokat!

---

class: inverse, center, middle

# Státuszkódok és hibakezelés

## RFC 7807

* Problem Details for HTTP APIs
* `application/problem+json` mime-type

```json
{
    "type": "employees/invalid-json-request",
    "title": "JSON error",
    "status": 400,
    "detail": "JSON parse error: Unexpected character..."
}
```

---

## RFC 7807 mezők

* `type`: URI, mely azonosítja a hiba típusát
* `title`: ember által olvasható üzenet
* `status`: http státuszkód
* `detail`: részletek, ember által olvasható
* `instance`: URI, mely azonosítja a hibát, <br />és később is elérhető (pl. valamilyen log hivatkozás)
* Egyedi saját mezők definiálhatók

---

## org.zalando:problem

```xml
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>problem</artifactId>
  <version>${problem.version}</version>
</dependency>
<dependency>
  <groupId>org.zalando</groupId>
  <artifactId>jackson-datatype-problem</artifactId>
  <version>${problem.version}</version>
</dependency>
```

---

## A problem használata

```java
@ExceptionHandler({IllegalArgumentException.class})
public ResponseEntity<Problem> handleNotFound(IllegalArgumentException  e) {
    Problem problem = Problem.builder()
            .withType(URI.create("employees/employee-not-found"))
            .withTitle("Not found")
            .withStatus(Status.NOT_FOUND)
            .withDetail(e.getMessage())
            .build();

    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .body(problem);
}
```

```java
@Bean
public ObjectMapper objectMapper() {
  return new ObjectMapper()
  .findAndRegisterModules();
}
```

---

## problem-spring-web-starter

* Integrált Spring MVC kivételkezelés és Problem 3rd-party library

```xml
<dependency>
   <groupId>org.zalando</groupId>
   <artifactId>problem-spring-web-starter</artifactId>
   <version>0.26.2</version>
</dependency>
```

---

## Hiba személyre szabása

* Beépítetten több kivételt kezel
* Vagy a `AbstractThrowableProblem` kivételtől származik a saját kivétel osztályunk
* Saját `AdviceTrait` implementálása, mely saját `Problem` példányt hoz létre saját kivétel osztályunk esetén

```java
public class EmployeeNotFoundException extends AbstractThrowableProblem {

    private static final URI TYPE
            = URI.create("employees/employee-not-found");

    public EmployeeNotFoundException(Long id) {
        super(
                TYPE,
                "Not found",
                Status.NOT_FOUND,
                String.format("Employee with id '%d' not found", id));
    }
}
```

---

class: inverse, center, middle

# Integrációs tesztelés

---

## Web réteg tesztelése

* Elindítható csak a Spring MVC réteg: <br />`@SpringBootTest` helyett `@WebMvcTest` annotáció használata
* Service réteg mockolható Mockitoval, `@MockBean` annotációval
* `MockMvc` injektálható
    * Kérések összeállítására (path variable, paraméterek, header, stb.)
    * Válasz ellenőrzésére (státuszkód, header, tartalom)
    * Válasz naplózására
    * Válasz akár Stringként, JSON dokumentumként <br />(jsonPath)
* Nem indít valódi konténert, a Servlet API-t mockolja
* JSON szerializáció

---

## Web réteg tesztelése példa

.small-code-14[
```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
```

```java
@Test
void testListEmployees() throws Exception {
    when(employeesService.listEmployees(any())).thenReturn(List.of(
            new EmployeeDto(1L, "John Doe"),
            new EmployeeDto(2L, "Jane Doe")
    ));

    mockMvc.perform(get("/api/employees"))
      .andDo(print())
            .andExpect(status().isOk())        
            .andExpect(jsonPath("$[0].name", equalTo("John Doe")));
}
```
]

---

## Teljes alkalmazás tesztelése <br /> konténer nélkül

* `@SpringBootTest` és `@AutoConfigureMockMvc` annotáció

```java
@Test
void testListEmployees() throws Exception {
  mockMvc.perform(get("/api/employees"))
    .andExpect(status().isOk())
    .andDo(print())
    .andExpect(
      jsonPath("$[0].name", equalTo("John Doe")));
}
```

---

## Teljes alkalmazás tesztelése <br /> konténerrel

* `RANDOM_PORT`
* Port `@LocalServerPort` annotációval injektálható
* Injektálható `TestRestTemplate` - url és port előre beállítva
* JSON szerializáció és deszerializáció

```java
@SpringBootTest(webEnvironment =
    SpringBootTest.WebEnvironment.RANDOM_PORT)
```

---

## Teljes alkalmazás tesztelése <br /> konténerrel példa

```java
@Test
void testListEmployees() {
  List<EmployeeDto> employees = 
    restTemplate.exchange("/api/employees",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<List<EmployeeDto>>(){})
    .getBody();
  
  assertThat(employees)
          .extracting(EmployeeDto::getName)
          .containsExactly("John Doe", "Jane Doe");
}
```


class: inverse, center, middle

# Swagger UI

---

## Swagger UI

* API dokumentáció generálására
* Az API ki is próbálható
* OpenAPI Specification (eredetileg Swagger Specification)
  * RESTful webszolgáltatások leírására
  * Kód és dokumentáció generálás
  * Programozási nyelv független
  * JSON/YAML formátumú
  * JSON Scheman alapul
* Keretrendszer független
* Annotációkkal személyre szabható

---

## springdoc-openapi projekt

* Swagger UI automatikus elindítása a `/swagger-ui.html` címen
* OpenAPI elérhetőség a `/v3/api-docs` címen (vagy `/v3/api-docs.yaml`)

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-ui</artifactId>
  <version>${springdoc-openapi-ui.version}</version>
</dependency>
```

---

## Globális testreszabás

```java
@Bean
public OpenAPI customOpenAPI() {
  return new OpenAPI()
  .info(new Info()
  .title("Employees API")
  .version("1.0.0")
  .description("Operations with employees"));
}
```

---

## Séma testreszabás

* Figyelembe veszi a Bean Validation annotációkat

```java
public class CreateEmployeeCommand {

    @NotNull
    @Schema(description="name of the employee", example = "John Doe")
    private String name;
}
```

---

## Osztály és metódus szint

* Figyelembe veszi a Spring MVC annotációkat

```java
@RestController
@RequestMapping("/api/employees")
@Tag( name = "Operations on employees")
public class EmployeesController {

  @GetMapping("/{id}")
  @Operation(summary = "Find employee by id",
    description = "Find employee by id.")
  @ApiResponse(responseCode = "404",
    description = "Employee not found")
  public EmployeeDto findEmployeeById(
      @Parameter(description = "Id of the employee",
        example = "12")
      @PathVariable("id") long id) {
    // ...
  }

}
```

---

## 12Factor hivatkozás: API first

* Contract first alapjain
* Laza csatolás
* Webes és mobil GUI és az üzleti logika is ide tartozik
* Dokumentálva és tesztelve legyen
* [API Blueprint](https://apiblueprint.org/): Markdown alapú formátum API dokumentálására

---

class: inverse, center, middle

# Tesztelés REST Assured használatával

---

## REST Assured

* Keretrendszer független eszköz REST API tesztelésére
* Dinamikus nyelvek egyszerűségét próbálja hozni Java nyelven
* JSON, mint szöveg, vagy objektum mapping (Jackson, Gson, JAXB, stb.)
* Megadható
  * Path, parameter, header, cookie, content-type, stb.
* Sokszínű assertek
* Támogatja a különböző autentikációs módokat

---

## REST Assured - Assert

* XML tartalomra XmlPath, GPath (Groovy-ból, hasonló az XPath-hoz)
* DTD és XSD validáció
* JSON tartalomra JSONPath-szal
* JSON Schema validáció
* Header, status, cookie, content-type
* Response time


---

## Függőségek - JsonPath és XmlPath

```xml
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>json-path</artifactId>
  <version>${rest-assured.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>xml-path</artifactId>
  <version>${rest-assured.version}</version>
  <scope>test</scope>
</dependency>
```

Csak JSON használata esetén is kell mindkét függőség

---

## Függőségek

* Un. RestAssuredMockMvc API

```xml
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <version>${rest-assured.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>spring-mock-mvc</artifactId>
  <version>${rest-assured.version}</version>
  <scope>test</scope>
</dependency>
```

---

## Inicializálás

```java
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static org.hamcrest.Matchers.equalTo;
```

```java
@Autowired
WebApplicationContext webApplicationContext;

@BeforeEach
void init() {
  RestAssuredMockMvc.requestSpecification = given()
    .contentType(ContentType.JSON)
    .accept(ContentType.JSON);

  RestAssuredMockMvc
    .webAppContextSetup(webApplicationContext);
}
```

---

## Teszteset

```java
@Test
void testCreateEmployeeThenListEmployees() {
    with().body(new CreateEmployeeCommand("Jack Doe")).
    when()
      .post("/api/employees")
      .then()
      .body("name", equalTo("Jack Doe"));

    when()
      .get("/api/employees")
      .then()
      .body("[0].name", equalTo("Jack Doe"));
}
```

---

class: inverse, center, middle

# REST Assured séma validáció

---

## JSON Schema

```json
{
  "$schema": "https://json-schema.org/draft/2019-09/schema",
  "title": "Employees",
  "type": "array",
  "items": [
    {
      "title": "EmployeeDto",
      "type": "object",
      "required": ["name", "id"],
      "properties": {
        "id": {
          "type": "integer",
          "description": "id of the employee",
          "format": "int64",
          "example": 12
        },
        "name": {
          "type": "string",
          "description": "name of the employee",
          "example": "John Doe"
}}}]}
```
---

## Függőség

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <scope>test</scope>
</dependency>
```

---

## JSON Schema validáció

```java
when()
    .get("/api/employees")
    .then()
    .body(matchesJsonSchemaInClasspath("employee-dto-schema.json"));
```

---

class: inverse, center, middle

# Saját validáció készítése

---

## Saját annotáció

```java
@Constraint(validatedBy = NameValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
public @interface Name {

    String message() default "Invalid name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int maxLength() default 50;

}
```

---

## Validator osztály

```java
public class NameValidator implements ConstraintValidator<Name, String> {

  private int maxLength;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value != null &&
      !value.isBlank() &&
      value.length() > 2 && 
      value.length() <= maxLength && 
      Character.isUpperCase(value.charAt(0));
  }

  @Override
  public void initialize(Name constraintAnnotation) {
      maxLength = constraintAnnotation.maxLength();
  }
}
```
---

class: inverse, center, middle

# Spring Boot konfiguráció

---

## Externalized Configuration

* Konfiguráció alkalmazáson kívül szervezése, <br /> hogy ugyanazon alkalmazás több környezetben is tudjon futni
* Spring `Environment` absztrakcióra épül, `PropertySource` implementációk <br />hierarchiája,
  melyek különböző helyekről töltenek be property-ket
* Majdnem húsz forrása a property-knek<br />(a magasabb prioritásúak felülírják a később szereplőket)
* Leggyakoribb az `application.properties` fájl
* YAML formátum is használható

---

## Források

* Az elöl szereplők felülírják a később szereplőket
* Legfontosabbak:
  * Parancssori paraméterek
  * Operációs rendszer környezeti változók
  * `application.properties` állomány a jar fájlon kívül <br />(`/config` könyvtár, vagy közvetlenül a jar mellett)
  * `application.properties` állomány a jar fájlon belül

---

## Konfiguráció beolvasása @Value annotációval

```java
@Service
public class HelloService {

  private String hello;

  public HelloService(@Value("${employees.hello}") String hello) {
    this.hello = hello;
  }

  public String sayHello() {
    return hello + " " + LocalDateTime.now();
  }
}
```

---

## application.properties tartalma

```properties
employees.hello = Hello Spring Boot Config
```

---

## ConfigurationProperties

* Több, esetleg hierarchikus property-k esetén


```java
@ConfigurationProperties(prefix = "employees")
@Data
public class HelloProperties {

    private String hello;
}
```

* Regisztrálni kell a
`@EnableConfigurationProperties(HelloProperties.class)` annotációval, pl. a service-en
* Használat helyén injektálható

---

## További ConfigurationProperties lehetőségek

* Setteren keresztül, de használható a `@ConstructorBinding`, ekkor konstruktoron keresztül
* Relaxed binding: nem kell pontos egyezőség
* Használható a `@Validated` Spring annotáció, <br />(majd használható a Bean Validation)
* A property-ket definiálni lehet külön állományban, <br />ekkor felismeri az IDE

```
META-INF/additional-spring-configuration-metadata.json
```

---

## Előre definiált property-k

* Százas nagyságrendben
* Spring Boot Reference Documentation: <br />[Appendix A: Common Application properties](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#common-application-properties)

---

## Property-k titkosítása

* Pl. [Jasypt használatával](https://www.baeldung.com/spring-boot-jasypt)

---

## Port

* `server.port`

---

## 12Factor: Port binding

* Konfigurálható legyen a port, ahol elindul
* Két alkalmazás ne legyen telepítve ugyanarra a web konténerre, <br /> alkalmazásszerverre

---

## Konfiguráció Dockerrel

```shell
docker run -d -p 8080:8081 -e SERVER_PORT=8081 -e EMPLOYEES_HELLO=HelloDocker employees
```

---

## 12Factor: Configuration

* Környezetenként eltérő értékek
* Pl. backing service-ek elérhetőségei
* Ide tartoznak a jelszavak, titkos kulcsok, <br />melyeket különös figyelemmel kell kezelni
* Konfigurációs paraméterek a környezet részét képezzék, <br />és ne az alkalmazás részét
* Konfigurációs paraméterek környezeti változókból jöjjenek
* Kerüljük az alkalmazásban a környezetek nevesítését
* Nem kerülhetnek a kód mellé a verziókezelőbe <br />(csak a fejlesztőkörnyezet default beállításai)
* Verziókezelve legyen, ki, mikor mit módosított
* Lásd még Spring Cloud Config

---

class: inverse, center, middle

# Spring Boot naplózás

---

## Naplózás

* Spring belül a Commons Loggingot használja
* Előre be van konfigurálva a Java Util Logging, Log4J2, és Logback
* Alapesetben konzolra ír
* Naplózás szintje, és fájlba írás is állítható <br />az `application.properties` állományban

---

## Best practice

* SLF4J használata
* Lombok használata
* Paraméterezett üzenet

```java
private static final org.slf4j.Logger log =
  org.slf4j.LoggerFactory.getLogger(LogExample.class);
```

```java
@Slf4j
```

```java
log.info("Employee has been created");
log.debug("Employee has been created with name {}",
  employee.getName());
```

---

## Konfiguráció

* `application.properties`: szint, fájl
* Használható logger library specifikus konfigurációs fájl (pl. `logback.xml`)

```properties
logging.level.training = debug
```

---

## 12Factor hivatkozás: Naplózás

* Time ordered event stream
* Nem az alkalmazás feladata a napló irányítása a megfelelő helyre, <br />vagy a napló tárolása, kezelése, archiválása, görgetése, stb.
* Írjon konzolra
* Központi szolgáltatás: pl. ELK, Splunk, hiszen <br />az alkalmazás node-ok bármikor eltűnhetnek


---

class: inverse, center, middle

# PostgreSQL

---

## PostgreSQL indítása Dockerrel

```shell
docker run
  -d
  -e POSTGRES_PASSWORD=password 
  -p 5432:5432 
  --name employees-postgres 
  postgres
```

---

## Driver

`pom.xml`-be:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

---

## Inicializálás

* `application.properties` konfiguráció

```properties
spring.datasource.url=jdbc:postgresql:postgres
spring.datasource.username=postgres
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=create-drop
```

---

class: inverse, center, middle

# Integrációs tesztelés

---

## JPA repository tesztelése

* JPA repository-k tesztelésére
* `@DataJpaTest` annotáció, csak a repository réteget indítja el
    * Embedded adatbázis
    * Tesztbe injektálható: JPA repository,  `DataSource`, `JdbcTemplate`, <br /> `EntityManager`
* Minden teszt metódus saját tranzakcióban, végén rollback
* Service réteg már nem kerül elindításra
* Tesztelni:
    * Entitáson lévő annotációkat
    * Névkonvenció alapján generált metódusokat
    * Custom query

---

## DataJpaTest

```java
@DataJpaTest
public class EmployeesRepositoryIT {

  @Autowired
  EmployeesRepository employeesRepository;

  @Test
  void testPersist() {
    Employee employee = new Employee("John Doe");
    employeesRepository.save(employee);
    List<Employee> employees =
      employeesRepository.findAllByPrefix("%");
    assertThat(employees)
      .extracting(Employee::getName)
      .containsExactly("John Doe");
  }

}
```

---

## @SpringBootTest használata

* Teljes alkalmazás tesztelése
* Valós adatbázis szükséges hozzá, gondoskodni kell az elindításáról
* Séma inicializáció és adatfeltöltés szükséges

---

## Tesztek H2 adatbázisban

* `src\test\resources\application.properties` fájlban <br /> a teszteléshez használt DataSource

```properties
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=sa
```

---

## Séma inicializáció

* `spring.jpa.hibernate.ddl-auto` `create-drop` alapesetben, <br /> teszt lefutása végén eldobja a sémát
    * `create`-re állítva megmaradnak a táblák és adatok
* Ha van `schema.sql` a classpath-on, azt futtatja le
* Flyway vagy Liquibase használata

---

## Adatfeltöltés

* `data.sql` a classpath-on
* `@Sql` annotáció használata a teszten
* Programozott módon
    * Teszt osztályban `@BeforeEach` vagy `@AfterEach` <br /> annotációkkal megjelölt metódusokban
    * Publikus API-n keresztül
    * Injektált controller, service, repository, stb. használatával
    * Közvetlen hozzáférés az adatbázishoz <br /> (pl. `JdbcTemplate`)

---

## Tesztek egymásra hatása

* Csak külön adatokon dolgozunk - nehéz lehet a kivitelezése
* Teszteset maga előtt vagy után rendet tesz
* Állapot
    * Teljes séma törlése, séma inicializáció
    * Adatbázis import
    * Csak (bizonyos) táblák ürítése

---

class: inverse, center, middle

# Alkalmazás futtatása Dockerben MariaDB-vel

---

## Architektúra

![Alkalmazás futtatása Dockerben](images/docker-mysql-arch.png)

---

## Hálózat létrehozása

```shell
docker network ls
docker network create --driver bridge employees-net
docker network inspect employees-net
```

---

## Alkalmazás futtatása Dockerben

```shell
docker run
    -d  
*    -e SPRING_DATASOURCE_URL=jdbc:mariadb://employees-mariadb/employees
*    -e SPRING_DATASOURCE_USERNAME=employees
*    -e SPRING_DATASOURCE_PASSWORD=employees
    -p 8080:8080
*    --network employees-net
    --name employees
    employees
```

---

class: inverse, center, middle

# Alkalmazás futtatása Dockerben MariaDB-vel Fabric8 Docker Maven Pluginnel

---

## Adatbázis

```xml
<image>
  <name>mariadb</name>
  <alias>employees-mariadb</alias>
  <run>
  <env>
  <MYSQL_DATABASE>employees</MYSQL_DATABASE>
  <MYSQL_USER>employees</MYSQL_USER>
  <MYSQL_PASSWORD>employees</MYSQL_PASSWORD>
  <MYSQL_ALLOW_EMPTY_PASSWORD>yes</MYSQL_ALLOW_EMPTY_PASSWORD>
  </env>
  <ports>3306:3306</ports>
  </run>
</image>
```

---

## Wait

```
FROM adoptopenjdk:14-jre-hotspot
RUN  apt update \
     && apt-get install wget \
     && apt-get install -y netcat \
     && wget https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh \
     && chmod +x ./wait-for-it.sh
RUN mkdir /opt/app
ADD maven/${project.artifactId}-${project.version}.jar /opt/app/employees.jar
CMD ["./wait-for-it.sh", "-t", "180", "employees-mariadb:3306", "--", "java", "-jar", "/opt/app/employees.jar"]
```

---

## Alkalmazás

```xml
<image>

  <!--- ... -->

  <run>
  <env>
  <SPRING_DATASOURCE_URL>jdbc:mariadb://employees-mariadb/employees</SPRING_DATASOURCE_URL>
  </env>
  <ports>8080:8080</ports>
  <links>
  <link>employees-mariadb:employees-mariadb</link>
  </links>
  <dependsOn>
  <container>employees-mariadb</container>
  </dependsOn>
  </run>
</image>
```

---

class: inverse, center, middle

# Teljes alkalmazás futtatása docker compose-zal

---

## docker-compose.yml

```yaml
version: '3'

services:
  employees-mariadb:
    image: mariadb
    restart: always
    ports:
      - '3306:3306'
    environment:
      MYSQL_DATABASE: employees
      MYSQL_ALLOW_EMPTY_PASSWORD: 'yes' # aposztrófok nélkül boolean true-ként értelmezi
      MYSQL_USER: employees
      MYSQL_PASSWORD: employees

```

---

## docker-compose.yml folytatás

```yaml
  employees-app:
    image: employees
    ports:
      - "8080:8080"
    restart: always
    depends_on:
      - employees-mariadb
    environment:
      SPRING_DATASOURCE_URL: 'jdbc:mariadb://employees-mariadb:3306/employees'
    command: ["./wait-for-it.sh", "-t", "120", "employees-mariadb:3306", "--", "java", "-jar", "/opt/app/employees.jar"]
```

---

class: inverse, center, middle

# Integrációs tesztelés adatbázis előkészítéssel

---

## pom.xml

```xml
<profile>
  <id>startdb</id>
  <properties>
  <docker.filter>mariadb</docker.filter>
  </properties>
  <build>
  <plugins>
  <plugin>
  <groupId>io.fabric8</groupId>
  <artifactId>docker-maven-plugin</artifactId>
  <executions>
  <execution>
  <id>start</id>
  <phase>pre-integration-test</phase>
  <goals>
  <goal>start</goal>
  </goals>
  </execution>
  <execution>
  <id>stop</id>
  <phase>post-integration-test</phase>
  <goals>
  <goal>stop</goal>
  </goals>
  </execution>
  </executions>
  </plugin>
  </plugins>
  </build>
</profile>
```

---

class: inverse, center, middle

# Naplózás

---

## Naplózás lekérdezése és beállítása

* `/loggers`
* `/logfile`

```plaintext
### Get logger
GET http://localhost:8080/actuator/loggers/training.employees

### Set logger
POST http://localhost:8080/actuator/loggers/training.employees
Content-Type: application/json

{
  "configuredLevel": "INFO"
}
```

---

class: inverse, center, middle

# Metrics

---

## Metrics

* `/metrics` végponton
* [Micrometer](https://micrometer.io/) - application metrics facade (mint az SLF4J a naplózáshoz)
* Több, mint 15 monitoring eszközhöz való csatlakozás <br /> (Elastic, Ganglia, Graphite, New Relic, Prometheus, stb.)

---

## Gyűjtött értékek

* JVM
    * Memória
    * GC
    * Szálak
    * Betöltött osztályok
* CPU
* File descriptors
* Uptime
* Tomcat (`server.tomcat.mbeanregistry.enabled` <br /> értéke legyen `true`)
* Library-k: Spring MVC, WebFlux, Jersey, HTTP Client, <br /> Cache, DataSource, Hibernate, RabbitMQ
* Stb.

---

## Saját metrics

```java
Counter.builder(EMPLOYEES_CREATED_COUNTER_NAME)
        .baseUnit("employees")
        .description("Number of created employees")
        .register(meterRegistry);

meterRegistry.counter(EMPLOYEES_CREATED_COUNTER_NAME).increment();
```

A `/metrics/employees.created` címen elérhető

---

## 12Factor hivatkozás: Telemetry

* Adatok különböző kategóriákba sorolhatóak:
  * Application performance monitoring
  * Domain specifikus értékek
  * Health, logs
* Új konténerek születnek és szűnnek meg
* Központi eszköz
