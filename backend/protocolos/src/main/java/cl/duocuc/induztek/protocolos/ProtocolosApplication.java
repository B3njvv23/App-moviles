package cl.duocuc.induztek.protocolos;
// Aquí levanto el servicio y manejo las rutas de protocolos.
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@RequestMapping("/api/protocolos")
public class ProtocolosApplication {
    private final JdbcTemplate db;

    public ProtocolosApplication(JdbcTemplate db) { this.db = db; }
    public static void main(String[] args) { SpringApplication.run(ProtocolosApplication.class, args); }

    @Bean CommandLineRunner crearTabla() {
        return args -> db.execute("CREATE TABLE IF NOT EXISTS protocolos (id VARCHAR(80) PRIMARY KEY, contenido CLOB NOT NULL, fotografia BLOB)");
    }

    @PutMapping("/sync")
    Map<String, String> sincronizar(@RequestBody List<Map<String, String>> filas) {
        db.update("DELETE FROM protocolos");
        filas.forEach(fila -> {
            byte[] foto = fila.containsKey("fotografia")
                    ? java.util.Base64.getDecoder().decode(fila.get("fotografia")) : null;
            db.update("MERGE INTO protocolos(id,contenido,fotografia) KEY(id) VALUES(?,?,?)",
                    fila.get("id"), fila.get("contenido"), foto);
        });
        return Map.of("estado", "sincronizado");
    }

    @GetMapping
    List<Map<String, Object>> listar() {
        return db.queryForList("SELECT id, CAST(contenido AS VARCHAR) AS contenido, fotografia FROM protocolos ORDER BY id");
    }

    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> buscar(@PathVariable String id) {
        var filas = db.queryForList("SELECT id, CAST(contenido AS VARCHAR) AS contenido, fotografia FROM protocolos WHERE id=?", id);
        return filas.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(filas.get(0));
    }

    @PutMapping("/{id}")
    ResponseEntity<Map<String, String>> guardar(@PathVariable String id, @RequestBody Map<String, String> body) {
        if (!body.containsKey("contenido") || body.get("contenido").isBlank()) return ResponseEntity.badRequest().build();
        byte[] foto = body.containsKey("fotografia") ? java.util.Base64.getDecoder().decode(body.get("fotografia")) : null;
        db.update("MERGE INTO protocolos(id,contenido,fotografia) KEY(id) VALUES(?,?,?)", id, body.get("contenido"), foto);
        return ResponseEntity.ok(Map.of("estado", "guardado", "id", id));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminar(@PathVariable String id) {
        return db.update("DELETE FROM protocolos WHERE id=?", id) == 0
                ? ResponseEntity.notFound().build() : ResponseEntity.noContent().build();
    }
}
