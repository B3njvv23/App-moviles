package cl.duocuc.induztek.catalogo;
// Aquí pruebo las rutas CRUD del servicio.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogoApplicationTest {
    @Value("${local.server.port}") int port;
    private final HttpClient http = HttpClient.newHttpClient();

    @Test void permiteCrearLeerYEliminar() throws Exception {
        var url = "http://localhost:" + port + "/api/catalogo/pruebas/catalogo-test";
        var guardar = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"contenido\":\"{\\\"nombre\\\":\\\"Planta\\\"}\"}"))
                .build();
        assertEquals(200, http.send(guardar, HttpResponse.BodyHandlers.ofString()).statusCode());
        var lectura = HttpRequest.newBuilder(URI.create(url)).GET().build();
        var respuesta = http.send(lectura, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Planta"));
        var borrar = HttpRequest.newBuilder(URI.create(url)).DELETE().build();
        http.send(borrar, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, http.send(lectura, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
