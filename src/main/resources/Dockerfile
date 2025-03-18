# Configurações do servidor
server.port=8080
server.servlet.context-path=/
spring.application.name=image-processor

# Configurações de base de dados
spring.datasource.url=jdbc:h2:mem:imageprocessordb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Configurações H2 Console (apenas para desenvolvimento)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false

# Configurações JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Configurações de upload de arquivos
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=12MB
app.upload.dir=uploads

# Configurações JWT
app.jwt.secret=chave_secreta_muito_segura_que_deve_ser_grande_o_suficiente_para_hmacsha256
app.jwt.expiration-ms=86400000
app.jwt.issuer=image-processor-app

# Configurações RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Configurações de e-mail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=sua_conta@gmail.com
spring.mail.password=sua_senha_de_aplicativo
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Configurações da aplicação
app.base-url=http://localhost:8080

# Configurações OpenAPI/Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method