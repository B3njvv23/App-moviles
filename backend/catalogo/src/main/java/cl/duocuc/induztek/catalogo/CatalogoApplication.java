package cl.duocuc.induztek.catalogo;
// Aquí levanto el servicio y manejo las rutas del catálogo.
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
@RequestMapping("/api/catalogo")
public class CatalogoApplication {
    private final JdbcTemplate db;

    public CatalogoApplication(JdbcTemplate db) { this.db = db; }
    public static void main(String[] args) { SpringApplication.run(CatalogoApplication.class, args); }

    @Bean CommandLineRunner crearTabla() {
        return args -> db.execute("CREATE TABLE IF NOT EXISTS catalogo (tipo VARCHAR(40), id VARCHAR(80), contenido CLOB NOT NULL, PRIMARY KEY(tipo,id))");
    }

    @PutMapping("/sync")
    Map<String, String> sincronizar(@RequestBody Map<String, List<Map<String, String>>> categorias) {
        categorias.forEach((tipo, filas) -> {
            db.update("DELETE FROM catalogo WHERE tipo=?", tipo);
            filas.forEach(fila -> db.update(
                    "MERGE INTO catalogo(tipo,id,contenido) KEY(tipo,id) VALUES(?,?,?)",
                    tipo, fila.get("id"), fila.get("contenido")));
        });
        return Map.of("estado", "sincronizado");
    }

    @GetMapping("/{tipo}")
    List<Map<String, Object>> listar(@PathVariable String tipo) {
        return db.queryForList("SELECT id, CAST(contenido AS VARCHAR) AS contenido FROM catalogo WHERE tipo=? ORDER BY id", tipo);
    }

    @GetMapping("/{tipo}/{id}")
    ResponseEntity<Map<String, Object>> buscar(@PathVariable String tipo, @PathVariable String id) {
        var filas = db.queryForList("SELECT id, CAST(contenido AS VARCHAR) AS contenido FROM catalogo WHERE tipo=? AND id=?", tipo, id);
        return filas.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(filas.get(0));
    }

    @PutMapping("/{tipo}/{id}")
    ResponseEntity<Map<String, String>> guardar(@PathVariable String tipo, @PathVariable String id, @RequestBody Map<String, String> body) {
        if (!body.containsKey("contenido") || body.get("contenido").isBlank()) return ResponseEntity.badRequest().build();
        db.update("MERGE INTO catalogo(tipo,id,contenido) KEY(tipo,id) VALUES(?,?,?)", tipo, id, body.get("contenido"));
        return ResponseEntity.ok(Map.of("estado", "guardado", "id", id));
    }

    @DeleteMapping("/{tipo}/{id}")
    ResponseEntity<Void> eliminar(@PathVariable String tipo, @PathVariable String id) {
        return db.update("DELETE FROM catalogo WHERE tipo=? AND id=?", tipo, id) == 0
                ? ResponseEntity.notFound().build() : ResponseEntity.noContent().build();
    }
}
