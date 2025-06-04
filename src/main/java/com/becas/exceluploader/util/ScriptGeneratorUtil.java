package com.becas.exceluploader.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
// Removed unnecessary import of java.lang.String

import java.util.ArrayList;
import java.util.List;

public class ScriptGeneratorUtil {

    public static List<String> generarScriptsDesdeFila(Row row) {
        List<String> scripts = new ArrayList<>();
        String cedula = getCellString(row.getCell(0));
        String nombres = getCellString(row.getCell(1));
        String nombrePrograma = getCellString(row.getCell(3));
        String numeroTramite = getCellString(row.getCell(4));

        String historialBecas = getCellString(row.getCell(5));
        String resultado = getCellString(row.getCell(6));
        String criterioTecnico = getCellString(row.getCell(7));
        String analistaResultado = getCellString(row.getCell(8));

        String nivelEstudio = getCellString(row.getCell(10));
        String areaEstudio = getCellString(row.getCell(11));
        String subareaEstudio = getCellString(row.getCell(12));
        String campoDetallado = getCellString(row.getCell(13));
        String carrera = getCellString(row.getCell(14));
        String universidad = getCellString(row.getCell(15));
        String pais = getCellString(row.getCell(16));
        String titulo = getCellString(row.getCell(17));
        String idioma = getCellString(row.getCell(18));

        String fechaInicioEstudios = getCellString(row.getCell(19));
        String fechaFinEstudios = getCellString(row.getCell(20));
        String duracionEstudios = getCellString(row.getCell(21));

        String fechaInicioFin = getCellString(row.getCell(22));
        String fechaFinFin = getCellString(row.getCell(23));
        String duracionFin = getCellString(row.getCell(24));

        String presupuesto = getCellString(row.getCell(25)).trim();
        if (presupuesto.isBlank())
            presupuesto = "0";

        String nombreRubro = getCellString(row.getCell(26)).trim();
        if (nombreRubro.isBlank())
            throw new RuntimeException("Falta el nombre del rubro");

        // SCRIPT 1: update solicitudes
        scripts.add("UPDATE solicitudes so SET catalogos_historial_becas_id=" + historialBecas +
                ", resultado='" + resultado + "', criterio_tecnico='" + criterioTecnico + "' " +
                "FROM solicitantes sl WHERE sl.numero_identificacion='" + cedula + "' AND so.numero_tramite='"
                + numeroTramite + "' AND sl.id=so.solicitantes_id;");

        // SCRIPT 2: update solicitudes_programas_requisitos
        scripts.add("UPDATE solicitudes_programas_requisitos spr SET resultado='" + analistaResultado + "' " +
                "FROM solicitudes so, solicitantes sl, programas_requisitos pr " +
                "WHERE so.id=spr.solicitudes_id AND sl.id=so.solicitantes_id " +
                "AND spr.programas_requisitos_id=pr.id AND pr.requisito_obligatorio=false " +
                "AND (sl.numero_identificacion='" + cedula + "' AND so.numero_tramite='" + numeroTramite + "');");

        // SCRIPT 3: update solicitudes_datos_estudio
        // validar si existe el programa

        // SCRIPT 3: UPSERT (Update or Insert) para solicitudes_datos_estudio
        scripts.add("DO $$ " +
                "BEGIN " +
                "IF EXISTS (SELECT 1 FROM solicitudes_datos_estudio sde " +
                "JOIN solicitudes so ON so.id = sde.solicitudes_id " +
                "JOIN solicitantes sl ON sl.id = so.solicitantes_id " +
                "JOIN programas p ON p.id = so.programas_id " +
                "JOIN programas_regiones pr ON pr.programas_id = p.id " +
                "JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id " +
                "WHERE sl.numero_identificacion = '" + cedula + "' AND so.numero_tramite = '" + numeroTramite
                + "') THEN " +

                "UPDATE solicitudes_datos_estudio sde SET " +
                "programas_regiones_niv_est_id = prne.id, " +
                "catalogos_nivel_estudio_id = " + nivelEstudio + ", " +
                "areas_estudio_id = " + campoDetallado + ", " +
                "carreras_id = " + carrera + ", " +
                "universidades_id = " + universidad + ", " +
                "ubicaciones_geograficas_id = " + pais + ", " +
                "catalogos_titulo_id = " + titulo + ", " +
                "catalogos_idioma_estudio_id = " + idioma + ", " +
                "fecha_inicio_estudios = '" + fechaInicioEstudios + "', " +
                "fecha_fin_estudios = '" + fechaFinEstudios + "', " +
                "duracion_estudios = '" + duracionEstudios + "' " +
                "FROM solicitudes so " +
                "JOIN solicitantes sl ON sl.id = so.solicitantes_id " +
                "JOIN programas p ON p.id = so.programas_id " +
                "JOIN programas_regiones pr ON pr.programas_id = p.id " +
                "JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id " +
                "WHERE sde.solicitudes_id = so.id " +
                "AND sl.numero_identificacion = '" + cedula + "' AND so.numero_tramite = '" + numeroTramite + "'; " +

                "ELSE " +

                "INSERT INTO solicitudes_datos_estudio (" +
                "solicitudes_id, programas_regiones_niv_est_id, catalogos_nivel_estudio_id, " +
                "areas_estudio_id, carreras_id, universidades_id, ubicaciones_geograficas_id, " +
                "catalogos_titulo_id, catalogos_idioma_estudio_id, fecha_inicio_estudios, " +
                "fecha_fin_estudios, duracion_estudios) " +
                "SELECT so.id, prne.id, " + nivelEstudio + ", " + campoDetallado + ", " +
                carrera + ", " + universidad + ", " + pais + ", " + titulo + ", " + idioma + ", '" +
                fechaInicioEstudios + "', '" + fechaFinEstudios + "', '" + duracionEstudios + "' " +
                "FROM solicitudes so " +
                "JOIN solicitantes sl ON sl.id = so.solicitantes_id " +
                "JOIN programas p ON p.id = so.programas_id " +
                "JOIN programas_regiones pr ON pr.programas_id = p.id " +
                "JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id " +
                "WHERE sl.numero_identificacion = '" + cedula + "' AND so.numero_tramite = '" + numeroTramite + "'; " +

                "END IF; " +
                "END $$ LANGUAGE plpgsql;");

        // SCRIPT 4: update solicitudes (fechas y presupuesto)
        scripts.add("UPDATE solicitudes so SET fecha_inicio_financiamiento='" + fechaInicioFin + "', " +
                "fecha_fin_financiamiento='" + fechaFinFin + "', duracion_financiamiento='" + duracionFin + "', " +
                "fecha_inicio_financiamiento_ac='" + fechaInicioFin + "', fecha_fin_financiamiento_ac='" + fechaFinFin
                + "', " +
                "duracion_financiamiento_ac='" + duracionFin + "', presupuesto_beca='" + presupuesto + "' " +
                "FROM solicitantes sl WHERE sl.numero_identificacion='" + cedula + "' " +
                "AND so.numero_tramite='" + numeroTramite + "' AND sl.id=so.solicitantes_id;");

        // SCRIPT 5: insert en solicitudes_rubros con validación

        scripts.add("DO $$ " +
                "DECLARE " +
                "    rubro_existe INTEGER; " +
                "BEGIN " +
                "    SELECT COUNT(*) INTO rubro_existe " +
                "    FROM solicitudes_rubros sr " +
                "    INNER JOIN solicitudes so ON sr.solicitudes_id = so.id " +
                "    INNER JOIN solicitantes sl ON sl.id = so.solicitantes_id " +
                "    INNER JOIN programas p ON p.id = so.programas_id " +
                "    INNER JOIN programas_regiones pr ON pr.programas_id = p.id " +
                "    INNER JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id " +
                "    INNER JOIN programas_reg_niv_est_rub prner ON prner.programas_regiones_niv_est_id = prne.id " +
                "    INNER JOIN rubros r ON r.id = prner.rubros_id " +
                "    WHERE sl.numero_identificacion = '" + cedula + "' " +
                "    AND so.numero_tramite = '" + numeroTramite + "' " +
                "    AND r.nombre = '" + nombreRubro + "'; " +
                "    IF rubro_existe > 0 THEN " +
                "        RAISE NOTICE 'El rubro ''" + nombreRubro + "'' ya existe para el solicitante ''" + cedula
                + "'' y no será actualizado.'; " +
                "    ELSE " +
                "        INSERT INTO solicitudes_rubros (solicitudes_id, catalogos_periodicidad_id, presupuesto_referencial, "
                +
                "            programas_reg_niv_est_rub_id, estado, valor_maximo_financiamiento, presupuesto_aprobado, presupuesto_proyectado) "
                +
                "        SELECT so.id, null, " + presupuesto + ", prner.id, true, " + presupuesto + ", null, null " +
                "        FROM solicitudes so " +
                "        INNER JOIN solicitantes sl ON sl.id = so.solicitantes_id " +
                "        INNER JOIN programas p ON p.id = so.programas_id " +
                "        INNER JOIN programas_regiones pr ON pr.programas_id = p.id " +
                "        INNER JOIN programas_regiones_niv_est prne ON prne.programas_regiones_id = pr.id " +
                "        INNER JOIN programas_reg_niv_est_rub prner ON prner.programas_regiones_niv_est_id = prne.id " +
                "        INNER JOIN rubros r ON r.id = prner.rubros_id " +
                "        WHERE p.nombre_corto = '" + nombrePrograma + "' " +
                "        AND prne.catalogos_niveles_estudio_id = " + nivelEstudio + " " +
                "        AND sl.numero_identificacion = '" + cedula + "' " +
                "        AND so.numero_tramite = '" + numeroTramite + "'; " +
                "    END IF; " +
                "END $$ LANGUAGE plpgsql;");

        return scripts;
    }

    public static String getCellString(Cell cell) {
        if (cell == null)
            return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    } else {
                        double val = cell.getNumericCellValue();
                        return (val == (long) val) ? String.valueOf((long) val) : String.valueOf(val);
                    }

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    // Si quieres evaluar el resultado real de la fórmula, no solo la cadena:
                    try {
                        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper()
                                .createFormulaEvaluator();
                        CellValue cellValue = evaluator.evaluate(cell);
                        switch (cellValue.getCellType()) {
                            case STRING:
                                return cellValue.getStringValue().trim();
                            case NUMERIC:
                                return String.valueOf(cellValue.getNumberValue());
                            case BOOLEAN:
                                return String.valueOf(cellValue.getBooleanValue());
                            default:
                                return "";
                        }
                    } catch (Exception e) {
                        return cell.getCellFormula(); // fallback: mostrar la fórmula como texto
                    }

                case BLANK:
                case ERROR:
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
