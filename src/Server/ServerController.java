package Server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Class permettant de repondre au requêtes afin de d'envoyer les Public Parameters
// et la clé privé associé à l'id reçus par le server
@RestController
@RequestMapping("/api")
public class ServerController {
    IdentificationServer identificationServer= new IdentificationServer();

    @GetMapping("/getPublicParameters")
    public ResponseEntity<String> getPublicParameters(@RequestParam String id) {
        // Call the method to get public parameters
        String JSON = this.identificationServer.send_Public_Parameters_MSK(id);
        // Print the response before sending it to the client
        System.out.println("Server Response: " + JSON);
        return ResponseEntity.ok(JSON);
    }
}

