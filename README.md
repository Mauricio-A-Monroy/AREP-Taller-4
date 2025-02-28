# MicroSpring Framework

## Descripción
MicroSpring es un micro-framework ligero en Java que permite definir controladores REST de manera sencilla usando anotaciones similares a Spring Boot.

## Características
- Servidor HTTP integrado.
- Manejo de rutas mediante anotaciones `@GetMapping` y `@RestController`.
- Servir archivos estáticos.
- Descubrimiento automático de clases controladoras.

## Tecnologías Utilizadas
- **Java 17**
- **Spring Boot 3**
- **Maven**
- **Docker**
- **AWS EC2**
- **DockerHub**

---

## Endpoints Disponibles

- /
- /index.html
- /webApp/App.html
- /app/pi
- /app/e
- /app/greeting
- /app/greeting?name=alejo
- /app/count

## Uso
```java
@RestController
public class MyController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
```

## Diseño de Clases

### **1. Clase Principal**
- **`MicroServer`**  
  - Punto de entrada del servidor.  
  - Escanea y registra controladores anotados con `@RestController`.  
  - Mapea métodos anotados con `@GetMapping` a rutas HTTP.  
  - Configura archivos estáticos y arranca el servidor con `HttpServer.start()`.  

### **2. Módulo HTTP (`http`)**
- **`HttpServer`**  
  - Servidor HTTP básico.  
  - Gestiona rutas registradas mediante `get()`.  
  - Procesa peticiones y respuestas.  

- **`HttpResponse`**  
  - Representa una respuesta HTTP con código de estado, encabezados y cuerpo.  

- **`HttpRequest`**  
  - Representa una petición HTTP con método, ruta y encabezados.  

### **3. Controladores y Anotaciones (`annotation`)**
- **`@RestController`**  
  - Indica que una clase es un controlador.  

- **`@GetMapping`**  
  - Indica que un método maneja solicitudes HTTP GET en una ruta específica.  

### **4. Capa de Controladores (`controller`)**
- **Clases de usuario que extienden `@RestController`**  
  - Definen métodos con `@GetMapping` que responden a solicitudes.  

## Instalación y Configuración

### 1. Clonar el Repositorio
```sh
    git clone https://github.com/Mauricio-A-Monroy/AREP-Taller-4.git
```

### 2. Construir la Aplicación
```sh
    mvn clean package
```
Esto generará un archivo `JAR` en la carpeta `target/`.

---

## Creación del Contenedor Docker

### 1. Crear el Dockerfile
Asegúrate de tener un `Dockerfile` en la raíz del proyecto con el siguiente contenido:
```dockerfile
FROM openjdk:21

WORKDIR /usrapp/bin

ENV PORT=35000

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency
COPY src/main/resources/static /usrapp/bin/resources/static

CMD ["java","-cp","./classes:./dependency/*","edu.escuelaing.arep.microspring.MicroServer"]
```

### 2. Construir la Imagen Docker
```sh
    docker build --tag dockersparkprimer .
```

### 3. Construir contenedor en la imagen creada anteriormente
```sh
    docker run -d -p 34000:35000 --name firstdockercontainer  -e DOCKER_ENV=true dockersparkprimer
```

### 4. Probar el servidro ingresando a a alguno de los endpoints disponibles

### 5. Ejecutar el docker compose:
```sh
    docker-compose up -d
```
---

## Publicación en DockerHub

### 1. Referenciar imagen de un repositorio
En tu motor de docker local cree una referencia a su imagen con el nombre del repositorio a donde desea subirla:
docker tag dockersparkprimer dnielben/firstsprkwebapprepo
```sh
    docker tag dockersparkprimer tu-usuario/nombre-repositorio
```

### 2. Iniciar Sesión en DockerHub
```sh
    docker login -u tu-usuario
```

### 3. Subir la Imagen a DockerHub
Este comando funciona en Windows
```sh
    docker push tu-usuario/nombre-repositorio latest
```

## Despliegue en AWS EC2 con Docker

### 1. Creación de la instancia EC2

- Se creó una instancia EC2 en AWS.
- Se generó una clave RSA en formato `.pem`.
- Se guardó la clave en un archivo en el computador local.

### 2. Conexión a la instancia EC2

Desde la consola, se navegó hasta la ubicación del archivo `.pem` y se ejecutaron los siguientes comandos:

```sh
chmod 400 "clave.pem"
ssh -i "clave.pem" usuario@direccion-ec2.compute.amazonaws.com
```

### 3. Instalación y configuración de Docker en EC2

Dentro de la instancia EC2, se ejecutaron los siguientes comandos:

```sh
sudo yum update -y
sudo yum install docker
sudo service docker start
sudo usermod -a -G docker ec2-user
exit
```

Luego, se volvió a conectar con SSH.

### 4. Ejecución del contenedor Docker

Una vez dentro de la instancia, se ejecutó el siguiente comando para correr el contenedor:

```sh
docker run -d -p 42000:35000 --name firstdockercontainer -e DOCKER_ENV=true imagen-docker:latest
```

Con esto, el servicio quedó desplegado en AWS EC2 utilizando Docker y se pueden probar los endpoints listados anteriormente.

Prueba del despliegue: https://www.youtube.com/watch?v=qpNjInHXPzc


## Conclusión
Has desplegado exitosamente tu aplicación en AWS usando Docker. Para actualizaciones, simplemente sube una nueva versión a DockerHub y reinicia el contenedor en AWS.


