-- Requires the tipos_cliente catalog from database.sql.
-- Adds 50 deterministic demo customers without duplicating existing documents.
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo)
WITH RECURSIVE sequence_numbers (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM sequence_numbers WHERE n < 50
)
SELECT CONCAT('Demo Customer ', LPAD(sequence_numbers.n, 2, '0')),
       CONCAT('FT-DEMO-', LPAD(sequence_numbers.n, 4, '0')),
       CONCAT('customer', LPAD(sequence_numbers.n, 2, '0'), '@example.test'),
       CONCAT('300', LPAD(sequence_numbers.n, 7, '0')),
       CONCAT('Demo address ', sequence_numbers.n),
       IF(MOD(sequence_numbers.n, 2) = 0, 1, 2)
FROM sequence_numbers
WHERE NOT EXISTS (
    SELECT 1 FROM clientes existing
    WHERE existing.documento = CONCAT('FT-DEMO-', LPAD(sequence_numbers.n, 4, '0'))
);
