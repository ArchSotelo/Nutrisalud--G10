
USE nutrisalud;
SET SESSION time_zone = '+00:00';
START TRANSACTION;
INSERT INTO empresa (razon_social,ruc,sector) VALUES ('Empresa Alfa DEMO','00000000001','Servicios');
SET @alfa=LAST_INSERT_ID();
INSERT INTO empresa (razon_social,ruc,sector) VALUES ('Empresa Beta DEMO','00000000002','Comercio');
SET @beta=LAST_INSERT_ID();
INSERT INTO usuario (nombres,apellidos,correo,username,contrasena_hash,rol,estado) VALUES ('Medico','Demo','medico@example.test','medico_demo_inactivo','!DEMO_INACTIVO_NO_AUTENTICABLE!','MEDICO',FALSE);
SET @medico=LAST_INSERT_ID();
INSERT INTO trabajador (id_empresa,nro_documento,nombres,apellidos,correo,cargo) VALUES (@alfa,'12345678','Ana','Demo','ana@example.test','Operaciones');
SET @ana=LAST_INSERT_ID();
INSERT INTO trabajador (id_empresa,nro_documento,nombres,apellidos,correo,cargo) VALUES (@beta,'87654321','Luis','Ejemplo','luis@example.test','Administración');
SET @luis=LAST_INSERT_ID();
INSERT INTO evaluacion_imc (id_trabajador,id_usuario,fecha_evaluacion,peso_kg,talla_m,imc,clasificacion) VALUES (@ana,@medico,'2026-11-01 15:00:00',72,1.75,23.51,'NORMAL');
INSERT INTO evaluacion_imc (id_trabajador,id_usuario,fecha_evaluacion,peso_kg,talla_m,imc,clasificacion) VALUES (@ana,@medico,'2026-11-20 15:00:00',70,1.75,22.86,'NORMAL');
SET @imc=LAST_INSERT_ID();
INSERT INTO evaluacion_imc (id_trabajador,id_usuario,fecha_evaluacion,peso_kg,talla_m,imc,clasificacion) VALUES (@luis,@medico,'2026-11-20 16:00:00',95,1.70,32.87,'OBESIDAD');
INSERT INTO evaluacion_salto_vertical (id_trabajador,id_usuario,fecha_evaluacion,masa_kg,alcance_pie_cm,alcance_salto_cm,altura_salto_cm,indicador_rendimiento) VALUES (@ana,@medico,'2026-11-21 15:00:00',70,200,240,40,3544);
SET @salto=LAST_INSERT_ID();
-- Sin consentimiento o correo verificado: omitidas; no se envía nada.
INSERT INTO notificacion_correo (id_trabajador,tipo_evaluacion,id_imc,id_salto,destinatario,asunto,estado_envio) VALUES (@ana,'IMC',@imc,NULL,'ana@example.test','Resultado de evaluación','OMITIDO'),(@ana,'SALTO_VERTICAL',NULL,@salto,'ana@example.test','Resultado de evaluación','OMITIDO');
INSERT INTO auditoria (id_usuario,evento,tipo_recurso,recurso_id,resultado) VALUES (@medico,'CREACION_DEMO','EVALUACION_IMC',@imc,'OK');
COMMIT;
SELECT id_imc,peso_kg,talla_m,imc,clasificacion FROM evaluacion_imc;
SELECT id_salto,altura_salto_cm,indicador_rendimiento FROM evaluacion_salto_vertical;
SELECT id_notificacion,tipo_evaluacion,estado_envio FROM notificacion_correo;
