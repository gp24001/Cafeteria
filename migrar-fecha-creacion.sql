-- Ejecutar una sola vez sobre cafeteria antes de desplegar el mapeo TIMESTAMP.
-- La columna anterior solo guardaba la hora. Por decisión del usuario, las
-- asignaciones anteriores se fechan el 27/10/2026 conservando su hora local.
-- Esta fecha es una regularización, no una fecha histórica recuperada.
BEGIN;
SET LOCAL TIME ZONE 'America/El_Salvador';
ALTER TABLE public.producto_tipo_producto
    ALTER COLUMN fecha_creacion TYPE timestamp without time zone
    USING (DATE '2026-10-27' + (fecha_creacion AT TIME ZONE 'America/El_Salvador')::time);
COMMIT;
