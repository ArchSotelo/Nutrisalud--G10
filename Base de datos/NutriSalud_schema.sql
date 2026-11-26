
CREATE DATABASE IF NOT EXISTS nutrisalud CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE nutrisalud;
SET NAMES utf8mb4;
SET SESSION time_zone = '+00:00';

CREATE TABLE empresa (
 id_empresa BIGINT NOT NULL AUTO_INCREMENT,
 razon_social VARCHAR(150) NOT NULL,
 ruc CHAR(11) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 sector VARCHAR(100) NULL,
 direccion VARCHAR(250) NULL,
 telefono VARCHAR(25) NULL,
 estado BOOLEAN NOT NULL DEFAULT TRUE,
 PRIMARY KEY (id_empresa),
 UNIQUE KEY uq_empresa_ruc (ruc),
 CONSTRAINT ck_empresa_razon CHECK (CHAR_LENGTH(TRIM(razon_social)) > 0),
 CONSTRAINT ck_empresa_ruc CHECK (CHAR_LENGTH(ruc)=11 AND ASCII(SUBSTRING(ruc,1,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,2,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,3,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,4,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,5,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,6,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,7,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,8,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,9,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,10,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(ruc,11,1)) BETWEEN 48 AND 57),
 CONSTRAINT ck_empresa_estado CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE usuario (
 id_usuario BIGINT NOT NULL AUTO_INCREMENT,
 nombres VARCHAR(100) NOT NULL,
 apellidos VARCHAR(100) NOT NULL,
 correo VARCHAR(254) NOT NULL,
 username VARCHAR(80) NULL COMMENT 'Alias opcional conservado de V3; acceso principal por correo',
 contrasena_hash VARCHAR(255) NOT NULL,
 rol VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL,
 estado BOOLEAN NOT NULL DEFAULT TRUE,
 PRIMARY KEY (id_usuario),
 UNIQUE KEY uq_usuario_correo (correo),
 UNIQUE KEY uq_usuario_username (username),
 CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN','MEDICO')),
 CONSTRAINT ck_usuario_estado CHECK (estado IN (0,1)),
 CONSTRAINT ck_usuario_datos CHECK (CHAR_LENGTH(TRIM(nombres))>0 AND CHAR_LENGTH(TRIM(apellidos))>0 AND CHAR_LENGTH(TRIM(correo))>0 AND CHAR_LENGTH(TRIM(contrasena_hash))>0)
) ENGINE=InnoDB;

CREATE TABLE trabajador (
 id_trabajador BIGINT NOT NULL AUTO_INCREMENT,
 id_empresa BIGINT NOT NULL,
 tipo_documento VARCHAR(15) COLLATE utf8mb4_0900_as_cs NOT NULL DEFAULT 'DNI',
 nro_documento VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 nombres VARCHAR(100) NOT NULL,
 apellidos VARCHAR(100) NOT NULL,
 correo VARCHAR(254) NOT NULL,
 fecha_nacimiento DATE NULL,
 sexo VARCHAR(30) NULL,
 cargo VARCHAR(100) NULL,
 estado BOOLEAN NOT NULL DEFAULT TRUE,
 correo_verificado BOOLEAN NOT NULL DEFAULT FALSE,
 envio_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
 PRIMARY KEY (id_trabajador),
 UNIQUE KEY uq_trabajador_documento (tipo_documento,nro_documento),
 KEY ix_trabajador_empresa (id_empresa),
 KEY ix_trabajador_apellidos (apellidos),
 CONSTRAINT fk_trabajador_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa),
 CONSTRAINT ck_trabajador_tipo CHECK (tipo_documento IN ('DNI','CE','PASAPORTE')),
 CONSTRAINT ck_trabajador_doc CHECK (CHAR_LENGTH(TRIM(nro_documento))>0 AND (tipo_documento <> 'DNI' OR (CHAR_LENGTH(nro_documento)=8 AND ASCII(SUBSTRING(nro_documento,1,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,2,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,3,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,4,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,5,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,6,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,7,1)) BETWEEN 48 AND 57 AND ASCII(SUBSTRING(nro_documento,8,1)) BETWEEN 48 AND 57))),
 CONSTRAINT ck_trabajador_datos CHECK (CHAR_LENGTH(TRIM(nombres))>0 AND CHAR_LENGTH(TRIM(apellidos))>0 AND CHAR_LENGTH(TRIM(correo))>0),
 CONSTRAINT ck_trabajador_flags CHECK (estado IN (0,1) AND correo_verificado IN (0,1) AND envio_autorizado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE evaluacion_imc (
 id_imc BIGINT NOT NULL AUTO_INCREMENT,
 id_trabajador BIGINT NOT NULL,
 id_usuario BIGINT NOT NULL,
 fecha_evaluacion DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
 peso_kg DECIMAL(5,2) NOT NULL,
 talla_m DECIMAL(3,2) NOT NULL,
 imc DECIMAL(7,2) NOT NULL,
 clasificacion VARCHAR(20) COLLATE utf8mb4_0900_as_cs NOT NULL,
 observaciones VARCHAR(1000) NULL,
 PRIMARY KEY (id_imc),
 UNIQUE KEY uq_imc_trabajador (id_imc,id_trabajador),
 KEY ix_imc_trabajador_fecha (id_trabajador,fecha_evaluacion,id_imc),
 KEY ix_imc_usuario (id_usuario),
 KEY ix_imc_fecha (fecha_evaluacion),
 CONSTRAINT fk_imc_trabajador FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador),
 CONSTRAINT fk_imc_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
 CONSTRAINT ck_imc_peso CHECK (peso_kg BETWEEN 30 AND 300),
 CONSTRAINT ck_imc_talla CHECK (talla_m BETWEEN 1 AND 2.50),
 CONSTRAINT ck_imc_calculo CHECK (imc=ROUND(CAST(peso_kg AS DECIMAL(15,8))/NULLIF(talla_m*talla_m,0),2)),
 CONSTRAINT ck_imc_clasificacion CHECK ((imc<18.50 AND clasificacion='BAJO_PESO') OR (imc>=18.50 AND imc<25 AND clasificacion='NORMAL') OR (imc>=25 AND imc<30 AND clasificacion='SOBREPESO') OR (imc>=30 AND clasificacion='OBESIDAD'))
) ENGINE=InnoDB;

CREATE TABLE evaluacion_salto_vertical (
 id_salto BIGINT NOT NULL AUTO_INCREMENT,
 id_trabajador BIGINT NOT NULL,
 id_usuario BIGINT NOT NULL,
 fecha_evaluacion DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
 masa_kg DECIMAL(5,2) NOT NULL,
 alcance_pie_cm DECIMAL(5,2) NOT NULL,
 alcance_salto_cm DECIMAL(5,2) NOT NULL,
 altura_salto_cm DECIMAL(5,2) NOT NULL,
 indicador_rendimiento DECIMAL(9,2) NOT NULL COMMENT 'Potencia pico estimada en W; Sayers, propuesta por validar',
 observaciones VARCHAR(1000) NULL,
 PRIMARY KEY (id_salto),
 UNIQUE KEY uq_salto_trabajador (id_salto,id_trabajador),
 KEY ix_salto_trabajador_fecha (id_trabajador,fecha_evaluacion,id_salto),
 KEY ix_salto_usuario (id_usuario),
 KEY ix_salto_fecha (fecha_evaluacion),
 CONSTRAINT fk_salto_trabajador FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador),
 CONSTRAINT fk_salto_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
 CONSTRAINT ck_salto_masa CHECK (masa_kg BETWEEN 30 AND 300),
 CONSTRAINT ck_salto_alcance CHECK (alcance_pie_cm>0 AND alcance_pie_cm<=350 AND alcance_salto_cm>alcance_pie_cm AND alcance_salto_cm<=350),
 CONSTRAINT ck_salto_altura CHECK (altura_salto_cm>0 AND altura_salto_cm<=150 AND altura_salto_cm=alcance_salto_cm-alcance_pie_cm),
 CONSTRAINT ck_salto_indicador CHECK (indicador_rendimiento>0 AND indicador_rendimiento=ROUND(60.7*altura_salto_cm+45.3*masa_kg-2055,2))
) ENGINE=InnoDB;

CREATE TABLE notificacion_correo (
 id_notificacion BIGINT NOT NULL AUTO_INCREMENT,
 id_trabajador BIGINT NOT NULL,
 tipo_evaluacion VARCHAR(15) COLLATE utf8mb4_0900_as_cs NOT NULL,
 id_imc BIGINT NULL,
 id_salto BIGINT NULL,
 fecha_creacion DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
 fecha_envio DATETIME(6) NULL,
 destinatario VARCHAR(254) NOT NULL,
 asunto VARCHAR(200) NOT NULL,
 estado_envio VARCHAR(24) COLLATE utf8mb4_0900_as_cs NOT NULL DEFAULT 'PENDIENTE',
 intentos INT NOT NULL DEFAULT 0,
 ultimo_error_sanitizado VARCHAR(255) NULL,
 PRIMARY KEY (id_notificacion),
 UNIQUE KEY uq_notificacion_imc (id_imc),
 UNIQUE KEY uq_notificacion_salto (id_salto),
 KEY ix_notificacion_imc_trabajador (id_imc,id_trabajador),
 KEY ix_notificacion_salto_trabajador (id_salto,id_trabajador),
 KEY ix_notificacion_trabajador (id_trabajador),
 KEY ix_notificacion_estado_fecha (estado_envio,fecha_creacion),
 CONSTRAINT fk_notificacion_trabajador FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador),
 CONSTRAINT fk_notificacion_imc FOREIGN KEY (id_imc,id_trabajador) REFERENCES evaluacion_imc(id_imc,id_trabajador),
 CONSTRAINT fk_notificacion_salto FOREIGN KEY (id_salto,id_trabajador) REFERENCES evaluacion_salto_vertical(id_salto,id_trabajador),
 CONSTRAINT ck_notificacion_tipo CHECK ((tipo_evaluacion='IMC' AND id_imc IS NOT NULL AND id_salto IS NULL) OR (tipo_evaluacion='SALTO_VERTICAL' AND id_salto IS NOT NULL AND id_imc IS NULL)),
 CONSTRAINT ck_notificacion_estado CHECK (estado_envio IN ('PENDIENTE','ENVIADO','FALLIDO','OMITIDO','POR_VERIFICAR')),
 CONSTRAINT ck_notificacion_intentos CHECK (intentos>=0),
 CONSTRAINT ck_notificacion_fecha CHECK ((estado_envio='ENVIADO' AND fecha_envio IS NOT NULL AND intentos>0) OR (estado_envio<>'ENVIADO' AND fecha_envio IS NULL)),
 CONSTRAINT ck_notificacion_datos CHECK (CHAR_LENGTH(TRIM(destinatario))>0 AND CHAR_LENGTH(TRIM(asunto))>0)
) ENGINE=InnoDB;

CREATE TABLE auditoria (
 id_auditoria BIGINT NOT NULL AUTO_INCREMENT,
 id_usuario BIGINT NOT NULL,
 evento VARCHAR(60) NOT NULL,
 tipo_recurso VARCHAR(60) NOT NULL,
 recurso_id BIGINT NULL,
 fecha DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
 resultado VARCHAR(20) NOT NULL,
 PRIMARY KEY (id_auditoria),
 KEY ix_auditoria_usuario_fecha (id_usuario,fecha),
 KEY ix_auditoria_recurso (tipo_recurso,recurso_id),
 CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

SHOW TABLES;
